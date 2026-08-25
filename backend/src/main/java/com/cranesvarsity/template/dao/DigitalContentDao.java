package com.cranesvarsity.template.dao;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Raw SQL against manage_dl_modules / manage_dl_categories / manage_dl_topics /
 * manage_dl_subtopics / manage_dl_videos (schema cranescrm) — copied verbatim
 * from view-content.jsp / get-content.jsp.
 */
@Repository
public class DigitalContentDao {

    private final JdbcTemplate jdbcTemplate;

    public DigitalContentDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public record ModuleRow(int moduleId, String moduleName, int seqNo) {}
    public record TopicRow(int topicId, String topicName) {}
    public record SubtopicRow(int subtopicId, String subtopicName) {}
    public record VideoRow(int id, String name, String link) {}
    public record Counts(int totalTopics, int totalSubtopics, int totalVideos) {}

    public List<ModuleRow> findModulesByToken(String token) {
        String sql = "SELECT t1.module_id, t1.module_name, t1.seq_no " +
                "FROM manage_dl_modules t1 " +
                "JOIN manage_dl_categories t2 ON t1.cat_id = t2.cat_id " +
                "WHERE t1.is_active = 'Yes' AND t1.is_delete = 'No' AND t2.token = ? " +
                "ORDER BY t1.seq_no ASC";
        return jdbcTemplate.query(sql, (rs, rowNum) -> new ModuleRow(
                rs.getInt("module_id"), trim(rs.getString("module_name")), rs.getInt("seq_no")
        ), token);
    }

    public Optional<ModuleRow> findModuleById(int moduleId) {
        String sql = "SELECT module_id, module_name, seq_no FROM manage_dl_modules WHERE module_id = ?";
        return jdbcTemplate.query(sql, (rs, rowNum) -> new ModuleRow(
                rs.getInt("module_id"), rs.getString("module_name"), rs.getInt("seq_no")
        ), moduleId).stream().findFirst();
    }

    public Counts findCounts(int moduleId) {
        String sql = "SELECT " +
                "(SELECT COUNT(*) FROM manage_dl_topics WHERE module_id = m.module_id AND is_active='Yes' AND is_delete='No') AS total_topics, " +
                "(SELECT COUNT(*) FROM manage_dl_subtopics WHERE topic_id IN (SELECT topic_id FROM manage_dl_topics WHERE module_id = m.module_id AND is_active='Yes' AND is_delete='No') AND is_active='Yes' AND is_delete='No') AS total_subtopics, " +
                "(SELECT COUNT(*) FROM manage_dl_videos WHERE subtopic_id IN (SELECT subtopic_id FROM manage_dl_subtopics WHERE topic_id IN (SELECT topic_id FROM manage_dl_topics WHERE module_id = m.module_id AND is_active='Yes' AND is_delete='No') AND is_active='Yes' AND is_delete='No') AND is_active='Yes' AND is_delete='No') AS total_videos " +
                "FROM manage_dl_modules m WHERE m.module_id = ?";
        List<Counts> rows = jdbcTemplate.query(sql, (rs, rowNum) -> new Counts(
                rs.getInt("total_topics"), rs.getInt("total_subtopics"), rs.getInt("total_videos")
        ), moduleId);
        return rows.stream().findFirst().orElse(new Counts(0, 0, 0));
    }

    public List<TopicRow> findTopics(int moduleId) {
        String sql = "SELECT topic_id, topic_name FROM manage_dl_topics WHERE module_id = ? AND is_active = 'Yes' AND is_delete = 'No'";
        return jdbcTemplate.query(sql, (rs, rowNum) -> new TopicRow(
                rs.getInt("topic_id"), trim(rs.getString("topic_name"))
        ), moduleId);
    }

    public List<SubtopicRow> findSubtopics(int topicId) {
        String sql = "SELECT subtopic_id, subtopic_name FROM manage_dl_subtopics WHERE topic_id = ? AND is_active = 'Yes' AND is_delete = 'No'";
        return jdbcTemplate.query(sql, (rs, rowNum) -> new SubtopicRow(
                rs.getInt("subtopic_id"), trim(rs.getString("subtopic_name"))
        ), topicId);
    }

    public List<VideoRow> findVideos(int subtopicId) {
        String sql = "SELECT v_id, v_name, v_link FROM manage_dl_videos WHERE subtopic_id = ? AND is_active = 'Yes' AND is_delete = 'No'";
        return jdbcTemplate.query(sql, (rs, rowNum) -> new VideoRow(
                rs.getInt("v_id"), trim(rs.getString("v_name")), rs.getString("v_link")
        ), subtopicId);
    }

    /** Trims trailing/leading whitespace (incl. \r\n) seen in some production text columns — a data-quality artifact, not legacy behavior to preserve. */
    private static String trim(String value) {
        return value == null ? null : value.trim();
    }
}
