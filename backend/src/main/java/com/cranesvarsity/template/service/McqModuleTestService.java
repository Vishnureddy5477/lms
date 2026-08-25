package com.cranesvarsity.template.service;

import com.cranesvarsity.template.dao.ModuleTestAnswerLogDao;
import com.cranesvarsity.template.dao.ModuleTestQuestionDao;
import com.cranesvarsity.template.dao.ModuleTestResultDao;
import com.cranesvarsity.template.dao.ModuleTestSessionDao;
import com.cranesvarsity.template.dao.ReexamSlotDao;
import com.cranesvarsity.template.dto.*;
import com.cranesvarsity.template.model.cranescrm.Admission;
import com.cranesvarsity.template.repository.AdmissionRepository;
import com.cranesvarsity.template.security.AuthenticatedStudent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * The MCQ Module Test engine — successor of validateTestAccess.jsp,
 * manage-mcq-test-panel.jsp, saveAnswer.jsp, getTestTimeRemaining.jsp and
 * manage-mcq-test-submit.jsp.
 *
 * The business rules are carried over unchanged: one active session per
 * student, no retake after a pass, two free attempts then a paid slot, at least
 * 40 questions, 0.5 marks each over 40 questions with a 50% pass line, and a
 * retake auto-scheduled two days out only when a FIRST attempt fails.
 *
 * What is different is where the trust sits. The legacy pages took regno,
 * batchno, the test times and the exam id straight from the request, and did no
 * ownership check at all — any exam_id could be posted by anyone. Here every
 * one of those comes from the authenticated principal or from the database, and
 * every session-scoped call verifies the session belongs to the caller.
 */
@Service
public class McqModuleTestService {

    private static final Logger log = LoggerFactory.getLogger(McqModuleTestService.class);

    private static final int    TOTAL_QUESTIONS     = 40;
    private static final double MARKS_PER_QUESTION  = 0.5;
    private static final double PASS_PERCENT        = 50.0;
    private static final int    MAX_VIOLATIONS      = 3;
    private static final int    SECONDS_PER_QUESTION = 60;
    private static final int    FREE_ATTEMPTS       = 2;
    private static final int    RETAKE_GAP_DAYS     = 2;

    private static final ZoneId IST = ZoneId.of("Asia/Kolkata");
    private static final DateTimeFormatter DATE_TIME = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final DateTimeFormatter DATE_ONLY = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter TIME_ONLY = DateTimeFormatter.ofPattern("HH:mm:ss");

    private final AvailableTestsService availableTestsService;
    private final AdmissionRepository admissionRepository;
    private final ModuleTestSessionDao sessionDao;
    private final ModuleTestQuestionDao questionDao;
    private final ModuleTestAnswerLogDao answerDao;
    private final ModuleTestResultDao resultDao;
    private final ReexamSlotDao reexamSlotDao;

    public McqModuleTestService(AvailableTestsService availableTestsService,
                                AdmissionRepository admissionRepository,
                                ModuleTestSessionDao sessionDao,
                                ModuleTestQuestionDao questionDao,
                                ModuleTestAnswerLogDao answerDao,
                                ModuleTestResultDao resultDao,
                                ReexamSlotDao reexamSlotDao) {
        this.availableTestsService = availableTestsService;
        this.admissionRepository = admissionRepository;
        this.sessionDao = sessionDao;
        this.questionDao = questionDao;
        this.answerDao = answerDao;
        this.resultDao = resultDao;
        this.reexamSlotDao = reexamSlotDao;
    }

    // ─── The access gate ──────────────────────────────────────────────

