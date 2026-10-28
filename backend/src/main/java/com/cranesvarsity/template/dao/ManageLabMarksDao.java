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

/** Raw SQL against "exam_system.manage_lab_marks". */
@Repository
public class ManageLabMarksDao {

    private final JdbcTemplate jdbcTemplate;

    public ManageLabMarksDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /** Modules this batch has conducted a lab test for, for any student. */
    public Set<String> modulesConductedForBatch(String batch) {
        String sql = "SELECT DISTINCT TRIM(lab_module) AS m FROM exam_system.manage_lab_marks WHERE TRIM(batch) = ?";
        Set<String> out = new HashSet<>();
        jdbcTemplate.query(sql, rs -> {
            String k = Marks.key(rs.getString("m"));
            if (!k.isEmpty()) {
                out.add(k);
            }
        }, batch);
        return out;
    }

    /** This student's lab mark per module — newest row wins. See ManageTheoryMarksDao#latestObtainedByModule. */
    public Map<String, BigDecimal> latestObtainedByModule(String regNo) {
        String sql = "SELECT TRIM(lab_module) AS m, obtained_marks AS o FROM exam_system.manage_lab_marks " +
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

    /** Every module — for the Skill Tracker's Lab Results tab. Copied faithfully from student-performance.jsp. */
    public List<SkillTrackerMarksRow> findAllByRegNo(String regNo) {
        String sql = "SELECT lab_module, total_marks, obtained_marks, remarks " +
                "FROM exam_system.manage_lab_marks WHERE registration_no = ?";

        return jdbcTemplate.query(sql, (rs, rowNum) -> new SkillTrackerMarksRow(
                rs.getString("lab_module"),
                rs.getInt("total_marks"),
                rs.getInt("obtained_marks"),
                rs.getString("remarks")
        ), regNo);
    }
}
