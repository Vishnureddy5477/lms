package com.cranesvarsity.template.service;

import com.cranesvarsity.template.dao.InterviewQuestionsDao;
import com.cranesvarsity.template.dto.InterviewAnswerItem;
import com.cranesvarsity.template.dto.InterviewCompanyModuleItem;
import com.cranesvarsity.template.dto.InterviewQuestionItem;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Backs the Industry Interview Questions page — successor of legacy
 * manage-interview-questions.jsp / view-interview-questions.jsp / get-interview-answers.jsp.
 */
@Service
public class InterviewQuestionsService {

    private final InterviewQuestionsDao dao;

    public InterviewQuestionsService(InterviewQuestionsDao dao) {
        this.dao = dao;
    }

    public List<InterviewCompanyModuleItem> listCompanyModules() {
        return dao.findCompanyModuleSummary().stream()
                .map(row -> new InterviewCompanyModuleItem(row.clientName(), row.moduleCategory(), row.moduleName(), row.questionCount()))
                .toList();
    }

    public List<InterviewQuestionItem> listQuestions(String moduleCategory) {
        return dao.findQuestionsByModuleCategory(moduleCategory).stream()
                .map(row -> new InterviewQuestionItem(row.id(), row.moduleCategory(), row.moduleName(), row.question(), row.answerCount()))
                .toList();
    }

    public List<InterviewAnswerItem> listAnswers(int questionId) {
        return dao.findAnswersByQuestionId(questionId).stream()
                .map(row -> new InterviewAnswerItem(row.answerId(), row.answerText(), row.ansCount(), row.createdBy(), row.createdAt()))
                .toList();
    }
}
