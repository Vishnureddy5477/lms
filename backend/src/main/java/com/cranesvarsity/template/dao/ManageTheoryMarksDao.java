package com.cranesvarsity.template.dao;

import com.cranesvarsity.template.dto.SkillTrackerMarksRow;
import com.cranesvarsity.template.util.Marks;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** Raw SQL against "manage_theory_marks" (schema cranescrm). */
@Repository
public class ManageTheoryMarksDao {

    private final JdbcTemplate jdbcTemplate;

    public ManageTheoryMarksDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /** Modules this batch has conducted a theory test for, for any student. */
    public Set<String> modulesConductedForBatch(String batch) {
        String sql = "SELECT DISTINCT TRIM(theory_module) AS m FROM manage_theory_marks WHERE TRIM(batch) = ?";
        Set<String> out = new HashSet<>();
        jdbcTemplate.query(sql, rs -> {
            String k = Marks.key(rs.getString("m"));
            if (!k.isEmpty()) {
                out.add(k);
            }
        }, batch);
        return out;
    }

    /**
     * This student's theory mark per module — newest row wins.
     *
     * 292 student/module pairs here have duplicate rows with DIFFERENT marks.
     * The report card used to take whichever row MySQL happened to return
     * first, with no ORDER BY, so the mark shown could change between page
     * loads. ORDER BY id DESC + putIfAbsent takes the newest, which is what the
     * Performance Master Sheet does and what a re-entered mark is meant to be.
     *
     * Not filtered by batch, also matching the Performance Master Sheet: a
     * student who sat a module under an earlier batch number still earned that
     * mark, and whether the module counts is decided by the batch's conducted
     * set, not by where the mark happens to be filed.
     */
    public Map<String, BigDecimal> latestObtainedByModule(String regNo) {
        String sql = "SELECT TRIM(theory_module) AS m, obtained_marks AS o FROM manage_theory_marks " +
                "WHERE TRIM(registration_no) = ? ORDER BY id DESC";
        Map<String, BigDecimal> out = new HashMap<>();
        jdbcTemplate.query(sql, rs -> {
            String k = Marks.key(rs.getString("m"));
            if (!k.isEmpty()) {
                out.putIfAbsent(k, Marks.value(rs.getString("o")));
            }
        }, regNo);
        return out;
    }

    /** Every module — for the Skill Tracker's Theory Results tab. Copied faithfully from student-performance.jsp. */
    public List<SkillTrackerMarksRow> findAllByRegNo(String regNo) {
        String sql = "SELECT theory_module, total_marks, obtained_marks, remarks " +
                "FROM manage_theory_marks WHERE registration_no = ?";

        return jdbcTemplate.query(sql, (rs, rowNum) -> new SkillTrackerMarksRow(
                rs.getString("theory_module"),
                rs.getInt("total_marks"),
                rs.getInt("obtained_marks"),
                rs.getString("remarks")
        ), regNo);
    }
}
