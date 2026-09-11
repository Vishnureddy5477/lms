package com.cranesvarsity.template.service;

import com.cranesvarsity.template.dao.LoginAuditDao;
import com.cranesvarsity.template.dao.RetailInvoiceDao;
import com.cranesvarsity.template.dto.LoginResponse;
import com.cranesvarsity.template.model.cranescrm.Admission;
import com.cranesvarsity.template.repository.AdmissionRepository;
import com.cranesvarsity.template.security.JwtService;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

/**
 * Covers the case that took the new portal down for ~246 students while the
 * legacy JSP kept working: one email owning more than one active admission.
 *
 * The production shape that matters is two rows with DIFFERENT passwords (137
 * of the 246 look like that), because it rules out the tempting fix of just
 * taking the newest row.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class AuthServiceTest {

    private static final String EMAIL = "manasi.example@gmail.com";

    @Mock private AdmissionRepository admissionRepository;
    @Mock private RetailInvoiceDao retailInvoiceDao;
    @Mock private LoginAuditDao loginAuditDao;
    @Mock private JwtService jwtService;
    @Mock private HttpServletRequest request;

    private AuthService authService;

    /** The older enrolment — a finished diploma. */
    private Admission diploma;
    /** The newer enrolment — the internship they joined afterwards. */
    private Admission internship;

    @BeforeEach
    void setUp() {
        authService = new AuthService(admissionRepository, retailInvoiceDao, loginAuditDao, jwtService);

        diploma = admission("G2026010693213371", "diploma-pw", "PGDVD 25",
                "PG Diploma in VLSI Design & ASIC Verification", LocalDateTime.of(2025, 12, 31, 0, 0));
        internship = admission("IVTU202602614078", "internship-pw", "VTUIVLS 01",
                "Internship in VLSI Design & Verification", LocalDateTime.of(2026, 2, 6, 0, 0));

        // Everything downstream of the row choice passes, so each test is only
        // ever exercising which row got picked.
        when(admissionRepository.countValidRegistration(anyString(), anyString())).thenReturn(1L);
        when(admissionRepository.countCourseCompleted(anyString())).thenReturn(0L);
        when(retailInvoiceDao.hasOverdueDues(anyString())).thenReturn(false);
        when(jwtService.generateToken(any())).thenReturn("signed-token");
    }

    private Admission admission(String regNo, String password, String batch, String course, LocalDateTime registered) {
        Admission a = new Admission();
        a.setRegistrationNo(regNo);
        a.setEmail(EMAIL);
        a.setPassword(password);
        a.setStname("Manasi Shilwant");
        a.setBatchno(batch);
        a.setCourse(course);
        a.setDropout("NO");
        a.setRegistrationDate(registered);
        return a;
    }

    /** The repository hands rows back newest-registration-first. */
    private void givenBothEnrolments() {
        when(admissionRepository.findAllActiveByEmail(EMAIL)).thenReturn(List.of(internship, diploma));
    }

    @Test
    @DisplayName("two enrolments: the password of the OLDER row still signs in, as that row")
    void oldRowPasswordSignsInAsOldRow() {
        givenBothEnrolments();

        LoginResponse response = authService.login(EMAIL, "diploma-pw", request);

        // The whole point of the fix: "newest wins" would have rejected this.
        assertThat(response.regNo()).isEqualTo("G2026010693213371");
        assertThat(response.batch()).isEqualTo("PGDVD 25");
    }

    @Test
    @DisplayName("two enrolments: the password of the NEWER row signs in, as that row")
    void newRowPasswordSignsInAsNewRow() {
        givenBothEnrolments();

        LoginResponse response = authService.login(EMAIL, "internship-pw", request);

        assertThat(response.regNo()).isEqualTo("IVTU202602614078");
        assertThat(response.batch()).isEqualTo("VTUIVLS 01");
    }

    @Test
    @DisplayName("two enrolments sharing one password: the newest wins, so they land on the current course")
    void sharedPasswordPrefersNewestEnrolment() {
        diploma.setPassword("same-pw");
        internship.setPassword("same-pw");
        givenBothEnrolments();

        LoginResponse response = authService.login(EMAIL, "same-pw", request);

        assertThat(response.regNo()).isEqualTo("IVTU202602614078");
    }

    @Test
    @DisplayName("two enrolments, password matching neither: a plain refusal, not a 500")
    void wrongPasswordIsRefusedNotCrashed() {
        givenBothEnrolments();

        // Before the fix this line threw NonUniqueResultException out of the
        // repository and the student saw a blank 500 instead of a reason.
        assertThatThrownBy(() -> authService.login(EMAIL, "neither-of-them", request))
                .isInstanceOf(AuthException.class)
                .hasMessageContaining("Invalid password");
    }

    @Test
    @DisplayName("the ordinary single-enrolment login is unchanged")
    void singleEnrolmentStillWorks() {
        when(admissionRepository.findAllActiveByEmail(EMAIL)).thenReturn(List.of(diploma));

        LoginResponse response = authService.login(EMAIL, "diploma-pw", request);

        assertThat(response.regNo()).isEqualTo("G2026010693213371");
        assertThat(response.token()).isEqualTo("signed-token");
    }

    @Test
    @DisplayName("an unknown email is still reported as an unknown email")
    void unknownEmailIsRejected() {
        when(admissionRepository.findAllActiveByEmail(EMAIL)).thenReturn(List.of());

        assertThatThrownBy(() -> authService.login(EMAIL, "anything", request))
                .isInstanceOf(AuthException.class)
                .hasMessageContaining("Email address not found");
    }

    @Test
    @DisplayName("dues are still checked against the row actually signed in to")
    void duesAreCheckedAgainstTheChosenRow() {
        givenBothEnrolments();
        // Only the older enrolment owes anything.
        when(retailInvoiceDao.hasOverdueDues("G2026010693213371")).thenReturn(true);

        assertThatThrownBy(() -> authService.login(EMAIL, "diploma-pw", request))
                .isInstanceOf(AuthException.class)
                .hasMessageContaining("outstanding dues");

        // ...and the internship row, which owes nothing, is unaffected.
        LoginResponse response = authService.login(EMAIL, "internship-pw", request);
        assertThat(response.regNo()).isEqualTo("IVTU202602614078");
    }
}
