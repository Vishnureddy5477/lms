package com.cranesvarsity.template.dao;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

/**
 * Raw SQL against "student_tickets" (schema cranescrm). Two separate INSERTs,
 * matching contact-us.jsp exactly: file_name/file_link are NOT NULL columns
 * with a DB-side default, which only applies when the column is omitted from
 * the statement — binding an explicit NULL violates the constraint instead.
 */
@Repository
public class TicketDao {

    private final JdbcTemplate jdbcTemplate;

    public TicketDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void insert(String ticketId, String identityType, String regNo, String batchNo,
                        String subject, String description, String fileName, String fileLink) {
        if (fileName != null && fileLink != null) {
            String sql = "INSERT INTO student_tickets " +
                    "(ticket_id, identity_type, reg_no, batch_no, student_subject, description, current_status, file_name, file_link) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
            jdbcTemplate.update(sql, ticketId, identityType, regNo, batchNo, subject, description, "OPEN", fileName, fileLink);
        } else {
            String sql = "INSERT INTO student_tickets " +
                    "(ticket_id, identity_type, reg_no, batch_no, student_subject, description, current_status) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?)";
            jdbcTemplate.update(sql, ticketId, identityType, regNo, batchNo, subject, description, "OPEN");
        }
    }
}
