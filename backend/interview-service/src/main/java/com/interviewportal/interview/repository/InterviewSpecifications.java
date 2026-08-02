package com.interviewportal.interview.repository;

import com.interviewportal.interview.dto.InterviewSearchCriteria;
import com.interviewportal.interview.entity.Interview;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

/**
 * Builds a dynamic JPA {@link Specification} from optional search criteria.
 *
 * <p>Why Specifications: the Criteria API composes predicates programmatically, so we add a
 * {@code WHERE} clause only for the filters the caller actually supplied. The alternatives are
 * worse here: a wall of {@code findByAAndBAndC...} methods explodes combinatorially, and hand-built
 * query strings invite SQL injection. Specifications are type-safe and injection-proof.
 *
 * <p>All string matches are case-insensitive {@code LIKE}s; equality matches hit indexed columns.
 */
public final class InterviewSpecifications {

    private InterviewSpecifications() {
    }

    public static Specification<Interview> fromCriteria(InterviewSearchCriteria c) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (hasText(c.keyword())) {
                String like = "%" + c.keyword().toLowerCase() + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("companyName")), like),
                        cb.like(cb.lower(root.get("jobRole")), like),
                        cb.like(cb.lower(root.get("overallExperience")), like)));
            }
            if (hasText(c.company())) {
                predicates.add(cb.like(cb.lower(root.get("companyName")),
                        "%" + c.company().toLowerCase() + "%"));
            }
            if (hasText(c.role())) {
                predicates.add(cb.like(cb.lower(root.get("jobRole")),
                        "%" + c.role().toLowerCase() + "%"));
            }
            if (hasText(c.location())) {
                predicates.add(cb.like(cb.lower(root.get("location")),
                        "%" + c.location().toLowerCase() + "%"));
            }
            if (c.experienceLevel() != null) {
                predicates.add(cb.equal(root.get("experienceLevel"), c.experienceLevel()));
            }
            if (c.selectionStatus() != null) {
                predicates.add(cb.equal(root.get("selectionStatus"), c.selectionStatus()));
            }
            if (c.difficultyLabel() != null) {
                predicates.add(cb.equal(root.get("difficultyLabel"), c.difficultyLabel()));
            }
            if (c.minYearsOfExperience() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("yearsOfExperience"),
                        c.minYearsOfExperience()));
            }
            if (c.maxYearsOfExperience() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("yearsOfExperience"),
                        c.maxYearsOfExperience()));
            }
            if (c.dateFrom() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("interviewDate"), c.dateFrom()));
            }
            if (c.dateTo() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("interviewDate"), c.dateTo()));
            }
            if (hasText(c.tag()) && query != null) {
                // Membership test against the element-collection of tags.
                predicates.add(cb.isMember(c.tag(), root.get("tags")));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    private static boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
