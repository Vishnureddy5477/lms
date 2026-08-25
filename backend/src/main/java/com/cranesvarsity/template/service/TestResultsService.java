package com.cranesvarsity.template.service;

import com.cranesvarsity.template.dao.ModuleTestAnswerDao;
import com.cranesvarsity.template.dto.TestResultRow;
import com.cranesvarsity.template.dto.TestResultsResponse;
import com.cranesvarsity.template.dto.TestResultsSummary;
import com.cranesvarsity.template.security.AuthenticatedStudent;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Backs the MCQ Test Result Details page — successor of legacy
 * getTestResults.jsp / mcq-test-result-details.jsp.
 *
 * Deliberately does NOT join back to a question bank to resolve the picked
 * option number to option text — moduletestanswer.question_id does not map
 * reliably to one bank (ids get reused/recycled when the question bank is
 * re-imported), and joining would silently show WRONG answer text for old
 * rows. Same answer-display fallback as legacy: if the row was graded
 * Correct, the student's pick IS the correct answer by definition, so show
 * the real text for those; otherwise show "Option N" or the raw value.
 */
@Service
public class TestResultsService {

    private final ModuleTestAnswerDao dao;

    public TestResultsService(ModuleTestAnswerDao dao) {
        this.dao = dao;
    }

    public List<String> getModules(AuthenticatedStudent student) {
        return dao.findDistinctSubjects(student.regNo());
    }

    public TestResultsResponse getResults(AuthenticatedStudent student, String subject) {
        List<ModuleTestAnswerDao.AnswerRow> rows = dao.findByRegNoAndSubject(student.regNo(), subject);

        List<TestResultRow> results = new ArrayList<>();
        int totalQuestions = 0;
        int correctAnswers = 0;
        int incorrectAnswers = 0;
        Set<String> uniqueAttempts = new HashSet<>();

        for (ModuleTestAnswerDao.AnswerRow row : rows) {
            String test = row.test() != null ? row.test() : "Test " + (row.answerId() != null ? row.answerId() : "");
            String attempt = row.attempt() != null ? row.attempt() : "1";

            results.add(new TestResultRow(
                    row.examId() != null ? row.examId() : "",
                    row.subject() != null ? row.subject() : "",
                    row.noOfTest() != null ? row.noOfTest() : "",
                    test,
                    row.question() != null ? row.question() : "",
                    displayAnswer(row.answer(), row.status(), row.correctAnswer()),
                    row.answer() != null ? row.answer() : "",
                    row.correctAnswer() != null ? row.correctAnswer() : "",
                    row.status() != null ? row.status() : "",
                    attempt,
                    row.datetime() != null ? row.datetime() : ""
            ));

            totalQuestions++;
            if (row.status() != null && "correct".equalsIgnoreCase(row.status())) {
                correctAnswers++;
            } else {
                incorrectAnswers++;
            }
            uniqueAttempts.add(test + "_" + attempt);
        }

        TestResultsSummary summary = new TestResultsSummary(totalQuestions, correctAnswers, incorrectAnswers, uniqueAttempts.size());
        return new TestResultsResponse(results, summary);
    }

    private String displayAnswer(String answer, String status, String correctAnswer) {
        if (answer == null || answer.isBlank() || "0".equals(answer.trim())) {
            return "Not Attempted";
        }
        if (status != null && "correct".equalsIgnoreCase(status.trim()) && correctAnswer != null && !correctAnswer.isBlank()) {
            return correctAnswer.trim();
        }
        String trimmed = answer.trim();
        if (trimmed.matches("[1-4]")) {
            return "Option " + trimmed;
        }
        return trimmed;
    }
}
