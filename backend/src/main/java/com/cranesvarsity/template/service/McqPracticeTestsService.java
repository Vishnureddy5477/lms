package com.cranesvarsity.template.service;

import com.cranesvarsity.template.dao.SampleTestQuestionsDao;
import com.cranesvarsity.template.dto.McqPracticeTestItem;
import org.springframework.stereotype.Service;

import java.util.List;

/** Backs the MCQ Practice Tests listing — successor of legacy mcq-practice-test.jsp's browse table. */
@Service
public class McqPracticeTestsService {

    private final SampleTestQuestionsDao dao;

    public McqPracticeTestsService(SampleTestQuestionsDao dao) {
        this.dao = dao;
    }

    public List<McqPracticeTestItem> list() {
        return dao.findModuleChapterCounts().stream()
                .map(row -> new McqPracticeTestItem(
                        row.moduleName(), row.chapter(), row.questionsCount(), row.questionsCount() + " Minutes"
                ))
                .toList();
    }
}
