package com.cranesvarsity.template.service;

import com.cranesvarsity.template.dao.ManageTheoryTestsDao;
import com.cranesvarsity.template.dto.TheoryQuestionPaper;
import com.cranesvarsity.template.dto.TheoryQuestionPaperFile;
import com.cranesvarsity.template.dto.TheoryTestItem;
import com.cranesvarsity.template.dto.TheoryTestScheduleRow;
import com.cranesvarsity.template.model.cranescrm.Admission;
import com.cranesvarsity.template.repository.AdmissionRepository;
import com.cranesvarsity.template.security.AuthenticatedStudent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Backs the Theory/Lab Test page — successor of legacy theory_test_schedule.jsp
 * and TheoryTestScheduleServlet.
 *
 * Theory and lab tests are the same mechanism throughout: one schedule table,
 * one handout flow, one PDF pipe. test_type only tags which badge the card
 * draws.
 *
 * Two rules from the legacy flow are carried over exactly:
 *
 *  1. A test slot holds a POOL of question-paper variants. Students in one
 *     batch are spread across them, so the paper next to you is probably not
 *     the one in front of you.
 *  2. Once you have been handed a variant it is yours for the whole test.
 *     Reloading, reopening or calling the endpoint again returns the same
 *     paper — there is no re-rolling for an easier one.
 *
 * One thing is deliberately NOT carried over: the legacy servlet had no
 * server-side time check at all. The only thing stopping a student pulling the
 * paper an hour early was a disabled button in JavaScript, and anyone who
 * posted to the endpoint directly got the PDF regardless. Here the window is
 * re-checked against the schedule row on every call — both when the paper is
 * handed out and again whenever the PDF itself is fetched — the same way the
 * MCQ flow's access gate does it.
 */
@Service
public class TheoryTestService {

    private static final Logger log = LoggerFactory.getLogger(TheoryTestService.class);

    private static final ZoneId IST = ZoneId.of("Asia/Kolkata");

    /**
     * The paper opens a couple of minutes early so a student sitting there
     * refreshing at 10:59:50 is not told the test has not started. The far edge
     * gets no such grace — when time is up, it is up.
     */
    private static final Duration EARLY_GRACE = Duration.ofMinutes(2);

    private final AdmissionRepository admissionRepository;
    private final ManageTheoryTestsDao dao;
    private final SecureRandom random = new SecureRandom();

    public TheoryTestService(AdmissionRepository admissionRepository, ManageTheoryTestsDao dao) {
        this.admissionRepository = admissionRepository;
        this.dao = dao;
    }

    // ─── The schedule listing ─────────────────────────────────────────

    /**
     * Every test for this student's batch from yesterday onwards, each already
     * bucketed into upcoming/live/ended against the server's IST clock.
     *
     * The browser is handed second counts rather than timestamps, so the
     * countdown it ticks is anchored to the server rather than to whatever the
     * device clock happens to say.
     */
    public List<TheoryTestItem> getSchedule(AuthenticatedStudent student) {
        Admission admission = requireAdmission(student);
        LocalDateTime now = LocalDateTime.now(IST);

        List<TheoryTestItem> items = new ArrayList<>();
        for (TheoryTestScheduleRow row : dao.findByBatch(admission.getBatchno())) {
            LocalDateTime startsAt = parseSchedule(row.testDate(), row.testStartTime());
            LocalDateTime endsAt   = parseSchedule(row.testDate(), row.testEndTime());

            items.add(new TheoryTestItem(
                    row.theoryTestId(),
                    row.domainName(),
                    normaliseType(row.testType()),
                    row.moduleName(),
                    row.batchName(),
                    row.testDate(),
                    trimSeconds(row.testStartTime()),
                    trimSeconds(row.testEndTime()),
                    row.totalMarks(),
                    row.totalQuestions(),
                    statusOf(now, startsAt, endsAt),
                    secondsBetween(now, startsAt),
                    secondsBetween(now, endsAt),
                    secondsBetween(startsAt, endsAt)
            ));
        }

        // Live first, then upcoming, then ended — the ordering the legacy page
        // did in JavaScript, done here so every client agrees on it.
        items.sort((a, b) -> {
            int byStatus = Integer.compare(statusRank(a.status()), statusRank(b.status()));
            if (byStatus != 0) return byStatus;
            return (a.testDate() + " " + a.testStartTime())
                    .compareTo(b.testDate() + " " + b.testStartTime());
        });
        return items;
    }

