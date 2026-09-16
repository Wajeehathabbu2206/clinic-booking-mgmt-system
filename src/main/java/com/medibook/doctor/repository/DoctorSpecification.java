package com.medibook.doctor.repository;

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
            } else {
                predicates.add(cb.isTrue(root.get("isActive")));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}