package com.cranesvarsity.template.dao;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Raw SQL against the legacy "books" table (schema cranescrm) — backs Course
 * Outline (contenttype='Course Outline'), Lab Manual (contenttype='Lab Manual'),
 * and Study Materials (contenttype='materials'), same table/shape, different
 * filter, copied from course-and-modules.jsp / view-lab-manual.jsp / materials.jsp.
 */
@Repository
public class BooksDao {

    private final JdbcTemplate jdbcTemplate;

    public BooksDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public record BookRow(int id, String moduleName, String content) {}

    public List<BookRow> findByCourseAndType(String course, String contentType) {
        String sql = "SELECT id, content, modulename FROM books WHERE course = ? AND contenttype = ?";
        return jdbcTemplate.query(sql, (rs, rowNum) ->
                new BookRow(rs.getInt("id"), rs.getString("modulename"), rs.getString("content")), course, contentType);
    }

    public java.util.Optional<BookRow> findByIdAndCourseAndType(int id, String course, String contentType) {
        String sql = "SELECT id, content, modulename FROM books WHERE id = ? AND course = ? AND contenttype = ?";
        List<BookRow> rows = jdbcTemplate.query(sql, (rs, rowNum) ->
                new BookRow(rs.getInt("id"), rs.getString("modulename"), rs.getString("content")), id, course, contentType);
        return rows.stream().findFirst();
    }
}
