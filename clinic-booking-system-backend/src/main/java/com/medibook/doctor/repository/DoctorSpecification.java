package com.medibook.doctor.repository;

import com.medibook.clinic.entity.ClinicStatus;
import com.medibook.clinic.entity.ClinicStaff;
import com.medibook.clinic.entity.StaffStatus;
import com.medibook.common.util.Role;
import com.medibook.doctor.entity.Doctor;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class DoctorSpecification {

    public static Specification<Doctor> withFilters(
            String specialization,
            Long clinicId,
            String name,
            Integer minExperience,
            BigDecimal maxFee,
            Boolean isActive
    ) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (specialization != null && !specialization.isBlank()) {
                predicates.add(cb.like(cb.lower(root.get("specialization")),
                        "%" + specialization.toLowerCase() + "%"));
            }

            if (clinicId != null) {
                predicates.add(cb.equal(root.get("clinic").get("id"), clinicId));
            }

            if (name != null && !name.isBlank()) {
                var userJoin = root.join("user", jakarta.persistence.criteria.JoinType.INNER);
                predicates.add(cb.like(cb.lower(userJoin.get("fullName")),
                        "%" + name.toLowerCase() + "%"));
            }

            if (minExperience != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("experienceYears"), minExperience));
            }

            if (maxFee != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("consultationFee"), maxFee));
            }

            if (isActive != null) {
                predicates.add(cb.equal(root.get("isActive"), isActive));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    /** Only doctors whose clinic is ACTIVE. */
    public static Specification<Doctor> clinicIsActive() {
        return (root, query, cb) -> cb.equal(root.get("clinic").get("status"), ClinicStatus.ACTIVE);
    }

    /** Only doctors in clinics created or actively managed by this user. */
    public static Specification<Doctor> clinicManagedBy(Long userId) {
        return (root, query, cb) -> {
            var clinic = root.get("clinic");
            var staffQuery = query.subquery(Long.class);
            var staff = staffQuery.from(ClinicStaff.class);
            staffQuery.select(staff.get("id")).where(
                    cb.equal(staff.get("clinic").get("id"), clinic.get("id")),
                    cb.equal(staff.get("user").get("id"), userId),
                    cb.equal(staff.get("roleInClinic"), Role.CLINIC_ADMIN),
                    cb.equal(staff.get("status"), StaffStatus.ACTIVE));
            return cb.or(
                    cb.equal(clinic.get("createdBy").get("id"), userId),
                    cb.exists(staffQuery));
        };
    }
}
