package com.cranesvarsity.template.dao;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Raw SQL against "exam_system.sampletestquestions" — backs Practice MCQ Tests
 * listing. Legacy (mcq-practice-test.jsp) builds this via nested per-module/
 * per-chapter loops issuing one COUNT query each; consolidated here into a
 * single GROUP BY query with the same result shape.
 */
@Repository
public class SampleTestQuestionsDao {

    private final JdbcTemplate jdbcTemplate;

    public SampleTestQuestionsDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public record ModuleChapterCount(String moduleName, String chapter, int questionsCount) {}

    public List<ModuleChapterCount> findModuleChapterCounts() {
        String sql = "SELECT modules, chapter, COUNT(*) as cnt FROM exam_system.sampletestquestions " +
                "GROUP BY modules, chapter ORDER BY modules, chapter";
        return jdbcTemplate.query(sql, (rs, rowNum) -> new ModuleChapterCount(
                rs.getString("modules"), rs.getString("chapter"), rs.getInt("cnt")
        ));
    }
}
