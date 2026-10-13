package com.medibook.clinic.repository;

import com.medibook.clinic.entity.Clinic;
import com.medibook.clinic.entity.ClinicStatus;
import com.medibook.clinic.entity.ClinicStaff;
import com.medibook.clinic.entity.StaffStatus;
import com.medibook.common.util.Role;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Subquery;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

public class ClinicSpecification {

    private ClinicSpecification() {
    }

    public static Specification<Clinic> withFilters(String search, String city, ClinicStatus status,
                                                    Long requesterId, Role requesterRole) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (StringUtils.hasText(search)) {
                predicates.add(cb.like(cb.lower(root.get("name")),
                        "%" + search.trim().toLowerCase() + "%"));
            }

            if (StringUtils.hasText(city)) {
                predicates.add(cb.equal(cb.lower(root.get("city")), city.trim().toLowerCase()));
            }

            if (status != null) {
                predicates.add(cb.equal(root.get("status"), status));
            }

            if (requesterRole == Role.CLINIC_ADMIN || requesterRole == Role.CLINIC_OWNER) {
                Subquery<Long> staffSubquery = query.subquery(Long.class);
                var staffRoot = staffSubquery.from(ClinicStaff.class);
                staffSubquery.select(staffRoot.get("id"))
                        .where(
                                cb.equal(staffRoot.get("clinic").get("id"), root.get("id")),
                                cb.equal(staffRoot.get("user").get("id"), requesterId),
                                cb.equal(staffRoot.get("roleInClinic"), Role.CLINIC_ADMIN),
                                cb.equal(staffRoot.get("status"), StaffStatus.ACTIVE));
                predicates.add(cb.or(
                        cb.equal(root.get("createdBy").get("id"), requesterId),
                        cb.exists(staffSubquery)));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
