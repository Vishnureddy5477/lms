package com.cranesvarsity.template.service;

import com.cranesvarsity.template.dao.ProgrammingPracticeQuestionsDao;
import com.cranesvarsity.template.dto.ProgrammingExerciseItem;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/** Backs the Programming Exercises page — successor of legacy programming-practice-questions.jsp (a curated external-link list). */
@Service
public class ProgrammingExercisesService {

    private final ProgrammingPracticeQuestionsDao dao;

    public ProgrammingExercisesService(ProgrammingPracticeQuestionsDao dao) {
        this.dao = dao;
    }

    public List<ProgrammingExerciseItem> list() {
        List<ProgrammingExerciseItem> result = new ArrayList<>();
        int id = 1;
        for (ProgrammingPracticeQuestionsDao.LinkRow row : dao.findAll()) {
            result.add(new ProgrammingExerciseItem(id++, row.title(), row.link()));
        }
        return result;
    }
}