    // ─── Handing out the question paper ───────────────────────────────

    /**
     * Give this student their question paper for this test — drawing one the
     * first time, returning that same one ever after.
     *
     * A refusal comes back as an allowed=false result rather than an HTTP
     * error, so the card can show what actually stopped it.
     */
    @Transactional
    public TheoryQuestionPaper assignQuestionPaper(AuthenticatedStudent student, String theoryTestId) {
        Admission admission = requireAdmission(student);

        Optional<TheoryTestScheduleRow> found = dao.findForBatch(theoryTestId, admission.getBatchno());
        if (found.isEmpty()) {
            // Either no such test, or it belongs to another batch. One answer
            // for both, so the endpoint cannot be used to probe the schedule.
            return TheoryQuestionPaper.block("Test Not Available",
                    "This test is not available for your batch.");
        }
        TheoryTestScheduleRow row = found.get();

        Optional<TheoryQuestionPaper> refusal = checkWindow(row);
        if (refusal.isPresent()) {
            return refusal.get();
        }

        String questionId = resolveAssignment(row, student.regNo());
        if (questionId == null) {
            return TheoryQuestionPaper.block("Question Paper Not Ready",
                    "No question paper has been uploaded for this test yet. Please inform your trainer.");
        }

        Optional<TheoryQuestionPaperFile> file = dao.findQuestionPaperFile(questionId);
        if (file.isEmpty()) {
            log.warn("Theory test {} assigned question paper {} to {}, but it has no PDF link",
                    theoryTestId, questionId, student.regNo());
            return TheoryQuestionPaper.block("Question Paper Not Ready",
                    "Your question paper could not be loaded. Please inform your trainer.");
        }

        // The label, not the row id: "CPP-VLSI - 14" means something to an
        // invigilator asking which set a student is holding; "654" does not.
        String label = file.get().code();
        return TheoryQuestionPaper.allow(
                row.theoryTestId(),
                normaliseType(row.testType()),
                row.moduleName(),
                (label == null || label.isBlank()) ? questionId : label,
                "/student/assessment/theory-tests/" + row.theoryTestId() + "/question-paper.pdf",
                secondsBetween(LocalDateTime.now(IST), parseSchedule(row.testDate(), row.testEndTime()))
        );
    }

