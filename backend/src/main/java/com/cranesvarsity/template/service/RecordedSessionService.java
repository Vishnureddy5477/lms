package com.cranesvarsity.template.service;

import com.cranesvarsity.template.dao.RecordedSessionDao;
import com.cranesvarsity.template.dto.RecordedSessionItem;
import com.cranesvarsity.template.model.cranescrm.Admission;
import com.cranesvarsity.template.repository.AdmissionRepository;
import com.cranesvarsity.template.security.AuthenticatedStudent;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * Ported from my-recorded-sessions.jsp: per-batch recorded session list, plus a
 * legacy hardcoded special case that appends 3 fixed S3 video rows (tagged "Archive")
 * for students on the "PG Diploma In Full Stack Java Development" course.
 */
@Service
public class RecordedSessionService {

    private static final String JAVA_COURSE = "PG Diploma In Full Stack Java Development";

    private final RecordedSessionDao recordedSessionDao;
    private final AdmissionRepository admissionRepository;

    public RecordedSessionService(RecordedSessionDao recordedSessionDao, AdmissionRepository admissionRepository) {
        this.recordedSessionDao = recordedSessionDao;
        this.admissionRepository = admissionRepository;
    }

    public List<RecordedSessionItem> getRecordedSessions(AuthenticatedStudent student) {
        List<RecordedSessionItem> result = new ArrayList<>();
        int slNo = 1;

        for (RecordedSessionDao.Row row : recordedSessionDao.findByBatch(student.batch())) {
            result.add(new RecordedSessionItem(slNo++, row.batch(), row.subject(), row.recordedLink(), row.linkDate(), "Available"));
        }

        Admission admission = admissionRepository.findActiveByEmail(student.email()).orElse(null);
        if (admission != null && JAVA_COURSE.equalsIgnoreCase(admission.getCourse())) {
            result.add(new RecordedSessionItem(slNo++, student.batch(), "Java SE 9 - Path Setting",
                    "https://cranesawsfile.s3.us-east-2.amazonaws.com/javarecordedvidoe/Day2_2021-07-14.mp4",
                    "14_07_2021", "Archive"));
            result.add(new RecordedSessionItem(slNo++, student.batch(), "Java SE 9 - First Program",
                    "https://cranesawsfile.s3.us-east-2.amazonaws.com/javarecordedvidoe/Day3_2021-07-15.mp4",
                    "15_07_2021", "Archive"));
            result.add(new RecordedSessionItem(slNo++, student.batch(), "JDK, JVM, JRE & Variables",
                    "https://cranesawsfile.s3.us-east-2.amazonaws.com/javarecordedvidoe/day4_2021-07-16.mp4",
                    "16_07_2021", "Archive"));
        }

        return result;
    }
}
