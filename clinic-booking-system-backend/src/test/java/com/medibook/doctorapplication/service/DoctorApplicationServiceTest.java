package com.medibook.doctorapplication.service;

import com.medibook.clinic.entity.*;
import com.medibook.clinic.repository.ClinicRepository;
import com.medibook.clinic.repository.ClinicStaffRepository;
import com.medibook.clinic.service.ClinicAccessService;
import com.medibook.common.exception.BadRequestException;
import com.medibook.common.exception.ConflictException;
import com.medibook.common.exception.DuplicateResourceException;
import com.medibook.common.exception.ForbiddenException;
import com.medibook.common.util.Role;
import com.medibook.doctor.dto.DoctorCreateRequest;
import com.medibook.doctor.dto.DoctorResponse;
import com.medibook.doctor.entity.Doctor;
import com.medibook.doctor.repository.DoctorRepository;
import com.medibook.doctor.service.DoctorService;
import com.medibook.doctorapplication.dto.DoctorApplicationRequest;
import com.medibook.doctorapplication.dto.ReviewRequest;
import com.medibook.doctorapplication.entity.ApplicationStatus;
import com.medibook.doctorapplication.entity.DoctorApplication;
import com.medibook.doctorapplication.event.DoctorApplicationNotificationEvent;
import com.medibook.doctorapplication.repository.DoctorApplicationRepository;
import com.medibook.notification.entity.NotificationType;
import com.medibook.user.entity.User;
import com.medibook.user.repository.UserRepository;
import com.medibook.user.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationEventPublisher;
import org.mockito.ArgumentCaptor;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class DoctorApplicationServiceTest {

    private DoctorApplicationRepository applications;
    private ClinicRepository clinics;
    private ClinicStaffRepository clinicStaff;
    private DoctorRepository doctors;
    private UserRepository users;
    private UserService userService;
    private DoctorService doctorService;
    private ClinicAccessService clinicAccess;
    private ApplicationEventPublisher events;
    private DoctorApplicationService service;
    private User patient;
    private User reviewer;
    private Clinic clinic;
    private DoctorApplication application;

    @BeforeEach
    void setUp() {
        applications = mock(DoctorApplicationRepository.class);
        clinics = mock(ClinicRepository.class);
        clinicStaff = mock(ClinicStaffRepository.class);
        doctors = mock(DoctorRepository.class);
        users = mock(UserRepository.class);
        userService = mock(UserService.class);
        doctorService = mock(DoctorService.class);
        clinicAccess = mock(ClinicAccessService.class);
        events = mock(ApplicationEventPublisher.class);
        service = new DoctorApplicationService(applications, clinics, clinicStaff, doctors, users,
                userService, doctorService, clinicAccess, events);

        patient = user(1L, Role.PATIENT, "patient@example.com");
        reviewer = user(2L, Role.CLINIC_ADMIN, "admin@example.com");
        clinic = new Clinic();
        clinic.setId(10L);
        clinic.setName("Central Clinic");
        clinic.setStatus(ClinicStatus.ACTIVE);
        clinic.setCreatedBy(reviewer);
        application = application();
    }

    @Test
    void submitsPatientApplicationAndNotifiesClinicAdmin() {
        when(users.findByEmail(patient.getEmail())).thenReturn(Optional.of(patient));
        when(clinics.findById(10L)).thenReturn(Optional.of(clinic));
        when(applications.existsByApplicantIdAndStatus(1L, ApplicationStatus.PENDING)).thenReturn(false);
        when(doctors.existsByNormalizedRegistrationNumber("reg-123")).thenReturn(false);
        when(applications.existsActiveRegistrationNumber(eq("reg-123"), anyList())).thenReturn(false);
        when(clinicStaff.findByClinicIdAndStatus(10L, StaffStatus.ACTIVE)).thenReturn(List.of());
        when(applications.saveAndFlush(any())).thenAnswer(invocation -> invocation.getArgument(0));

        var response = service.submit(request(), patient.getEmail());

        assertEquals(ApplicationStatus.PENDING, response.status());
        assertEquals("REG-123", response.registrationNumber());
        ArgumentCaptor<Object> eventCaptor = ArgumentCaptor.forClass(Object.class);
        verify(events).publishEvent(eventCaptor.capture());
        assertInstanceOf(DoctorApplicationNotificationEvent.class, eventCaptor.getValue());
        DoctorApplicationNotificationEvent notification =
                (DoctorApplicationNotificationEvent) eventCaptor.getValue();
        assertEquals(List.of(reviewer.getId()), notification.recipientIds());
        assertEquals(NotificationType.DOCTOR_APPLICATION_SUBMITTED, notification.type());
    }

    @Test
    void blocksNonPatientApplications() {
        User doctor = user(3L, Role.DOCTOR, "doctor@example.com");
        when(users.findByEmail(doctor.getEmail())).thenReturn(Optional.of(doctor));
        assertThrows(ForbiddenException.class, () -> service.submit(request(), doctor.getEmail()));
        verifyNoInteractions(clinics);
    }

    @Test
    void blocksInactiveClinic() {
        when(users.findByEmail(patient.getEmail())).thenReturn(Optional.of(patient));
        clinic.setStatus(ClinicStatus.INACTIVE);
        when(clinics.findById(10L)).thenReturn(Optional.of(clinic));
        assertThrows(ConflictException.class, () -> service.submit(request(), patient.getEmail()));
    }

    @Test
    void blocksSecondPendingApplication() {
        when(users.findByEmail(patient.getEmail())).thenReturn(Optional.of(patient));
        when(clinics.findById(10L)).thenReturn(Optional.of(clinic));
        when(applications.existsByApplicantIdAndStatus(1L, ApplicationStatus.PENDING)).thenReturn(true);
        assertThrows(DuplicateResourceException.class, () -> service.submit(request(), patient.getEmail()));
    }

    @Test
    void blocksRegistrationNumberAlreadyOwnedByDoctor() {
        when(users.findByEmail(patient.getEmail())).thenReturn(Optional.of(patient));
        when(clinics.findById(10L)).thenReturn(Optional.of(clinic));
        when(applications.existsByApplicantIdAndStatus(1L, ApplicationStatus.PENDING)).thenReturn(false);
        when(doctors.existsByNormalizedRegistrationNumber("reg-123")).thenReturn(true);
        assertThrows(DuplicateResourceException.class, () -> service.submit(request(), patient.getEmail()));
    }

    @Test
    void onlyApplicantCanWithdraw() {
        User other = user(3L, Role.PATIENT, "other@example.com");
        when(users.findByEmail(other.getEmail())).thenReturn(Optional.of(other));
        when(applications.findWithDetailsById(20L)).thenReturn(Optional.of(application));
        assertThrows(ForbiddenException.class, () -> service.withdraw(20L, other.getEmail()));
    }

    @Test
    void approvalPromotesUserCreatesDoctorAndMarksApplicationApproved() {
        when(users.findByEmail(reviewer.getEmail())).thenReturn(Optional.of(reviewer));
        when(applications.findByIdForUpdate(20L)).thenReturn(Optional.of(application));
        when(doctors.existsByNormalizedRegistrationNumber("reg-123")).thenReturn(false);
        when(applications.existsActiveRegistrationNumberExcluding(eq("reg-123"), anyList(), eq(20L)))
                .thenReturn(false);
        when(doctorService.createDoctorProfile(any(DoctorCreateRequest.class), eq(2L), eq(Role.CLINIC_ADMIN)))
                .thenReturn(DoctorResponse.builder().id(40L).build());
        Doctor doctor = Doctor.builder().user(patient).clinic(clinic).build();
        doctor.setId(40L);
        when(doctors.findById(40L)).thenReturn(Optional.of(doctor));
        when(clinicStaff.findByClinicIdAndUserId(10L, 1L)).thenReturn(Optional.empty());
        when(applications.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        var response = service.approve(20L, null, reviewer.getEmail(), Role.CLINIC_ADMIN);

        assertEquals(ApplicationStatus.APPROVED, response.status());
        assertEquals(40L, response.doctorId());
        verify(userService).updateUserRole(1L, Role.DOCTOR);
        verify(doctorService).createDoctorProfile(any(DoctorCreateRequest.class), eq(2L), eq(Role.CLINIC_ADMIN));
        verify(clinicStaff).save(argThat(staff -> staff.getRoleInClinic() == Role.DOCTOR
                && staff.getStatus() == StaffStatus.ACTIVE));
    }

    @Test
    void cannotApproveAnAlreadyApprovedApplication() {
        application.setStatus(ApplicationStatus.APPROVED);
        when(users.findByEmail(reviewer.getEmail())).thenReturn(Optional.of(reviewer));
        when(applications.findByIdForUpdate(20L)).thenReturn(Optional.of(application));
        assertThrows(ConflictException.class,
                () -> service.approve(20L, null, reviewer.getEmail(), Role.CLINIC_ADMIN));
        verifyNoInteractions(doctorService);
    }

    @Test
    void rejectionRequiresAtLeastFiveNonWhitespaceCharacters() {
        ReviewRequest request = new ReviewRequest();
        request.setRemarks("   ");
        assertThrows(BadRequestException.class,
                () -> service.reject(20L, request, reviewer.getEmail(), Role.CLINIC_ADMIN));
        verifyNoInteractions(applications, doctorService);
    }

    @Test
    void clinicAdminCannotReviewApplicationsForAnotherClinic() {
        User otherAdmin = user(4L, Role.CLINIC_ADMIN, "other-admin@example.com");
        when(users.findByEmail(otherAdmin.getEmail())).thenReturn(Optional.of(otherAdmin));
        when(applications.findByIdForUpdate(20L)).thenReturn(Optional.of(application));
        doThrow(new ForbiddenException("No access")).when(clinicAccess)
                .assertCanManageClinic(clinic, otherAdmin.getId(), Role.CLINIC_ADMIN);
        assertThrows(ForbiddenException.class,
                () -> service.reject(20L, rejectionRequest(), otherAdmin.getEmail(), Role.CLINIC_ADMIN));
    }

    @Test
    void reviewerCannotReviewOwnApplication() {
        application.setApplicant(reviewer);
        when(users.findByEmail(reviewer.getEmail())).thenReturn(Optional.of(reviewer));
        when(applications.findByIdForUpdate(20L)).thenReturn(Optional.of(application));
        assertThrows(ForbiddenException.class,
                () -> service.reject(20L, rejectionRequest(), reviewer.getEmail(), Role.CLINIC_ADMIN));
        verifyNoInteractions(clinicAccess);
    }

    private DoctorApplicationRequest request() {
        DoctorApplicationRequest request = new DoctorApplicationRequest();
        request.setClinicId(10L);
        request.setSpecialization("Cardiology");
        request.setQualification("MBBS, MD");
        request.setExperienceYears(5);
        request.setRegistrationNumber(" REG-123 ");
        request.setConsultationFee(new BigDecimal("500.00"));
        return request;
    }

    private ReviewRequest rejectionRequest() {
        ReviewRequest request = new ReviewRequest();
        request.setRemarks("Insufficient documentation");
        return request;
    }

    private DoctorApplication application() {
        DoctorApplication application = DoctorApplication.builder()
                .applicant(patient).clinic(clinic).specialization("Cardiology")
                .qualification("MBBS, MD").experienceYears(5).registrationNumber("REG-123")
                .consultationFee(new BigDecimal("500.00")).status(ApplicationStatus.PENDING).build();
        application.setId(20L);
        return application;
    }

    private User user(Long id, Role role, String email) {
        User user = User.builder().fullName("User " + id).email(email).password("encoded").role(role).build();
        user.setId(id);
        return user;
    }
}
