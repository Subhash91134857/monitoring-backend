// com/monitoring/common/pagination/PaginationRequest.java
package com.subhash.apimonitoring.common.pagination;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

/**
 * Standardized Pagination Request
 *
 * WHY: Consistent pagination across all list endpoints
 * Frontend can use same parameters for all pages
 *
 * HOW:
 * - Validation ensures reasonable page sizes
 * - Factory method converts to Spring Pageable
 * - Builder pattern for flexible construction
 *
 * RESULT:
 * - Prevents resource exhaustion with max page size
 * - Consistent sorting across endpoints
 * - Easy integration with Spring Data JPA
 */
@Data
public class PaginationRequest {

    @Min(value = 0, message = "Page number must be >= 0")
    private Integer page = 0;

    @Min(value = 1, message = "Page size must be >= 1")
    @Max(value = 100, message = "Page size must be <= 100")
    private Integer size = 20;

    private String sortBy = "createdAt";

    private Sort.Direction sortDirection = Sort.Direction.DESC;

    /**
     * Convert to Spring Data JPA Pageable
     */
    public Pageable toPageable() {
        Sort sort = Sort.by(sortDirection, sortBy);
        return PageRequest.of(page, size, sort);
    }
}