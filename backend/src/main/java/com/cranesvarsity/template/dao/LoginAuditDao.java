package com.cranesvarsity.template.dao;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Audit trail inserts against "exam_system.studentloginstatus" (successful
 * logins) and "exam_system.studentloginstatus_copy" (failed attempts) — a
 * different legacy schema on the same MySQL instance/user, reached via a
 * fully-qualified table name. Mirrors login.jsp's logging, except the failed-
 * login audit deliberately does NOT persist the submitted plaintext password
 * (legacy studentloginstatus_copy.password column is left unpopulated).
 */
@Repository
public class LoginAuditDao {

    private final JdbcTemplate jdbcTemplate;

    public LoginAuditDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void logSuccess(String regNo, String email, HttpServletRequest request) {
        String sql = "INSERT INTO exam_system.studentloginstatus " +
                "(regno, logintype, loginstatus, logindate, logintime, email, address, host, connectionip, realip, browser, lms_v_2) " +
                "VALUES (?,?,?,?,?,?,?,?,?,?,?,?)";
        jdbcTemplate.update(sql,
                regNo,
                "web",
                "Loged In",
                LocalDate.now().toString(),
                LocalTime.now().withNano(0).toString(),
                email,
                extractClientIp(request),
                request.getHeader("host"),
                request.getHeader("cf-connecting-ip"),
                request.getHeader("x-real-ip"),
                request.getHeader("user-agent"),
                "Yes"
        );
    }

    public void logFailure(String regNo, String email, String loginStatus) {
        String sql = "INSERT INTO exam_system.studentloginstatus_copy " +
                "(regno, logintype, loginstatus, logindate, logintime, email) VALUES (?,?,?,?,?,?)";
        jdbcTemplate.update(sql,
                regNo != null ? regNo : "",
                "web",
                loginStatus,
                LocalDate.now().toString(),
                LocalTime.now().withNano(0).toString(),
                email
        );
    }

    private String extractClientIp(HttpServletRequest request) {
        String ip = request.getHeader("x-forwarded-for");
        if (isBlankOrUnknown(ip)) ip = request.getHeader("Proxy-Client-IP");
        if (isBlankOrUnknown(ip)) ip = request.getHeader("WL-Proxy-Client-IP");
        if (isBlankOrUnknown(ip)) ip = request.getRemoteAddr();
        return ip;
    }

    private boolean isBlankOrUnknown(String value) {
        return value == null || value.isEmpty() || "unknown".equalsIgnoreCase(value);
    }
}