    /**
     * Every check that can stop a student starting this test, in the legacy
     * order, each with its own reason.
     *
     * The test identity is re-resolved against the student's OWN available-test
     * list rather than taken from the request, so a student can only start
     * something genuinely scheduled for their batch — and the time window comes
     * from that row, not from client-supplied start/end times as it did before.
     */
    public McqAccessResult accessCheck(AuthenticatedStudent student, String module, int testNo) {
        Admission admission = requireAdmission(student);
        String batch = admission.getBatchno();

        Optional<AvailableTestItem> scheduled = findScheduledTest(student, module, testNo);
        if (scheduled.isEmpty()) {
            return McqAccessResult.block("Test Not Available",
                    "This test is not currently available for your batch.");
        }
        AvailableTestItem test = scheduled.get();

        // (a) time window — both edges, each with its own message
        LocalDateTime now = LocalDateTime.now(IST);
        LocalDateTime startsAt = parseSchedule(test.testDate(), test.testStartTime());
        LocalDateTime endsAt   = parseSchedule(test.testDate(), test.testEndTime());

        if (startsAt != null && now.isBefore(startsAt)) {
            return McqAccessResult.block("Test Not Started",
                    "This test starts at " + test.testStartTime() + " IST. Please wait until the scheduled time.");
        }
        if (endsAt != null && now.isAfter(endsAt)) {
            return McqAccessResult.block("Test Ended",
                    "This test closed at " + test.testEndTime() + " IST. You can no longer take it.");
        }

        // (b) free the student from any OTHER open session so a crashed tab
        //     cannot lock them out. The session for THIS test is left alone so
        //     it resumes where they left off.
        int released = sessionDao.releaseOtherSessions(student.regNo(), module, testNo);
        if (released > 0) {
            log.info("Released {} stale session(s) for regno={} before starting {} test {}",
                    released, student.regNo(), module, testNo);
        }

        // (c) a pass is final
        if (resultDao.hasPassed(student.regNo(), module, testNo, batch)) {
            return McqAccessResult.block("Test Already Passed",
                    "You have already passed this test. No retake is offered after a pass.");
        }

        // (d) two free attempts, then a paid slot is required
        int failed = resultDao.countFailedAttempts(student.regNo(), module, testNo, batch);
        if (failed >= FREE_ATTEMPTS
                && !reexamSlotDao.hasUnusedSlot(student.regNo(), module, testNo, batch)) {
            return McqAccessResult.block("Re-Exam Payment Required",
                    "You have used your free attempts for this test. " +
                            "Please contact the Training Delivery team to arrange your next attempt.");
        }

        // (e) the bank must be able to fill the paper
        int available = questionDao.countActive(module, testNo);
        if (available < TOTAL_QUESTIONS) {
            log.warn("Question bank too small: module={} test={} has {} active questions (need {})",
                    module, testNo, available, TOTAL_QUESTIONS);
            return McqAccessResult.block("Test Unavailable",
                    "This test needs at least " + TOTAL_QUESTIONS +
                            " questions but only " + available + " are available. Please contact the administrator.");
        }

        // The paper can be filled, but not without repeating a question — the
        // test still runs (nothing scheduled should break), the bank gets flagged.
        int distinct = questionDao.countDistinctActive(module, testNo);
        if (distinct < TOTAL_QUESTIONS) {
            log.warn("ADMIN ACTION NEEDED — module={} test={} has {} active rows but only {} DISTINCT questions; " +
                            "{} slot(s) will repeat a question. The bank needs more authored questions.",
                    module, testNo, available, distinct, TOTAL_QUESTIONS - distinct);
        }

        return McqAccessResult.allow();
    }

    // ─── Start / resume ───────────────────────────────────────────────

    /**
     * Open the attempt, or hand back the one already in progress.
     *
     * Start and resume are deliberately the same call: if a session for this
     * module + test is still open, it is reused, so the student gets back the
     * identical questions in the identical order with their answers restored and
     * the clock where they left it. That is what makes a crashed tab or a
     * reloaded page harmless.
     */
    @Transactional
    public McqSessionState startOrResume(AuthenticatedStudent student, String module, int testNo) {
        // Never trust that the client called the gate first.
        McqAccessResult gate = accessCheck(student, module, testNo);
        if (!gate.allowed()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, gate.message());
        }

