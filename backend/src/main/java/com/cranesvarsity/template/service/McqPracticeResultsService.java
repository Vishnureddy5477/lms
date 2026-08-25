package com.cranesvarsity.template.service;

import com.cranesvarsity.template.dao.SampleTestAnswerDao;
import com.cranesvarsity.template.dto.McqPracticeResultItem;
import com.cranesvarsity.template.security.AuthenticatedStudent;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/** Backs the MCQ Practice Results page — successor of legacy mcq-practice-test-results.jsp. */
@Service
public class McqPracticeResultsService {

    private final SampleTestAnswerDao dao;

    public McqPracticeResultsService(SampleTestAnswerDao dao) {
        this.dao = dao;
    }

    public List<McqPracticeResultItem> list(AuthenticatedStudent student) {
        List<McqPracticeResultItem> result = new ArrayList<>();
        int slNo = 1;
        for (SampleTestAnswerDao.ResultRow row : dao.findByRegNo(student.regNo())) {
            result.add(new McqPracticeResultItem(
                    slNo++, student.regNo(), row.moduleName(), row.chapter(),
                    row.totalCorrect(), row.totalIncorrect(), row.totalNotAttempt(),
                    String.format("%.1f %%", row.marksPercent()), row.examDate()
            ));
        }
        return result;
    }
}