    /**
     * The storage URL of the PDF this student is entitled to right now, for the
     * streaming endpoint to fetch behind their back.
     *
     * Re-runs every check rather than trusting that assignQuestionPaper ran
     * first: this is the URL that ends up in the browser, so it has to stand on
     * its own. Refusals here are real HTTP errors — there is no card to show a
     * friendly message on, and a scripted caller has earned a 403.
     */
    @Transactional(readOnly = true)
    public String resolvePdfLink(AuthenticatedStudent student, String theoryTestId) {
        Admission admission = requireAdmission(student);

        TheoryTestScheduleRow row = dao.findForBatch(theoryTestId, admission.getBatchno())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "This test is not available for your batch."));

        Optional<TheoryQuestionPaper> refusal = checkWindow(row);
        if (refusal.isPresent()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, refusal.get().message());
        }

        // No drawing here — this endpoint can only serve a paper that was
        // already handed out through the proper one.
        String questionId = dao.findAssignedQuestionId(theoryTestId, student.regNo())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN,
                        "No question paper has been assigned to you for this test."));

        return dao.findQuestionPaperFile(questionId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "The question paper file is missing."))
                .link();
    }

    // ─── Internals ────────────────────────────────────────────────────

    /** The gate the legacy servlet never had: is this test actually open right now? */
    private Optional<TheoryQuestionPaper> checkWindow(TheoryTestScheduleRow row) {
        LocalDateTime now      = LocalDateTime.now(IST);
        LocalDateTime startsAt = parseSchedule(row.testDate(), row.testStartTime());
        LocalDateTime endsAt   = parseSchedule(row.testDate(), row.testEndTime());

        if (startsAt == null || endsAt == null) {
            log.warn("Theory test {} has an unparseable schedule: date={} start={} end={}",
                    row.theoryTestId(), row.testDate(), row.testStartTime(), row.testEndTime());
            return Optional.of(TheoryQuestionPaper.block("Test Not Available",
                    "This test has no valid schedule. Please inform your trainer."));
        }
        if (now.isBefore(startsAt.minus(EARLY_GRACE))) {
            return Optional.of(TheoryQuestionPaper.block("Test Not Started",
                    "This test starts at " + trimSeconds(row.testStartTime())
                            + " IST on " + row.testDate() + ". The question paper opens then."));
        }
        if (now.isAfter(endsAt)) {
            return Optional.of(TheoryQuestionPaper.block("Test Ended",
                    "This test closed at " + trimSeconds(row.testEndTime())
                            + " IST. The question paper is no longer available."));
        }
        return Optional.empty();
    }

    /**
     * The variant already locked to this student, or a freshly drawn one.
     *
     * The insert is conditional and the read that follows is authoritative, so
     * two simultaneous clicks converge on one paper instead of racing to assign
     * two different ones.
     */
    private String resolveAssignment(TheoryTestScheduleRow row, String regNo) {
        Optional<String> existing = dao.findAssignedQuestionId(row.theoryTestId(), regNo);
        if (existing.isPresent()) {
            return existing.get();
        }

        List<String> pool = parseQuestionPool(row.questions());
        if (pool.isEmpty()) {
            log.warn("Theory test {} has no question papers configured (questions='{}')",
                    row.theoryTestId(), row.questions());
            return null;
        }

        String drawn = pool.get(random.nextInt(pool.size()));
        dao.assignQuestionIfAbsent(row.theoryTestId(), regNo, drawn);

        // Re-read rather than returning `drawn`: if another request won the
        // race, the row that actually landed is the one that counts.
        String assigned = dao.findAssignedQuestionId(row.theoryTestId(), regNo).orElse(drawn);
        log.info("Theory test {} handed question paper {} to {}", row.theoryTestId(), assigned, regNo);
        return assigned;
    }

    /** "12,13,14,15,16" to [12, 13, 14, 15, 16], blanks and duplicates dropped. */
    private List<String> parseQuestionPool(String csv) {
        List<String> pool = new ArrayList<>();
        if (csv == null || csv.isBlank()) {
            return pool;
        }
        for (String part : csv.split(",")) {
            String id = part.trim();
            if (!id.isEmpty() && !pool.contains(id)) {
                pool.add(id);
            }
        }
        return pool;
    }

    private Admission requireAdmission(AuthenticatedStudent student) {
        return admissionRepository.findById(student.regNo())
                .orElseThrow(() -> new IllegalStateException(
                        "Student record not found for registration_no=" + student.regNo()));
    }

    private String statusOf(LocalDateTime now, LocalDateTime startsAt, LocalDateTime endsAt) {
        if (startsAt == null || endsAt == null) return "upcoming";
        if (now.isBefore(startsAt)) return "upcoming";
        if (now.isAfter(endsAt)) return "ended";
        return "live";
    }

    private int statusRank(String status) {
        return switch (status) {
            case "live" -> 0;
            case "upcoming" -> 1;
            default -> 2;
        };
    }

    /** Seconds from {@code from} to {@code to}, floored at 0. */
    private long secondsBetween(LocalDateTime from, LocalDateTime to) {
        if (from == null || to == null) return 0;
        return Math.max(Duration.between(from, to).getSeconds(), 0);
    }

    /**
     * Combine the schedule's DATE and TIME columns into one IST instant.
     * Accepts both "HH:mm" and "HH:mm:ss", which the legacy table mixes.
     */
    private LocalDateTime parseSchedule(String date, String time) {
        if (date == null || date.isBlank()) return null;
        try {
            LocalDate day = LocalDate.parse(date.trim());
            String raw = (time == null || time.isBlank()) ? "00:00:00" : time.trim();
            LocalTime clock = LocalTime.parse(raw.length() == 5 ? raw + ":00" : raw);
            return LocalDateTime.of(day, clock);
        } catch (Exception e) {
            return null;
        }
    }

    /** "11:54:00" to "11:54" for display. */
    private String trimSeconds(String time) {
        if (time == null) return null;
        String raw = time.trim();
        return raw.length() >= 8 && raw.charAt(2) == ':' ? raw.substring(0, 5) : raw;
    }

    /** Anything not explicitly tagged as a lab test is a theory test, as before. */
    private String normaliseType(String testType) {
        return (testType == null || testType.isBlank()) ? "theory" : testType.trim().toLowerCase();
    }
}
