package com.cranesvarsity.template.dao;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.Date;

/**
 * Raw SQL against "placement.placementcodeofconduct" — copied from
 * post-placement-code-of-conduct.jsp, including its one-submission-only gate
 * and "NA" placeholders for the branch of the form that wasn't filled in.
 */
@Repository
public class PlacementCodeOfConductDao {

    private final JdbcTemplate jdbcTemplate;

    public PlacementCodeOfConductDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public boolean exists(String regNo) {
        String sql = "SELECT COUNT(*) FROM placement.placementcodeofconduct WHERE regno = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, regNo);
        return count != null && count > 0;
    }

    public void insert(String regNo, String email, String placementAssistance, String placementAssistanceMessage,
                        String projectExperience, String projectExperienceMessage, String acceptance, String signature) {
        String sql = "INSERT INTO placement.placementcodeofconduct " +
                "(regno, email, placementassistance, placementassistancemessage, projectexperience, " +
                "projectexperiencemsg, acceptance, acceptancedate, signature) VALUES (?,?,?,?,?,?,?,?,?)";
        jdbcTemplate.update(sql, regNo, email, placementAssistance, placementAssistanceMessage,
                projectExperience, projectExperienceMessage, acceptance,
                new Date(System.currentTimeMillis()).toString(), signature);
    }
}
