package com.subhash.apimonitoring.monitoring.specification;


import com.subhash.apimonitoring.monitoring.entity.HealthCheck;
import com.subhash.apimonitoring.monitoring.enums.HealthStatus;
import jakarta.persistence.criteria.*;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Specification for HealthCheck entity
 *
 * WHY: Provides reusable, composable query predicates
 * HOW: Each method returns a Specification that can be combined
 * RESULT: Dynamic queries without string concatenation
 */
public class HealthCheckSpecification {

    /**
     * Filter by API ID
     */
    public static Specification<HealthCheck> byApiId(Long apiId) {
        return (root, query, criteriaBuilder) -> {
            if (apiId == null) {
                return criteriaBuilder.conjunction(); // Always true
            }
            return criteriaBuilder.equal(root.get("api").get("id"), apiId);
        };
    }

    /**
     * Filter by status
     */
    public static Specification<HealthCheck> byStatus(HealthStatus status) {
        return (root, query, criteriaBuilder) -> {
            if (status == null) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.equal(root.get("status"), status);
        };
    }

    /**
     * Filter by date range - start date
     */
    public static Specification<HealthCheck> fromDate(LocalDateTime startDate) {
        return (root, query, criteriaBuilder) -> {
            if (startDate == null) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.greaterThanOrEqualTo(root.get("checkedAt"), startDate);
        };
    }

    /**
     * Filter by date range - end date
     */
    public static Specification<HealthCheck> toDate(LocalDateTime endDate) {
        return (root, query, criteriaBuilder) -> {
            if (endDate == null) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.lessThanOrEqualTo(root.get("checkedAt"), endDate);
        };
    }

    /**
     * Filter by response time greater than
     */
    public static Specification<HealthCheck> responseTimeGreaterThan(Integer minMs) {
        return (root, query, criteriaBuilder) -> {
            if (minMs == null) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.greaterThan(root.get("responseTimeMs"), minMs);
        };
    }

    /**
     * Filter by response time less than
     */
    public static Specification<HealthCheck> responseTimeLessThan(Integer maxMs) {
        return (root, query, criteriaBuilder) -> {
            if (maxMs == null) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.lessThan(root.get("responseTimeMs"), maxMs);
        };
    }

    /**
     * Filter by error message contains text
     */
    public static Specification<HealthCheck> errorMessageContains(String searchText) {
        return (root, query, criteriaBuilder) -> {
            if (searchText == null || searchText.isEmpty()) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.like(
                    criteriaBuilder.lower(root.get("errorMessage")),
                    "%" + searchText.toLowerCase() + "%"
            );
        };
    }

    /**
     * Combine all filters - This is the main method you'll use
     */
    public static Specification<HealthCheck> withAllFilters(
            Long apiId,
            HealthStatus status,
            LocalDateTime startDate,
            LocalDateTime endDate,
            Integer minResponseTime,
            Integer maxResponseTime,
            String errorSearch) {

        return Specification.where(byApiId(apiId))
                .and(byStatus(status))
                .and(fromDate(startDate))
                .and(toDate(endDate))
                .and(responseTimeGreaterThan(minResponseTime))
                .and(responseTimeLessThan(maxResponseTime))
                .and(errorMessageContains(errorSearch));
    }
}