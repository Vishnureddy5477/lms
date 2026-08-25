package com.cranesvarsity.template.service;

import com.cranesvarsity.template.dao.OnlineScheduleDao;
import com.cranesvarsity.template.dao.TrainerLoginLogoutDao;
import com.cranesvarsity.template.dto.ScheduleResponse;
import com.cranesvarsity.template.dto.TodayClassItem;
import com.cranesvarsity.template.dto.WeekClassItem;
import com.cranesvarsity.template.model.cranescrm.Admission;
import com.cranesvarsity.template.repository.AdmissionRepository;
import com.cranesvarsity.template.security.AuthenticatedStudent;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Backs the "My Schedule" page — successor of legacy my-schedule.jsp.
 * Status derivation (Not Started / Live Now / Completed / Scheduled) copied
 * faithfully: it depends on whether/when the trainer actually logged in
 * today (trainerloginlogut), not just the nominal onlineschedule times.
 */
@Service
public class ScheduleService {

    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm:ss");

    private final AdmissionRepository admissionRepository;
    private final OnlineScheduleDao onlineScheduleDao;
    private final TrainerLoginLogoutDao trainerLoginLogoutDao;

    public ScheduleService(AdmissionRepository admissionRepository,
                            OnlineScheduleDao onlineScheduleDao,
                            TrainerLoginLogoutDao trainerLoginLogoutDao) {
        this.admissionRepository = admissionRepository;
        this.onlineScheduleDao = onlineScheduleDao;
        this.trainerLoginLogoutDao = trainerLoginLogoutDao;
    }

    public ScheduleResponse getSchedule(AuthenticatedStudent student) {
        Admission admission = admissionRepository.findById(student.regNo())
                .orElseThrow(() -> new IllegalStateException("Student record not found for registration_no=" + student.regNo()));
        String batch = admission.getBatchno();

        List<TodayClassItem> todayClasses = new ArrayList<>();
        for (OnlineScheduleDao.TodayRow row : onlineScheduleDao.findToday(batch)) {
            todayClasses.add(toTodayItem(row, batch));
        }

        List<WeekClassItem> weekClasses = new ArrayList<>();
        String currentDate = LocalDate.now().toString();
        for (OnlineScheduleDao.WeekRow row : onlineScheduleDao.findWeek(batch)) {
            String status = row.endDate() != null && row.endDate().compareTo(currentDate) > 0 ? "Scheduled" : "Completed";
            weekClasses.add(new WeekClassItem(
                    row.module(), row.trainer(), row.startTime(), row.endTime(),
                    row.startDate(), row.endDate(), row.mode(), status
            ));
        }

        return new ScheduleResponse(todayClasses, weekClasses);
    }

    private TodayClassItem toTodayItem(OnlineScheduleDao.TodayRow row, String batch) {
        Optional<TrainerLoginLogoutDao.LoginWindow> window =
                trainerLoginLogoutDao.findTodayWindow(row.trainer(), row.module(), batch);

        String status;
        boolean linkActive;

        if (window.isEmpty()) {
            status = "Not Started";
            linkActive = true;
        } else {
            LocalTime now = LocalTime.now();
            LocalTime start = parseTime(window.get().startTime());
            LocalTime end = parseTime(window.get().endTime());

            if (start != null && end != null && !now.isBefore(start) && now.isBefore(end)) {
                status = "Live Now";
                linkActive = true;
            } else if (end != null && now.isAfter(end)) {
                status = "Completed";
                linkActive = false;
            } else {
                status = "Scheduled";
                linkActive = true;
            }
        }

        return new TodayClassItem(row.module(), row.trainer(), row.startTime(), row.endTime(),
                status, linkActive, row.scheduleLink());
    }

    private LocalTime parseTime(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return LocalTime.parse(value, TIME_FORMAT);
        } catch (Exception e) {
            return null;
        }
    }
}
