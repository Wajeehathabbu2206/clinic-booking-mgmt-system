package com.medibook.clinic.repository;

import com.medibook.clinic.entity.Clinic;
import com.medibook.clinic.entity.ClinicStatus;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

public class ClinicSpecification {

    private ClinicSpecification() {
    }

    public static Specification<Clinic> withFilters(String search, String city, ClinicStatus status) {
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

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}