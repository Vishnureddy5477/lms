package com.cranesvarsity.template.dao;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The per-question answer log — "exam_system.module_test_answer".
 *
 * Replaces the write path into the legacy moduletestanswer, which stored the
 * full question longtext, the correct-answer longtext and three varchar(500)
 * columns on EVERY row: 1.55M rows / 344MB, nearly all of it re-derivable by a
 * join. The same information lives here in roughly 20 bytes a row.
 *
 * The old table is neither renamed nor rewritten — the live JSP portal still
 * writes to it, and exam_system.v_module_test_answer unions old and new so the
 * existing reports keep working untouched.
 *
 * Keyed by (session_id, question_order): the SLOT, not the question. That is
 * what lets a backfilled duplicate question occupy two slots and be answered
 * independently — and it also makes the duplicate rows the legacy scoring had
 * to defend against ("count only the latest row per question") structurally
 * impossible rather than merely filtered out.
 */
@Repository
public class ModuleTestAnswerLogDao {

    private final JdbcTemplate jdbcTemplate;

    public ModuleTestAnswerLogDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /** One answer to persist. {@code correct} is decided server-side by the caller. */
    public record AnswerRow(int questionOrder, int questionId, int selectedOption, boolean correct) {}

    public record Tally(int correct, int incorrect) {}

    /**
     * Upsert a whole debounced batch in a single round trip.
     *
     * Re-answering a slot updates it in place rather than adding a row, so the
     * student can change their mind as often as they like without the answer
     * log growing or the score skewing.
     */
    public void upsertBatch(int sessionId, List<AnswerRow> rows) {
        if (rows.isEmpty()) {
            return;
        }

        StringBuilder sql = new StringBuilder(
                "INSERT INTO exam_system.module_test_answer " +
                "(session_id, question_order, question_id, selected_option, is_correct) VALUES ");
        List<Object> params = new ArrayList<>(rows.size() * 5);

        for (int i = 0; i < rows.size(); i++) {
            sql.append(i == 0 ? "(?,?,?,?,?)" : ",(?,?,?,?,?)");
            AnswerRow r = rows.get(i);
            params.add(sessionId);
            params.add(r.questionOrder());
            params.add(r.questionId());
            params.add(r.selectedOption());
            params.add(r.correct() ? 1 : 0);
        }

        sql.append(" ON DUPLICATE KEY UPDATE ")
           .append("question_id = VALUES(question_id), ")
           .append("selected_option = VALUES(selected_option), ")
           .append("is_correct = VALUES(is_correct)");

        jdbcTemplate.update(sql.toString(), params.toArray());
    }

    /** Saved answers for a resume, as slot -> selected option. */
    public Map<Integer, Integer> findSaved(int sessionId) {
        Map<Integer, Integer> saved = new HashMap<>();
        jdbcTemplate.query(
                "SELECT question_order, selected_option FROM exam_system.module_test_answer WHERE session_id = ?",
                rs -> { saved.put(rs.getInt("question_order"), rs.getInt("selected_option")); },
                sessionId);
        return saved;
    }

    /**
     * Score this attempt.
     *
     * Only correct/incorrect are counted here. "Not attempted" is derived by the
     * service as (total slots - correct - incorrect), which correctly covers both
     * a slot explicitly cleared back to 0 AND a slot the student never opened at
     * all — the legacy panel had to write a NOT_ATTEMPTED row on every navigation
     * just to make that number come out right.
     */
    public Tally tally(int sessionId) {
        String sql = "SELECT " +
                "COALESCE(SUM(is_correct = 1), 0) AS correct, " +
                "COALESCE(SUM(selected_option <> 0 AND is_correct = 0), 0) AS incorrect " +
                "FROM exam_system.module_test_answer WHERE session_id = ?";

        return jdbcTemplate.queryForObject(sql, (rs, rowNum) ->
                new Tally(rs.getInt("correct"), rs.getInt("incorrect")), sessionId);
    }
}
