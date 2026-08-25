package com.cranesvarsity.template.dao;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Raw SQL against "placement.studentapplyjd" — a different legacy schema on
 * the same MySQL instance/user, reached via a fully-qualified table name.
 * Counts copied faithfully from index.jsp's JD stat cards (no join to
 * postjdtostudent is actually needed for these three numbers).
 */
@Repository
public class PlacementDao {

    private final JdbcTemplate jdbcTemplate;

    public PlacementDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public int countTotalOpportunities(String regNo) {
        String sql = "SELECT COUNT(*) FROM placement.studentapplyjd WHERE regno = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, regNo);
        return count != null ? count : 0;
    }

    public int countApplied(String regNo) {
        String sql = "SELECT COUNT(*) FROM placement.studentapplyjd WHERE isapplied = 'yes' AND regno = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, regNo);
        return count != null ? count : 0;
    }

    public record AppliedJobRow(String isApplied, String companyName, String driveMonth, String jobLocation,
                                 String skills, String domain, String ctc, String jobDescription, String postedDateTime) {}

    /** Every JD assigned to this student (applied or not) — copied from student-jd-details.jsp. */
    public List<AppliedJobRow> findAssignedJobs(String regNo) {
        String sql = "SELECT a.isapplied, p.companyname, p.drivemonth, p.joblocation, p.skills, p.domainname, " +
                "p.ctc, p.jobdescription, p.postdatetime " +
                "FROM placement.studentapplyjd a JOIN placement.postjdtostudent p ON a.jdpostedid = p.id " +
                "WHERE a.regno = ?";
        return jdbcTemplate.query(sql, (rs, rowNum) -> new AppliedJobRow(
                rs.getString("isapplied"), rs.getString("companyname"), rs.getString("drivemonth"),
                rs.getString("joblocation"), rs.getString("skills"), rs.getString("domainname"),
                rs.getString("ctc"), rs.getString("jobdescription"), rs.getString("postdatetime")
        ), regNo);
    }

    public record OpenJobRow(long id, String driveMonth, String companyName, String jobLocation, String skills,
                              String domain, String ctc, String jobDescription, String aboutCompany,
                              String postedDateTime, String interestStatus) {}

    /**
     * JDs posted in the last 72 hours that this student was assigned — copied from
     * company-current-jd-details.jsp, including its current interest decision (if any).
     */
    public List<OpenJobRow> findOpenJobs(String regNo) {
        String sql = "SELECT p.id, p.drivemonth, p.companyname, p.joblocation, p.skills, p.domainname, p.ctc, " +
                "p.jobdescription, p.aboutcompany, p.postdatetime, a.isapplied " +
                "FROM placement.postjdtostudent p " +
                "LEFT JOIN placement.studentapplyjd a ON a.jdpostedid = p.id AND a.regno = ? " +
                "WHERE p.approved_date >= DATE_SUB(NOW(), INTERVAL 72 HOUR) " +
                "AND p.id IN (SELECT jdpostedid FROM placement.studentapplyjd WHERE regno = ?) " +
                "ORDER BY p.id DESC";
        return jdbcTemplate.query(sql, (rs, rowNum) -> new OpenJobRow(
                rs.getLong("id"), rs.getString("drivemonth"), rs.getString("companyname"), rs.getString("joblocation"),
                rs.getString("skills"), rs.getString("domainname"), rs.getString("ctc"), rs.getString("jobdescription"),
                rs.getString("aboutcompany"), rs.getString("postdatetime"), rs.getString("isapplied")
        ), regNo, regNo);
    }

    /** Whether this student has already made a Yes/No interest decision on this JD. */
    public boolean hasDecided(String regNo, long jdPostedId) {
        String sql = "SELECT COUNT(*) FROM placement.studentapplyjd WHERE jdpostedid = ? AND regno = ? " +
                "AND (isapplied = 'Yes' OR isapplied = 'not_intrested')";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, jdPostedId, regNo);
        return count != null && count > 0;
    }

    public void updateInterest(String regNo, long jdPostedId, String status) {
        String sql = "UPDATE placement.studentapplyjd SET isapplied = ?, applieddate = NOW() " +
                "WHERE regno = ? AND jdpostedid = ?";
        jdbcTemplate.update(sql, status, regNo, jdPostedId);
    }
}
