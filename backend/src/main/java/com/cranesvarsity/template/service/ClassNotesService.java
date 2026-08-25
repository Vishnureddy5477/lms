package com.cranesvarsity.template.service;

import com.cranesvarsity.template.dao.DayWiseNotesDao;
import com.cranesvarsity.template.dto.ClassNoteItem;
import com.cranesvarsity.template.security.AuthenticatedStudent;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/** Backs the Class Notes page — successor of legacy class-notes.jsp. Same daywisenotes table, filtered by batch. */
@Service
public class ClassNotesService {

    private final DayWiseNotesDao dayWiseNotesDao;

    public ClassNotesService(DayWiseNotesDao dayWiseNotesDao) {
        this.dayWiseNotesDao = dayWiseNotesDao;
    }

    public List<ClassNoteItem> list(AuthenticatedStudent student) {
        List<ClassNoteItem> result = new ArrayList<>();
        int slNo = 1;
        for (DayWiseNotesDao.NoteRow row : dayWiseNotesDao.findByBatch(student.batch())) {
            result.add(new ClassNoteItem(slNo++, row.postedDate(), row.postedBy(), row.batch(), row.module(), row.attachFile(), row.content()));
        }
        return result;
    }
}
