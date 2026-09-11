package com.cranesvarsity.template.repository;

import com.cranesvarsity.template.model.cranescrm.Admission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface AdmissionRepository extends JpaRepository<Admission, String> {

    /**
     * Every active admission row for this email, newest registration first.
     *
     * Deliberately a List, not an Optional: one person can hold more than one
     * active admission (finish a diploma, then join an internship), and the
     * production table has ~246 such emails. Declaring this Optional made
     * Hibernate throw NonUniqueResultException the moment one of those students
     * tried to log in, which surfaced as a 500 and locked them out entirely —
     * while the legacy JSP, which just read the first row of its ResultSet, let
     * them straight in. Callers pick the row they mean; see AuthService.
     */
    @Query("SELECT a FROM Admission a WHERE a.email = :email AND a.dropout <> 'yes' " +
            "ORDER BY a.registrationDate DESC, a.registrationNo DESC")
    List<Admission> findAllActiveByEmail(@Param("email") String email);

    /**
     * Registration is valid if it was created less than a year ago.
     * Same age-comparison formula as legacy login.jsp, just filtered by
     * email/password directly in SQL instead of looping every row in Java.
     */
    @Query(value = "SELECT COUNT(*) FROM admission " +
            "WHERE email = :email AND password = :password AND dropout <> 'yes' " +
            "AND (YEAR(CURDATE()) - YEAR(registration_date) - " +
            "(DATE_FORMAT(CURDATE(), '%m%d') < DATE_FORMAT(registration_date, '%m%d'))) < 1",
            nativeQuery = true)
    long countValidRegistration(@Param("email") String email, @Param("password") String password);

    @Query(value = "SELECT COUNT(*) FROM admission " +
            "WHERE email = :email AND password = :password AND dropout <> 'yes' " +
            "AND activities = 'On Resume' AND CURDATE() <= DATE(upto)",
            nativeQuery = true)
    long countOnResumeValid(@Param("email") String email, @Param("password") String password);

    @Query(value = "SELECT COUNT(*) FROM admission WHERE registration_no = :regNo AND activities = 'Drop Out'",
            nativeQuery = true)
    long countCourseCompleted(@Param("regNo") String regNo);

    /**
     * Parameterized version of the sync UPDATE that legacy EnrollmentFormBackUp builds via raw
     * string concatenation (a SQL-injection hole there) after an enrollment form is saved.
     * Same fields/values, just safely bound.
     */
    @Modifying
    @Transactional
    @Query(value = "UPDATE admission SET stname = :stname, email = :email, address = :address, " +
            "btech_marks = :btechMarks, byop = :byop, diploma = :diploma, dyop = :dyop, " +
            "puc = :puc, pucyop = :pucyop, tenth_marks = :tenthMarks, tyop = :tyop, " +
            "placement = :placement, enrollmentform = 'Yes' WHERE registration_no = :regNo",
            nativeQuery = true)
    void syncFromEnrollment(@Param("regNo") String regNo,
                             @Param("stname") String stname,
                             @Param("email") String email,
                             @Param("address") String address,
                             @Param("btechMarks") String btechMarks,
                             @Param("byop") String byop,
                             @Param("diploma") String diploma,
                             @Param("dyop") String dyop,
                             @Param("puc") String puc,
                             @Param("pucyop") String pucyop,
                             @Param("tenthMarks") String tenthMarks,
                             @Param("tyop") String tyop,
                             @Param("placement") String placement);

    /**
     * Fallback module list (comma-separated) used by Course Outline / Lab Manual when no row
     * exists yet in books/manage_course_outline_content for this student's course/batch.
     */
    @Query(value = "SELECT modules FROM admission WHERE registration_no = :regNo", nativeQuery = true)
    String findModulesCsv(@Param("regNo") String regNo);

    /** Copied from change-password.jsp — plaintext update, same dropout guard as the rest of admission writes. */
    @Modifying
    @Transactional
    @Query(value = "UPDATE admission SET password = :newPassword WHERE registration_no = :regNo AND dropout <> 'yes'",
            nativeQuery = true)
    int updatePassword(@Param("regNo") String regNo, @Param("newPassword") String newPassword);

    /** Copied from login.jsp's forgot-password OTP flow — same email-keyed update + dropout guard. */
    @Modifying
    @Transactional
    @Query(value = "UPDATE admission SET password = :newPassword WHERE email = :email AND dropout <> 'yes'",
            nativeQuery = true)
    int updatePasswordByEmail(@Param("email") String email, @Param("newPassword") String newPassword);
}