        Admission admission = requireAdmission(student);
        String batch = admission.getBatchno();
        AvailableTestItem test = findScheduledTest(student, module, testNo)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN, "Test not available."));

        ModuleTestSessionDao.SessionRow session = sessionDao
                .findOpenSession(student.regNo(), module, testNo)
                .orElseGet(() -> {
                    LocalDateTime now = LocalDateTime.now(IST);
                    // Fixed at session start — see ModuleTestResultDao#upsertResult
                    // for why recomputing this at submit time was the bug.
                    int attemptNo = resultDao.maxAttempt(student.regNo(), module, testNo, batch) + 1;
                    int id = sessionDao.createSession(
                            student.regNo(), module, "Module Test " + testNo, test.testDate(),
                            now.format(DATE_TIME), now.format(TIME_ONLY), testNo, attemptNo);
                    log.info("Opened session {} for regno={} module={} test={} attempt={}",
                            id, student.regNo(), module, testNo, attemptNo);
                    return sessionDao.findById(id).orElseThrow();
                });

        assignQuestionsOnce(session.id(), student.regNo(), module, testNo);
        return buildState(session.id(), admission);
    }

    /**
     * Freeze this attempt's paper, once.
     *
     * Distinct questions first, preferring ones the student has not seen before;
     * only if the bank cannot fill 40 distinct slots is the remainder backfilled
     * with repeats, and that is logged for the admin.
     */
    private void assignQuestionsOnce(int sessionId, String regNo, String module, int testNo) {
        if (questionDao.countAssigned(sessionId) > 0) {
            return;
        }

        int assigned = questionDao.assignDistinctQuestions(sessionId, regNo, module, testNo, TOTAL_QUESTIONS);

        if (assigned < TOTAL_QUESTIONS) {
            int shortfall = TOTAL_QUESTIONS - assigned;
            log.warn("Backfilling {} repeated question(s) for session {} (module={} test={}): the bank has only {} " +
                            "distinct questions. Author more questions to remove the repeats.",
                    shortfall, sessionId, module, testNo, assigned);
            questionDao.backfillDuplicates(sessionId, module, testNo, assigned, shortfall);
        }
    }

    public McqSessionState getState(AuthenticatedStudent student, int sessionId) {
        requireOwnedSession(student, sessionId);
        return buildState(sessionId, requireAdmission(student));
    }

    private McqSessionState buildState(int sessionId, Admission admission) {
        ModuleTestSessionDao.SessionRow session = sessionDao.findById(sessionId).orElseThrow();
        List<McqQuestionItem> questions = questionDao.findAssigned(sessionId);
        Map<Integer, Integer> answers = answerDao.findSaved(sessionId);

        return new McqSessionState(
                sessionId,
                session.module(),
                session.noOfTest(),
                session.attemptNo() == null ? 1 : session.attemptNo(),
                admission.getStname(),
                admission.getRegistrationNo(),
                admission.getBatchno(),
                questions.size(),
                questions.size() * MARKS_PER_QUESTION,
                remainingSeconds(session, questions.size()),
                session.violationCount(),
                MAX_VIOLATIONS,
                questions,
                answers
        );
    }

    // ─── Answering ────────────────────────────────────────────────────

    /**
     * Persist a debounced batch of answers, deciding correctness here.
     *
     * The answer key is read server-side for every save. The client sends only
     * which option was picked for which slot; it is never told, and never gets
     * to assert, whether that was right.
     */
    @Transactional
    public McqSyncResponse saveAnswers(AuthenticatedStudent student, int sessionId, McqAnswerSave request) {
        ModuleTestSessionDao.SessionRow session = requireOwnedSession(student, sessionId);

        if (request.answers() != null && !request.answers().isEmpty()) {
            Map<Integer, Integer> answerKey = questionDao.findAnswerKey(sessionId);
            Map<Integer, Integer> slotQuestions = questionDao.findSlotQuestionIds(sessionId);

            List<ModuleTestAnswerLogDao.AnswerRow> rows = new ArrayList<>();
            for (McqAnswerSave.Item item : request.answers()) {
                Integer questionId = slotQuestions.get(item.questionOrder());
                if (questionId == null) {
                    // Slot isn't part of this attempt's paper — ignore rather than trust it.
                    continue;
                }
                int selected = item.selectedOption();
                if (selected < 0 || selected > 4) {
                    selected = 0;
                }
                Integer correctOption = answerKey.get(item.questionOrder());
                boolean correct = selected != 0 && correctOption != null && selected == correctOption;
                rows.add(new ModuleTestAnswerLogDao.AnswerRow(item.questionOrder(), questionId, selected, correct));
            }
            answerDao.upsertBatch(sessionId, rows);
        }

        return sync(session);
    }

    // ─── Proctoring ───────────────────────────────────────────────────

    /**
     * Record a strike.
     *
     * The client reports the event; the server owns the count. That is why a
     * refresh, a reopened tab or a tampered page cannot wind strikes back — the
     * running total lives on the session row and comes back on every resume.
     */
    @Transactional
    public McqSyncResponse reportViolation(AuthenticatedStudent student, int sessionId, McqViolationRequest request) {
        ModuleTestSessionDao.SessionRow session = requireOwnedSession(student, sessionId);

        String type = request.type() == null ? "unknown" : request.type();
        String reason = request.reason() == null ? "" : request.reason();

        int count = sessionDao.incrementViolation(sessionId);
        sessionDao.logViolation(sessionId, type, reason);
        log.info("Violation {} of {} on session {} (regno={}): {} — {}",
                count, MAX_VIOLATIONS, sessionId, student.regNo(), type, reason);

        long remaining = remainingSeconds(session, questionDao.countAssigned(sessionId));
        boolean limitReached = count >= MAX_VIOLATIONS;

        return new McqSyncResponse(remaining, count, MAX_VIOLATIONS, limitReached || remaining <= 0,
                limitReached ? "Maximum warnings reached — your test is being submitted."
                             : (remaining <= 0 ? "Time is up." : null));
    }

    private McqSyncResponse sync(ModuleTestSessionDao.SessionRow session) {
        int total = questionDao.countAssigned(session.id());
        long remaining = remainingSeconds(session, total);
        int violations = sessionDao.findById(session.id())
                .map(ModuleTestSessionDao.SessionRow::violationCount).orElse(session.violationCount());

        boolean autoSubmit = remaining <= 0 || violations >= MAX_VIOLATIONS;
        String reason = remaining <= 0 ? "Time is up."
                : violations >= MAX_VIOLATIONS ? "Maximum warnings reached — your test is being submitted."
                : null;

        return new McqSyncResponse(remaining, violations, MAX_VIOLATIONS, autoSubmit, reason);
    }

    public McqSyncResponse heartbeat(AuthenticatedStudent student, int sessionId) {
        return sync(requireOwnedSession(student, sessionId));
    }

    // ─── Submission ───────────────────────────────────────────────────

    /**
     * Score and close the attempt.
     *
     * Safe to call twice: the result is keyed on the session id, so a network
     * retry updates the same row instead of creating another. The legacy page
     * scored on a GET page load with no such key, which is how one student ended
     * up with 25 copies of a single attempt.
     */
    @Transactional
    public McqSubmitResult submit(AuthenticatedStudent student, int sessionId, boolean autoSubmitted) {
        ModuleTestSessionDao.SessionRow session = requireOwnedSession(student, sessionId);
        Admission admission = requireAdmission(student);
        String batch = admission.getBatchno();

        String module = session.module();
        int testNo = session.noOfTest();
        int attemptNo = session.attemptNo() == null ? 1 : session.attemptNo();

        int totalSlots = questionDao.countAssigned(sessionId);
        if (totalSlots == 0) {
            totalSlots = TOTAL_QUESTIONS;
        }

        ModuleTestAnswerLogDao.Tally tally = answerDao.tally(sessionId);
        int correct = tally.correct();
        int incorrect = tally.incorrect();
        // Covers both a slot cleared back to blank and one never opened at all.
        int notAttempted = Math.max(0, totalSlots - correct - incorrect);

        double obtainedMarks = correct * MARKS_PER_QUESTION;
        double totalMarks = totalSlots * MARKS_PER_QUESTION;
        double percentage = totalMarks == 0 ? 0 : (obtainedMarks / totalMarks) * 100.0;
        String status = percentage >= PASS_PERCENT ? "Pass" : "Fail";
        boolean failed = "Fail".equals(status);

        // A failed FIRST attempt is rescheduled automatically two days out.
        // Later failures fall through to the paid re-exam rule instead.
        String nextDate = (failed && attemptNo == 1)
                ? LocalDate.now(IST).plusDays(RETAKE_GAP_DAYS).format(DATE_ONLY)
                : "NA";

        resultDao.upsertResult(sessionId, admission.getStname(), student.regNo(), admission.getEmail(),
                module, testNo, obtainedMarks, totalMarks, String.format("%.2f%%", percentage),
                status, attemptNo, nextDate, batch);

        sessionDao.closeSession(sessionId, autoSubmitted);

        // A third or later failed attempt burns one paid slot. Non-fatal: the
        // score is already saved, so a failure here must not undo it.
        if (failed && attemptNo > FREE_ATTEMPTS) {
            try {
                int consumed = reexamSlotDao.consumeOldestSlot(student.regNo(), module, testNo, batch);
                log.info("Consumed {} re-exam slot(s) for regno={} module={} test={} attempt={}",
                        consumed, student.regNo(), module, testNo, attemptNo);
            } catch (Exception e) {
                log.warn("Could not consume re-exam slot for regno={} module={} test={}: {}",
                        student.regNo(), module, testNo, e.getMessage());
            }
        }

        log.info("Submitted session {} regno={} module={} test={} attempt={} -> {} ({}/{} marks, {}%)",
                sessionId, student.regNo(), module, testNo, attemptNo, status,
                obtainedMarks, totalMarks, String.format("%.2f", percentage));

        return new McqSubmitResult(
                status, correct, incorrect, notAttempted, obtainedMarks, totalMarks,
                String.format("%.2f", percentage), attemptNo, nextDate,
                failed && attemptNo >= FREE_ATTEMPTS, autoSubmitted,
                module, testNo, admission.getStname(), student.regNo(), batch);
    }

    // ─── Helpers ──────────────────────────────────────────────────────

    /**
     * The session must exist AND belong to the caller.
     *
     * The legacy endpoints took exam_id straight from the request with no such
     * check, so one student could have read or written another's attempt.
     */
    private ModuleTestSessionDao.SessionRow requireOwnedSession(AuthenticatedStudent student, int sessionId) {
        ModuleTestSessionDao.SessionRow session = sessionDao.findById(sessionId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Test session not found."));

        if (!session.regNo().equalsIgnoreCase(student.regNo())) {
            log.warn("regno={} attempted to access session {} owned by {}",
                    student.regNo(), sessionId, session.regNo());
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "This test session is not yours.");
        }
        return session;
    }

    private Admission requireAdmission(AuthenticatedStudent student) {
        return admissionRepository.findById(student.regNo())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN,
                        "Student record not found for registration_no=" + student.regNo()));
    }

    private Optional<AvailableTestItem> findScheduledTest(AuthenticatedStudent student, String module, int testNo) {
        return availableTestsService.getAvailableTests(student).stream()
                .filter(t -> t.moduleName() != null && t.moduleName().equalsIgnoreCase(module))
                .filter(t -> parseIntSafe(t.mcqTestNo()) == testNo)
                .findFirst();
    }

    /** Server-authoritative countdown, measured from when the session opened. */
    private long remainingSeconds(ModuleTestSessionDao.SessionRow session, int totalQuestions) {
        int duration = (totalQuestions <= 0 ? TOTAL_QUESTIONS : totalQuestions) * SECONDS_PER_QUESTION;
        try {
            LocalDateTime startedAt = LocalDateTime.parse(session.loginDate(), DATE_TIME);
            long elapsed = Duration.between(startedAt, LocalDateTime.now(IST)).getSeconds();
            return Math.max(0, Math.min(duration, duration - elapsed));
        } catch (Exception e) {
            // Never shorten a student's clock because of a parse problem.
            log.warn("Could not read start time for session {} ('{}') — granting full duration",
                    session.id(), session.loginDate());
            return duration;
        }
    }

    private LocalDateTime parseSchedule(String date, String time) {
        try {
            String t = (time == null || time.isBlank()) ? "00:00:00" : time.trim();
            if (t.length() == 5) {
                t = t + ":00";
            }
            return LocalDateTime.parse(date.trim() + " " + t, DATE_TIME);
        } catch (Exception e) {
            log.warn("Unparseable test schedule date='{}' time='{}' — skipping that time-window edge", date, time);
            return null;
        }
    }

    private static int parseIntSafe(String s) {
        try {
            return Integer.parseInt(s == null ? "" : s.trim());
        } catch (NumberFormatException e) {
            return -1;
        }
    }
}
