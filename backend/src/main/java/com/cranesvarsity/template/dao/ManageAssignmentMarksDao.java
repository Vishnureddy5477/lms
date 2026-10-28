package com.cranesvarsity.template.dao;

import com.cranesvarsity.template.util.Marks;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Raw SQL against "exam_system.manage_assignment_marks".
 *
 * The old report-card queries here were restricted to assignment_title =
 * 'Assignment 1', inherited from mark-card.jsp. Nothing else in the business
 * treats later assignments as not counting — the Performance Master Sheet takes
 * a student's newest assignment row for a module whatever its title — so the
 * filter is gone. It was hiding real marks: students routinely carry rows
 * titled Assignment 2, 3 and 4.
 */
@Repository
public class ManageAssignmentMarksDao {

    private final JdbcTemplate jdbcTemplate;

    public ManageAssignmentMarksDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /** Modules this batch has conducted an assignment for, for any student. */
    public Set<String> modulesConductedForBatch(String batch) {
        String sql = "SELECT DISTINCT TRIM(assignment_module) AS m FROM exam_system.manage_assignment_marks " +
                "WHERE TRIM(batch) = ?";
        Set<String> out = new HashSet<>();
        jdbcTemplate.query(sql, rs -> {
            String k = Marks.key(rs.getString("m"));
            if (!k.isEmpty()) {
                out.add(k);
            }
        }, batch);
        return out;
    }

    /** This student's assignment mark per module — newest row wins. See ManageTheoryMarksDao#latestObtainedByModule. */
    public Map<String, BigDecimal> latestObtainedByModule(String regNo) {
        String sql = "SELECT TRIM(assignment_module) AS m, obtained_marks AS o " +
                "FROM exam_system.manage_assignment_marks WHERE TRIM(registration_no) = ? ORDER BY id DESC";
        Map<String, BigDecimal> out = new HashMap<>();
        jdbcTemplate.query(sql, rs -> {
            String k = Marks.key(rs.getString("m"));
            if (!k.isEmpty()) {
                out.putIfAbsent(k, Marks.value(rs.getString("o")));
            }
        }, regNo);
        return out;
    }
}
