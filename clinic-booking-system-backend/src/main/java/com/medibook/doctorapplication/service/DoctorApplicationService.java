package com.medibook.doctorapplication.service;

import com.medibook.clinic.entity.Clinic;
import com.medibook.clinic.entity.ClinicStatus;
import com.medibook.clinic.entity.ClinicStaff;
import com.medibook.clinic.entity.StaffStatus;
import com.medibook.clinic.repository.ClinicRepository;
import com.medibook.clinic.repository.ClinicStaffRepository;
import com.medibook.clinic.service.ClinicAccessService;
import com.medibook.common.dto.PagedResponse;
import com.medibook.common.exception.BadRequestException;
import com.medibook.common.exception.ConflictException;
import com.medibook.common.exception.DuplicateResourceException;
import com.medibook.common.exception.ForbiddenException;
import com.medibook.common.exception.ResourceNotFoundException;
import com.medibook.common.util.Role;
import com.medibook.doctor.dto.DoctorCreateRequest;
import com.medibook.doctor.entity.Doctor;
import com.medibook.doctor.repository.DoctorRepository;
import com.medibook.doctor.service.DoctorService;
import com.medibook.doctorapplication.dto.DoctorApplicationRequest;
import com.medibook.doctorapplication.dto.DoctorApplicationResponse;
import com.medibook.doctorapplication.dto.ReviewRequest;
import com.medibook.doctorapplication.entity.ApplicationStatus;
import com.medibook.doctorapplication.entity.DoctorApplication;
import com.medibook.doctorapplication.event.DoctorApplicationNotificationEvent;
import com.medibook.doctorapplication.repository.DoctorApplicationRepository;
import com.medibook.notification.entity.NotificationType;
import com.medibook.user.entity.User;
import com.medibook.user.repository.UserRepository;
import com.medibook.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class DoctorApplicationService {

    private static final List<ApplicationStatus> REGISTRATION_LOCKING_STATUSES =
            List.of(ApplicationStatus.PENDING, ApplicationStatus.APPROVED);

    private final DoctorApplicationRepository applicationRepository;
    private final ClinicRepository clinicRepository;
    private final ClinicStaffRepository clinicStaffRepository;
    private final DoctorRepository doctorRepository;
    private final UserRepository userRepository;
    private final UserService userService;
    private final DoctorService doctorService;
    private final ClinicAccessService clinicAccessService;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public DoctorApplicationResponse submit(DoctorApplicationRequest request, String email) {
        User applicant = findUser(email);
        if (applicant.getRole() != Role.PATIENT) {
            throw new ForbiddenException("Only patients can apply to become a doctor");
        }

        Clinic clinic = clinicRepository.findById(request.getClinicId())
                .orElseThrow(() -> new ResourceNotFoundException("Clinic not found with id: " + request.getClinicId()));
        if (clinic.getStatus() != ClinicStatus.ACTIVE) {
            throw new ConflictException("Applications can only be submitted to active clinics");
        }
        if (applicationRepository.existsByApplicantIdAndStatus(applicant.getId(), ApplicationStatus.PENDING)) {
            throw new DuplicateResourceException("You already have a pending doctor application");
        }

        String registrationNumber = request.getRegistrationNumber().trim();
        ensureRegistrationNumberAvailable(registrationNumber, null);

        DoctorApplication application = DoctorApplication.builder()
                .applicant(applicant)
                .clinic(clinic)
                .specialization(request.getSpecialization().trim())
                .qualification(request.getQualification().trim())
                .experienceYears(request.getExperienceYears())
                .registrationNumber(registrationNumber)
                .consultationFee(request.getConsultationFee())
                .bio(clean(request.getBio()))
                .status(ApplicationStatus.PENDING)
                .build();
        DoctorApplication saved = applicationRepository.saveAndFlush(application);

        List<Long> adminIds = new ArrayList<>();
        adminIds.add(clinic.getCreatedBy().getId());
        clinicStaffRepository.findByClinicIdAndStatus(clinic.getId(), StaffStatus.ACTIVE).stream()
                .filter(staff -> staff.getRoleInClinic() == Role.CLINIC_ADMIN)
                .map(staff -> staff.getUser().getId())
                .filter(id -> !adminIds.contains(id))
                .forEach(adminIds::add);
        eventPublisher.publishEvent(new DoctorApplicationNotificationEvent(
                adminIds, NotificationType.DOCTOR_APPLICATION_SUBMITTED,
                "New doctor application",
                applicant.getFullName() + " submitted a doctor application for " + clinic.getName() + "."));

        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<DoctorApplicationResponse> myApplications(String email) {
        User applicant = findUser(email);
        return applicationRepository.findByApplicantIdOrderByCreatedAtDesc(applicant.getId()).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public DoctorApplicationResponse getById(Long id, String email) {
        User user = findUser(email);
        DoctorApplication application = findApplicationWithDetails(id);
        assertCanRead(application, user);
        return toResponse(application);
    }

    @Transactional
    public DoctorApplicationResponse withdraw(Long id, String email) {
        User applicant = findUser(email);
        DoctorApplication application = findApplicationWithDetails(id);
        if (!application.getApplicant().getId().equals(applicant.getId())) {
            throw new ForbiddenException("You can only withdraw your own application");
        }
        if (application.getStatus() != ApplicationStatus.PENDING) {
            throw new ConflictException("Only pending applications can be withdrawn");
        }
        application.setStatus(ApplicationStatus.WITHDRAWN);
        return toResponse(applicationRepository.save(application));
    }

    @Transactional(readOnly = true)
    public PagedResponse<DoctorApplicationResponse> listForClinic(Long clinicId, ApplicationStatus status,
                                                                   int page, int size, String email, Role role) {
        User reviewer = findUser(email);
        Clinic clinic = clinicRepository.findById(clinicId)
                .orElseThrow(() -> new ResourceNotFoundException("Clinic not found with id: " + clinicId));
        clinicAccessService.assertCanManageClinic(clinic, reviewer.getId(), role);

        PageRequest pageable = PageRequest.of(Math.max(page, 0), Math.min(Math.max(size, 1), 100),
                Sort.by(Sort.Order.desc("createdAt"), Sort.Order.desc("id")));
        Page<DoctorApplicationResponse> results = applicationRepository
                .findByClinicIdAndStatus(clinicId, status, pageable)
                .map(this::toResponse);
        return PagedResponse.from(results);
    }

    @Transactional
    public DoctorApplicationResponse approve(Long id, ReviewRequest request, String email, Role role) {
        User reviewer = findUser(email);
        DoctorApplication application = applicationRepository.findByIdForUpdate(id)
                .orElseThrow(() -> new ResourceNotFoundException("Doctor application not found with id: " + id));
        assertCanReview(application, reviewer, role);
        requirePending(application);
        ensureRegistrationNumberAvailable(application.getRegistrationNumber(), application.getId());

        userService.updateUserRole(application.getApplicant().getId(), Role.DOCTOR);

        DoctorCreateRequest doctorRequest = new DoctorCreateRequest();
        doctorRequest.setUserId(application.getApplicant().getId());
        doctorRequest.setClinicId(application.getClinic().getId());
        doctorRequest.setSpecialization(application.getSpecialization());
        doctorRequest.setQualification(application.getQualification());
        doctorRequest.setExperienceYears(application.getExperienceYears());
        doctorRequest.setRegistrationNumber(application.getRegistrationNumber());
        doctorRequest.setConsultationFee(application.getConsultationFee());
        doctorRequest.setBio(application.getBio());

        Doctor doctor = doctorRepository.findById(doctorService.createDoctorProfile(
                        doctorRequest, reviewer.getId(), role).getId())
                .orElseThrow(() -> new IllegalStateException("Created doctor profile could not be loaded"));
        ensureDoctorClinicStaff(doctor);

        application.setDoctor(doctor);
        application.setStatus(ApplicationStatus.APPROVED);
        application.setReviewedBy(reviewer);
        application.setReviewedAt(LocalDateTime.now());
        application.setReviewRemarks(clean(request == null ? null : request.getRemarks()));
        DoctorApplication saved = applicationRepository.save(application);

        publishApplicantNotification(application, NotificationType.DOCTOR_APPLICATION_APPROVED,
                "Doctor application approved", "Your doctor application was approved.");
        return toResponse(saved);
    }

    @Transactional
    public DoctorApplicationResponse reject(Long id, ReviewRequest request, String email, Role role) {
        String remarks = clean(request == null ? null : request.getRemarks());
        if (remarks == null || remarks.length() < 5) {
            throw new BadRequestException("Rejection remarks must contain at least 5 characters");
        }

        User reviewer = findUser(email);
        DoctorApplication application = applicationRepository.findByIdForUpdate(id)
                .orElseThrow(() -> new ResourceNotFoundException("Doctor application not found with id: " + id));
        assertCanReview(application, reviewer, role);
        requirePending(application);

        application.setStatus(ApplicationStatus.REJECTED);
        application.setReviewedBy(reviewer);
        application.setReviewedAt(LocalDateTime.now());
        application.setReviewRemarks(remarks);
        DoctorApplication saved = applicationRepository.save(application);

        publishApplicantNotification(application, NotificationType.DOCTOR_APPLICATION_REJECTED,
                "Doctor application rejected", "Your doctor application was rejected. Remarks: " + remarks);
        return toResponse(saved);
    }

    private void ensureRegistrationNumberAvailable(String registrationNumber, Long excludedApplicationId) {
        String normalized = normalizeRegistrationNumber(registrationNumber);
        if (doctorRepository.existsByNormalizedRegistrationNumber(normalized)) {
            throw new DuplicateResourceException("This registration number already belongs to a doctor");
        }
        boolean usedByApplication = excludedApplicationId == null
                ? applicationRepository.existsActiveRegistrationNumber(normalized, REGISTRATION_LOCKING_STATUSES)
                : applicationRepository.existsActiveRegistrationNumberExcluding(
                        normalized, REGISTRATION_LOCKING_STATUSES, excludedApplicationId);
        if (usedByApplication) {
            throw new DuplicateResourceException("This registration number is already in use by an application");
        }
    }

    private void ensureDoctorClinicStaff(Doctor doctor) {
        ClinicStaff staff = clinicStaffRepository.findByClinicIdAndUserId(
                        doctor.getClinic().getId(), doctor.getUser().getId())
                .orElseGet(ClinicStaff::new);
        staff.setClinic(doctor.getClinic());
        staff.setUser(doctor.getUser());
        staff.setRoleInClinic(Role.DOCTOR);
        staff.setStatus(StaffStatus.ACTIVE);
        clinicStaffRepository.save(staff);
    }

    private void publishApplicantNotification(DoctorApplication application, NotificationType type,
                                               String title, String message) {
        eventPublisher.publishEvent(new DoctorApplicationNotificationEvent(
                List.of(application.getApplicant().getId()), type, title, message));
    }

    private void assertCanRead(DoctorApplication application, User user) {
        if (application.getApplicant().getId().equals(user.getId()) || user.getRole() == Role.SUPER_ADMIN) {
            return;
        }
        clinicAccessService.assertCanManageClinic(application.getClinic(), user.getId(), user.getRole());
    }

    private void assertCanReview(DoctorApplication application, User reviewer, Role role) {
        if (application.getApplicant().getId().equals(reviewer.getId())) {
            throw new ForbiddenException("You cannot review your own application");
        }
        clinicAccessService.assertCanManageClinic(application.getClinic(), reviewer.getId(), role);
    }

    private void requirePending(DoctorApplication application) {
        if (application.getStatus() != ApplicationStatus.PENDING) {
            throw new ConflictException("Only pending applications can be reviewed");
        }
    }

    private User findUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + email));
    }

    private DoctorApplication findApplicationWithDetails(Long id) {
        return applicationRepository.findWithDetailsById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Doctor application not found with id: " + id));
    }

    private String normalizeRegistrationNumber(String value) {
        return value.trim().toLowerCase(Locale.ROOT);
    }

    private String clean(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }

    private DoctorApplicationResponse toResponse(DoctorApplication application) {
        User applicant = application.getApplicant();
        Clinic clinic = application.getClinic();
        return new DoctorApplicationResponse(
                application.getId(),
                application.getStatus(),
                new DoctorApplicationResponse.Applicant(applicant.getId(), applicant.getFullName(), applicant.getEmail()),
                new DoctorApplicationResponse.ClinicSummary(clinic.getId(), clinic.getName()),
                application.getSpecialization(),
                application.getQualification(),
                application.getExperienceYears(),
                application.getRegistrationNumber(),
                application.getConsultationFee(),
                application.getBio(),
                application.getReviewRemarks(),
                application.getReviewedBy() == null ? null : application.getReviewedBy().getFullName(),
                application.getReviewedAt(),
                application.getDoctor() == null ? null : application.getDoctor().getId(),
                application.getCreatedAt());
    }
}
