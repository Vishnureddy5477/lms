package com.cranesvarsity.template.dao;

import com.cranesvarsity.template.dto.ModuleEvaluationRow;
import com.cranesvarsity.template.dto.PlacementEvaluationRow;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Raw SQL against "moduleevaluationcriteria" / "placementevaluationcriteria"
 * (schema cranescrm) — copied from evaluation-criterias.jsp. Both are global
 * reference tables (no WHERE clause in legacy either — same policy for
 * every student), not per-student data.
 */
@Repository
public class EvaluationCriteriaDao {

    private final JdbcTemplate jdbcTemplate;

    public EvaluationCriteriaDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<ModuleEvaluationRow> findModuleCriteria() {
        String sql = "SELECT types, modes, marks, total, passmarks, updatedon, updatedby FROM moduleevaluationcriteria";
        AtomicInteger slNo = new AtomicInteger(0);
        return jdbcTemplate.query(sql, (rs, rowNum) -> new ModuleEvaluationRow(
                slNo.incrementAndGet(),
                trim(rs.getString("types")),
                trim(rs.getString("modes")),
                trim(rs.getString("marks")),
                trim(rs.getString("total")),
                trim(rs.getString("passmarks")),
                trim(rs.getString("updatedon")),
                trim(rs.getString("updatedby"))
        ));
    }

    public List<PlacementEvaluationRow> findPlacementCriteria() {
        String sql = "SELECT types, testname, modes, marks, total, passmarks, updatedon, updatedby FROM placementevaluationcriteria";
        AtomicInteger slNo = new AtomicInteger(0);
        return jdbcTemplate.query(sql, (rs, rowNum) -> new PlacementEvaluationRow(
                slNo.incrementAndGet(),
                trim(rs.getString("types")),
                trim(rs.getString("testname")),
                trim(rs.getString("modes")),
                trim(rs.getString("marks")),
                trim(rs.getString("total")),
                trim(rs.getString("passmarks")),
                trim(rs.getString("updatedon")),
                trim(rs.getString("updatedby"))
        ));
    }

    // Legacy source data has stray trailing \r\n embedded in several text columns.
    private String trim(String value) {
        return value != null ? value.trim() : null;
    }
}
