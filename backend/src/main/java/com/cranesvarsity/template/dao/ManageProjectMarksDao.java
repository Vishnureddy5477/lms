package com.cranesvarsity.template.dao;

import com.cranesvarsity.template.dto.MarksPair;
import com.cranesvarsity.template.dto.ProjectItem;
import com.cranesvarsity.template.dto.SkillTrackerMarksRow;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Raw SQL against "cranescrm.manage_project_marks" — this single table backs
 * two different student-facing features: the Report Card's per-module
 * project marks (getMarks, copied from mark-card.jsp) and the Upload
 * Projects page's document upload/listing (everything else here, copied
 * from UploadProjectDocument.jsp / GetStudentProjects.jsp). The ORDER BY
 * FIELD() list in findByRegNoAndBatch is copied verbatim, including its gap
 * (legacy's list omits "Specialization" — rows with that project_type sort
 * first, ahead of everything else, since FIELD() returns 0 for unlisted
 * values and MySQL sorts 0 before 1/2/3/4 ascending). Not "fixing" this
 * since it's existing production sort behavior, not a bug affecting data.
 */
@Repository
public class ManageProjectMarksDao {

    private final JdbcTemplate jdbcTemplate;

    public ManageProjectMarksDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public MarksPair getMarks(String regNo, String moduleName) {
        String sql = "SELECT total_marks, obtained_marks FROM manage_project_marks " +
                "WHERE registration_no = ? AND project_module = ?";
        List<MarksPair> rows = jdbcTemplate.query(sql,
                (rs, rowNum) -> new MarksPair(rs.getInt("total_marks"), rs.getInt("obtained_marks")),
                regNo, moduleName);
        return rows.isEmpty() ? MarksPair.ZERO : rows.get(0);
    }

    public Optional<String> findExistingLink(String regNo, String batch, String projectModule, String projectType) {
        String sql = "SELECT project_link FROM cranescrm.manage_project_marks " +
                "WHERE registration_no = ? AND batch = ? AND project_module = ? AND project_type = ?";
        List<String> rows = jdbcTemplate.query(sql, (rs, rowNum) -> rs.getString("project_link"),
                regNo, batch, projectModule, projectType);
        return rows.isEmpty() ? Optional.empty() : Optional.ofNullable(rows.get(0));
    }

    public int update(String regNo, String batch, String projectModule, String projectType,
                       String title, String link, String updatedDate, String stname) {
        String sql = "UPDATE cranescrm.manage_project_marks SET project_title = ?, project_link = ?, updated_date = ? " +
                "WHERE registration_no = ? AND batch = ? AND project_module = ? AND project_type = ? AND stname = ?";
        return jdbcTemplate.update(sql, title, link, updatedDate, regNo, batch, projectModule, projectType, stname);
    }

    public int insert(String regNo, String batch, String projectModule, String projectType,
                       String title, String link, String createdDate, String stname) {
        String sql = "INSERT INTO cranescrm.manage_project_marks " +
                "(registration_no, batch, project_module, project_type, project_title, project_link, created_date, stname) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        return jdbcTemplate.update(sql, regNo, batch, projectModule, projectType, title, link, createdDate, stname);
    }

    public List<ProjectItem> findByRegNoAndBatch(String regNo, String batch) {
        String sql = "SELECT project_type, project_title, project_link, project_module, " +
                "total_marks, obtained_marks, remarks, created_date, updated_date " +
                "FROM cranescrm.manage_project_marks " +
                "WHERE registration_no = ? AND batch = ? " +
                "AND project_link IS NOT NULL AND TRIM(project_link) <> '' AND project_link != 'NA' " +
                "ORDER BY FIELD(project_type, " +
                "'Cranes Programming Project', 'Cranes Hardware Project', 'College Project', 'Other Project/Internship')";

        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            String updated = rs.getString("updated_date");
            String created = rs.getString("created_date");
            return new ProjectItem(
                    rs.getString("project_type"),
                    rs.getString("project_title"),
                    rs.getString("project_link"),
                    rs.getString("project_module"),
                    rs.getString("total_marks"),
                    rs.getString("obtained_marks"),
                    rs.getString("remarks"),
                    updated != null ? updated : created
            );
        }, regNo, batch);
    }

    /**
     * Every module, unfiltered by link/batch — for the Skill Tracker's Project
     * Results tab. Distinct from {@link #findByRegNoAndBatch}, which only
     * returns rows with an uploaded link for the Upload Projects page.
     * Copied faithfully from student-performance.jsp.
     */
    public List<SkillTrackerMarksRow> findMarksByRegNo(String regNo) {
        String sql = "SELECT project_module, total_marks, obtained_marks, remarks " +
                "FROM manage_project_marks WHERE registration_no = ?";

        return jdbcTemplate.query(sql, (rs, rowNum) -> new SkillTrackerMarksRow(
                rs.getString("project_module"),
                rs.getInt("total_marks"),
                rs.getInt("obtained_marks"),
                rs.getString("remarks")
        ), regNo);
    }
}
