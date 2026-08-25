package com.cranesvarsity.template.dao;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Raw SQL against "manage_course_outline_content" (schema cranescrm) — the
 * B2B/corporate variant of the course outline, keyed by batch instead of
 * course. Copied from course-and-modules-outline-b2b.jsp.
 */
@Repository
public class CourseOutlineContentDao {

    private final JdbcTemplate jdbcTemplate;

    public CourseOutlineContentDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public record OutlineRow(String moduleName, String content) {}

    public List<OutlineRow> findByBatch(String batch) {
        String sql = "SELECT id, course_name, content_file_link FROM manage_course_outline_content WHERE batchno = ?";
        return jdbcTemplate.query(sql, (rs, rowNum) ->
                new OutlineRow(rs.getString("course_name"), rs.getString("content_file_link")), batch);
    }
}
