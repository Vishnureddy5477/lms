package com.cranesvarsity.template.dao;

import com.cranesvarsity.template.dto.DocumentItem;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Raw SQL against "placement.student_document" — schema placement, reached
 * via a fully-qualified table name. Read query copied from
 * manage-documents.jsp; insert/delete inferred from that same page's column
 * usage (the legacy upload servlet itself, /student/UploadDocument.jsp, was
 * not present in the checked-out source to copy verbatim).
 */
@Repository
public class StudentDocumentDao {

    private final JdbcTemplate jdbcTemplate;

    public StudentDocumentDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<DocumentItem> findByRegNo(String regNo) {
        String sql = "SELECT id, email, doctype, doc_link FROM placement.student_document " +
                "WHERE regno = ? AND is_delete = 'No'";
        return jdbcTemplate.query(sql, (rs, rowNum) -> new DocumentItem(
                rs.getInt("id"), rs.getString("email"), rs.getString("doctype"), rs.getString("doc_link")
        ), regNo);
    }

    public void insert(String regNo, String email, String docType, String docLink) {
        String sql = "INSERT INTO placement.student_document (regno, email, doctype, doc_link, is_delete) " +
                "VALUES (?, ?, ?, ?, 'No')";
        jdbcTemplate.update(sql, regNo, email, docType, docLink);
    }

    public Optional<String> findDocLinkByIdAndRegNo(int id, String regNo) {
        String sql = "SELECT doc_link FROM placement.student_document WHERE id = ? AND regno = ?";
        List<String> rows = jdbcTemplate.query(sql, (rs, rowNum) -> rs.getString("doc_link"), id, regNo);
        return rows.isEmpty() ? Optional.empty() : Optional.ofNullable(rows.get(0));
    }

    public void deleteById(int id) {
        jdbcTemplate.update("DELETE FROM placement.student_document WHERE id = ?", id);
    }
}
