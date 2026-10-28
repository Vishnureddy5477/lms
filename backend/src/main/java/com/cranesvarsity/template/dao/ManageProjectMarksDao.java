package com.cranesvarsity.template.dao;

import com.cranesvarsity.template.dto.ProjectItem;
import com.cranesvarsity.template.dto.SkillTrackerMarksRow;
import com.cranesvarsity.template.util.Marks;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * Raw SQL against "cranescrm.manage_project_marks" — this single table backs
 * two different student-facing features: the Report Card's project marks and
 * the Upload Projects page's document upload/listing (everything below
 * findExistingLink, copied from UploadProjectDocument.jsp /
 * GetStudentProjects.jsp). The ORDER BY FIELD() list in findByRegNoAndBatch is
 * copied verbatim, including its gap (legacy's list omits "Specialization" —
 * rows with that project_type sort first, ahead of everything else, since
 * FIELD() returns 0 for unlisted values and MySQL sorts 0 before 1/2/3/4
 * ascending). Not "fixing" this since it's existing production sort behavior,
 * not a bug affecting data.
 *
 * WHY PROJECTS ARE KEYED BY TYPE, NOT BY MODULE
 *
 * The report card used to look projects up by project_module against the
 * training module list, which could never match: project_module holds a
 * project CATEGORY — "Cranes Programming Project", "Cranes Hardware Project",
 * "Specialization", "College Project", "Other Project/Internship" — not a
 * module name. That is why the Projects column read 0/0 for every student.
 * Projects belong to the batch as a whole and are weighted by project_type,
 * exactly as the Performance Master Sheet scores them.
 */
@Repository
public class ManageProjectMarksDao {

    /** Project 1 and 2 are worth 25 each, Project 3 is worth 50. Same weights as the Performance Master Sheet. */
    public static final Map<Integer, BigDecimal> SLOT_WEIGHTS = Map.of(
            1, new BigDecimal("25"), 2, new BigDecimal("25"), 3, new BigDecimal("50"));

    private final JdbcTemplate jdbcTemplate;

    public ManageProjectMarksDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /** One scored project: its category label and the student's mark. */
    public record ProjectMark(String label, BigDecimal obtained) {
    }

    /**
     * project_type is free text ("Project 1", "Project 2", ...). Classified by
     * digit, as the Performance Master Sheet does. Project 4 and 5 exist in the
     * data and do not score, so they return null.
     */
    public static Integer slotOf(String projectType) {
        String s = projectType == null ? "" : projectType;
        if (s.contains("1")) {
            return 1;
        }
        if (s.contains("2")) {
            return 2;
        }
        if (s.contains("3")) {
            return 3;
        }
        return null;
    }

    /** Which project slots this batch ran, for any student. */
    public Set<Integer> slotsConductedForBatch(String batch) {
        String sql = "SELECT DISTINCT project_type FROM manage_project_marks WHERE TRIM(batch) = ?";
        Set<Integer> out = new HashSet<>();
        jdbcTemplate.query(sql, rs -> {
            Integer slot = slotOf(rs.getString("project_type"));
            if (slot != null) {
                out.add(slot);
            }
        }, batch);
        return out;
    }

    /** This student's project mark per slot — newest row wins. */
    public Map<Integer, ProjectMark> latestObtainedBySlot(String regNo) {
        String sql = "SELECT project_type, project_module, obtained_marks AS o FROM manage_project_marks " +
                "WHERE TRIM(registration_no) = ? ORDER BY id DESC";
        Map<Integer, ProjectMark> out = new HashMap<>();
        jdbcTemplate.query(sql, rs -> {
            Integer slot = slotOf(rs.getString("project_type"));
            if (slot != null) {
                out.putIfAbsent(slot, new ProjectMark(
                        rs.getString("project_module"), Marks.value(rs.getString("o"))));
            }
        }, regNo);
        return out;
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
