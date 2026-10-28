package com.cranesvarsity.template.dao;

import com.cranesvarsity.template.util.Marks;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * The batch's curriculum — which modules a student is actually scored against.
 *
 * The report card used to take its rows from {@code attendance}, which is a
 * record of classes held, not of the syllabus. That let modules with no marks
 * appear as 0/0 rows and let the denominator drift from student to student.
 * The Performance Master Sheet derives the list from the batch's mapping
 * instead, and this DAO is the same query.
 */
@Repository
public class BatchModuleDao {

    private final JdbcTemplate jdbcTemplate;

    public BatchModuleDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * A batch's vertical/domain/vertical-type triple. Modules are mapped to a
     * batch by id AND must match all three, because {@code map_batch_modules}
     * holds rows that the triple rules out.
     */
    public record BatchDims(int id, String vertical, String domain, String verticalType) {
    }

    public Optional<BatchDims> findDims(String batch) {
        if (batch == null || batch.isBlank()) {
            return Optional.empty();
        }
        String sql = "SELECT id, batch_vertical, batch_domain, batch_vertical_type " +
                "FROM cranescrm.generatedbatches WHERE TRIM(batch) = ? LIMIT 1";
        List<BatchDims> rows = jdbcTemplate.query(sql, (rs, rowNum) -> new BatchDims(
                rs.getInt("id"),
                rs.getString("batch_vertical"),
                rs.getString("batch_domain"),
                rs.getString("batch_vertical_type")
        ), batch.trim());
        return rows.isEmpty() ? Optional.empty() : Optional.of(rows.get(0));
    }

    /**
     * Module short names mapped to this batch, in catalogue order.
     *
     * DISTINCT because a batch can carry the same module mapped twice; ordering
     * by {@code modules.id} keeps the report card's row order the same as the
     * Performance Master Sheet's column order.
     */
    public List<String> mappedModules(BatchDims dims) {
        String sql = "SELECT DISTINCT m.id, TRIM(m.module_short_name) AS short_name " +
                "FROM cranescrm.map_batch_modules mb " +
                "JOIN cranescrm.modules m ON m.id = mb.module_id " +
                "WHERE mb.batch_id = ? " +
                "  AND LOWER(TRIM(m.spt_map)) = 'yes' " +
                "  AND LOWER(TRIM(m.module_vertical)) = LOWER(TRIM(?)) " +
                "  AND LOWER(TRIM(m.module_domain)) = LOWER(TRIM(?)) " +
                "  AND LOWER(TRIM(m.module_vertical_type)) = LOWER(TRIM(?)) " +
                "  AND m.module_short_name IS NOT NULL AND TRIM(m.module_short_name) <> '' " +
                "ORDER BY m.id";

        List<String> out = new ArrayList<>();
        jdbcTemplate.query(sql, rs -> {
            String name = rs.getString("short_name");
            if (!Marks.key(name).isEmpty()) {
                out.add(name.trim());
            }
        }, dims.id(), dims.vertical(), dims.domain(), dims.verticalType());
        return out;
    }
}
