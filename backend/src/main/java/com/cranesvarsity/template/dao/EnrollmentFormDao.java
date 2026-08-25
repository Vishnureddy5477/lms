package com.cranesvarsity.template.dao;

import com.cranesvarsity.template.dto.EducationRow;
import com.cranesvarsity.template.dto.EnrollmentSubmissionRequest;
import com.cranesvarsity.template.dto.PreviousEmployment;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Raw SQL against the legacy "enrollmentform" table (schema cranescrm).
 * Column set and insert semantics copied faithfully from
 * controller.EnrollmentFormBackUp — named parameters used instead of ~103
 * positional '?' placeholders, since that many positional params is exactly
 * the kind of thing that silently goes off-by-one.
 *
 * Column-name suffixes for year-of-passing/marks are NOT uniform across the
 * 5 education stages in the legacy schema (pgyop/pgmarks vs bpyop/bpmarks vs
 * diplomayop/diplomamarks vs pucpyop/pucpmarks vs tenthpyop/tenthpmarks), so
 * each stage is mapped explicitly rather than through a generic prefix helper.
 */
@Repository
public class EnrollmentFormDao {

    private static final DateTimeFormatter ISO_DATE = DateTimeFormatter.ISO_LOCAL_DATE;

    private final JdbcTemplate jdbcTemplate;
    private final NamedParameterJdbcTemplate namedJdbcTemplate;

    public EnrollmentFormDao(JdbcTemplate jdbcTemplate, NamedParameterJdbcTemplate namedJdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        this.namedJdbcTemplate = namedJdbcTemplate;
    }

    public record EducationDetails(
            String pgBranch, String pgMarks, String pgYop,
            String degreeBranch, String degreeMarks, String degreeYop,
            String diplomaBranch, String diplomaMarks, String diplomaYop,
            String pucBranch, String pucMarks, String pucYop,
            String tenthBranch, String tenthMarks, String tenthYop
    ) {
        static final EducationDetails EMPTY = new EducationDetails(
                "NA", "NA", "NA", "NA", "NA", "NA", "NA", "NA", "NA", "NA", "NA", "NA", "NA", "NA", "NA");
    }

    /** Copied from my-profile.jsp's Educational Details tab — one row per student, "NA" when unset/absent. */
    public EducationDetails findEducationDetails(String regNo) {
        String sql = "SELECT pgstream, pgmarks, pgyop, bstream, bpmarks, bpyop, " +
                "diplomastream, diplomamarks, diplomayop, pucstream, pucpmarks, pucpyop, " +
                "tenthstream, tenthpmarks, tenthpyop " +
                "FROM enrollmentform WHERE regno = ? LIMIT 1";

        List<EducationDetails> rows = jdbcTemplate.query(sql, (rs, rowNum) -> new EducationDetails(
                orNa(rs.getString("pgstream")), orNa(rs.getString("pgmarks")), orNa(rs.getString("pgyop")),
                orNa(rs.getString("bstream")), orNa(rs.getString("bpmarks")), orNa(rs.getString("bpyop")),
                orNa(rs.getString("diplomastream")), orNa(rs.getString("diplomamarks")), orNa(rs.getString("diplomayop")),
                orNa(rs.getString("pucstream")), orNa(rs.getString("pucpmarks")), orNa(rs.getString("pucpyop")),
                orNa(rs.getString("tenthstream")), orNa(rs.getString("tenthpmarks")), orNa(rs.getString("tenthpyop"))
        ), regNo);

        return rows.isEmpty() ? EducationDetails.EMPTY : rows.get(0);
    }

    private String orNa(String value) {
        return value != null ? value : "NA";
    }

    public boolean existsByRegNo(String regNo) {
        String sql = "SELECT COUNT(*) FROM enrollmentform WHERE regno = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, regNo);
        return count != null && count > 0;
    }

