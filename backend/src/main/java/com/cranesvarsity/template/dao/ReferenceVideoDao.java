package com.cranesvarsity.template.dao;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Raw SQL against "manage_reference_videos" (schema cranescrm) — copied verbatim
 * from reference-video-modules.jsp / manage-reference-videos.jsp. The module list
 * is intentionally unfiltered by course/batch, matching legacy behavior exactly.
 */
@Repository
public class ReferenceVideoDao {

    private final JdbcTemplate jdbcTemplate;

    public ReferenceVideoDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public record VideoRow(int id, String title, String link, String duration) {}

    public List<String> findDistinctModules() {
        String sql = "SELECT DISTINCT module_name FROM manage_reference_videos";
        return jdbcTemplate.queryForList(sql, String.class);
    }

    public List<VideoRow> findByModule(String moduleName) {
        String sql = "SELECT id, vdo_title, vdo_link, duration FROM manage_reference_videos " +
                "WHERE module_name = ? ORDER BY module_name, id";
        return jdbcTemplate.query(sql, (rs, rowNum) -> new VideoRow(
                rs.getInt("id"),
                rs.getString("vdo_title"),
                rs.getString("vdo_link"),
                rs.getString("duration")
        ), moduleName);
    }
}