    public void insert(String regNo, String batchNo, String course, String photoUrl, EnrollmentSubmissionRequest r) {
        String sql = "INSERT INTO enrollmentform (" +
                "regno, batchno, course, stname, photo, date, month, year, fathername, stdcode, " +
                "parentnumber, parentmobile, parentemail, parentpostal, pstate, pcity, ppin, " +
                "postaladdress, perstate, percity, perpin, " +
                "studentstdcode, studentnumber, studentmobile, studentemail, email1, email2, " +
                "pg, pgcollege, pguniversity, pgstream, pgyop, pgmarks, pgapineducation, pgapyears, " +
                "bdegree, bcollege, buniversity, bstream, bpyop, bpmarks, bgapineducation, bgapyears, " +
                "diploma, diplomacollege, diplomauniversity, diplomastream, diplomayop, diplomamarks, diplomagapineducation, diplomagapyears, " +
                "puc, puccollege, pucuniversity, pucstream, pucpyop, pucpmarks, pucgapineducation, pucgapyears, " +
                "tenth, tenthcollege, tenthuniversity, tenthstream, tenthpyop, tenthpmarks, tenthgapineducation, tenthgapyears, " +
                "additionalqualification, " +
                "organizationname, designation, areaofwork, domainname, technicalskills, workexp, totalexp, workingaddress, phone, email, webpageurl, " +
                "organization1, designation1, areawork1, from1, to1, " +
                "organization2, designation2, areawork2, from2, to2, " +
                "organization3, designation3, areawork3, from3, to3, " +
                "organization4, designation4, areawork4, from4, to4, " +
                "declaration, signature, placement, studentlinked" +
                ") VALUES (" +
                ":regno, :batchno, :course, :stname, :photo, :date, :month, :year, :fathername, :stdcode, " +
                ":parentnumber, :parentmobile, :parentemail, :parentpostal, :pstate, :pcity, :ppin, " +
                ":postaladdress, :perstate, :percity, :perpin, " +
                ":studentstdcode, :studentnumber, :studentmobile, :studentemail, :email1, :email2, " +
                ":pg, :pgcollege, :pguniversity, :pgstream, :pgyop, :pgmarks, :pgapineducation, :pgapyears, " +
                ":bdegree, :bcollege, :buniversity, :bstream, :bpyop, :bpmarks, :bgapineducation, :bgapyears, " +
                ":diploma, :diplomacollege, :diplomauniversity, :diplomastream, :diplomayop, :diplomamarks, :diplomagapineducation, :diplomagapyears, " +
                ":puc, :puccollege, :pucuniversity, :pucstream, :pucpyop, :pucpmarks, :pucgapineducation, :pucgapyears, " +
                ":tenth, :tenthcollege, :tenthuniversity, :tenthstream, :tenthpyop, :tenthpmarks, :tenthgapineducation, :tenthgapyears, " +
                ":additionalqualification, " +
                ":organizationname, :designation, :areaofwork, :domainname, :technicalskills, :workexp, :totalexp, :workingaddress, :phone, :email, :webpageurl, " +
                ":organization1, :designation1, :areawork1, :from1, :to1, " +
                ":organization2, :designation2, :areawork2, :from2, :to2, " +
                ":organization3, :designation3, :areawork3, :from3, :to3, " +
                ":organization4, :designation4, :areawork4, :from4, :to4, " +
                ":declaration, :signature, :placement, :studentlinked" +
                ")";

        List<EducationRow> edu = r.educationList();
        EducationRow pg = edu.get(0);
        EducationRow degree = edu.get(1);
        EducationRow diploma = edu.get(2);
        EducationRow puc = edu.get(3);
        EducationRow tenth = edu.get(4);

        List<PreviousEmployment> prev = r.previousEmployment();
        PreviousEmployment p1 = prev.get(0);
        PreviousEmployment p2 = prev.get(1);
        PreviousEmployment p3 = prev.get(2);
        PreviousEmployment p4 = prev.get(3);

        LocalDate dob = LocalDate.parse(r.dob(), ISO_DATE);

        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("regno", regNo);
        params.addValue("batchno", batchNo);
        params.addValue("course", course);
        params.addValue("stname", r.studentName());
        params.addValue("photo", photoUrl);
        params.addValue("date", String.valueOf(dob.getDayOfMonth()));
        params.addValue("month", String.valueOf(dob.getMonthValue()));
        params.addValue("year", String.valueOf(dob.getYear()));
        params.addValue("fathername", r.fatherName());
        params.addValue("stdcode", r.residenceStd());
        params.addValue("parentnumber", r.residenceNumber());
        params.addValue("parentmobile", r.parentMobile());
        params.addValue("parentemail", r.parentEmail());
        params.addValue("parentpostal", r.presentAddress());
        params.addValue("pstate", r.presentState());
        params.addValue("pcity", r.presentCity());
        params.addValue("ppin", r.presentPin());
        params.addValue("postaladdress", r.permanentAddress());
        params.addValue("perstate", r.permanentState());
        params.addValue("percity", r.permanentCity());
        params.addValue("perpin", r.permanentPin());
        params.addValue("studentstdcode", r.phoneStd());
        params.addValue("studentnumber", r.phoneNumber());
        params.addValue("studentmobile", r.studentMobile());
        params.addValue("studentemail", r.studentEmail());
        params.addValue("email1", r.email1());
        params.addValue("email2", r.skypeId());

        // PG
        params.addValue("pg", pg.degree());
        params.addValue("pgcollege", pg.college());
        params.addValue("pguniversity", pg.university());
        params.addValue("pgstream", pg.stream());
        params.addValue("pgyop", pg.passoutYear());
        params.addValue("pgmarks", pg.percentage());
        params.addValue("pgapineducation", pg.gap());
        params.addValue("pgapyears", pg.gapYears());

        // Degree (bachelor's)
        params.addValue("bdegree", degree.degree());
        params.addValue("bcollege", degree.college());
        params.addValue("buniversity", degree.university());
        params.addValue("bstream", degree.stream());
        params.addValue("bpyop", degree.passoutYear());
        params.addValue("bpmarks", degree.percentage());
        params.addValue("bgapineducation", degree.gap());
        params.addValue("bgapyears", degree.gapYears());

        // Diploma
        params.addValue("diploma", diploma.degree());
        params.addValue("diplomacollege", diploma.college());
        params.addValue("diplomauniversity", diploma.university());
        params.addValue("diplomastream", diploma.stream());
        params.addValue("diplomayop", diploma.passoutYear());
        params.addValue("diplomamarks", diploma.percentage());
        params.addValue("diplomagapineducation", diploma.gap());
        params.addValue("diplomagapyears", diploma.gapYears());

        // PUC / 12th
        params.addValue("puc", puc.degree());
        params.addValue("puccollege", puc.college());
        params.addValue("pucuniversity", puc.university());
        params.addValue("pucstream", puc.stream());
        params.addValue("pucpyop", puc.passoutYear());
        params.addValue("pucpmarks", puc.percentage());
        params.addValue("pucgapineducation", puc.gap());
        params.addValue("pucgapyears", puc.gapYears());

        // 10th
        params.addValue("tenth", tenth.degree());
        params.addValue("tenthcollege", tenth.college());
        params.addValue("tenthuniversity", tenth.university());
        params.addValue("tenthstream", tenth.stream());
        params.addValue("tenthpyop", tenth.passoutYear());
        params.addValue("tenthpmarks", tenth.percentage());
        params.addValue("tenthgapineducation", tenth.gap());
        params.addValue("tenthgapyears", tenth.gapYears());

        params.addValue("additionalqualification", r.additionalQualification());

        params.addValue("organizationname", r.employerName());
        params.addValue("designation", r.designation());
        params.addValue("areaofwork", r.areaOfWork());
        params.addValue("domainname", r.domainTechnology());
        params.addValue("technicalskills", r.technicalSkills());
        params.addValue("workexp", r.currentExperience());
        params.addValue("totalexp", r.totalExperience());
        params.addValue("workingaddress", r.employerAddress());
        params.addValue("phone", r.workPhone());
        params.addValue("email", r.workEmail());
        params.addValue("webpageurl", r.webpageUrl());

        params.addValue("organization1", p1.orgName());
        params.addValue("designation1", p1.designation());
        params.addValue("areawork1", p1.areaOfWork());
        params.addValue("from1", p1.from());
        params.addValue("to1", p1.to());

        params.addValue("organization2", p2.orgName());
        params.addValue("designation2", p2.designation());
        params.addValue("areawork2", p2.areaOfWork());
        params.addValue("from2", p2.from());
        params.addValue("to2", p2.to());

        params.addValue("organization3", p3.orgName());
        params.addValue("designation3", p3.designation());
        params.addValue("areawork3", p3.areaOfWork());
        params.addValue("from3", p3.from());
        params.addValue("to3", p3.to());

        params.addValue("organization4", p4.orgName());
        params.addValue("designation4", p4.designation());
        params.addValue("areawork4", p4.areaOfWork());
        params.addValue("from4", p4.from());
        params.addValue("to4", p4.to());

        params.addValue("declaration", r.declarationAccepted() ? "Yes" : "No");
        params.addValue("signature", r.studentName());
        params.addValue("placement", r.interestedInPlacement());
        params.addValue("studentlinked", r.linkedIn());

        namedJdbcTemplate.update(sql, params);
    }
}
