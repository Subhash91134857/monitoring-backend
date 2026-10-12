// com/monitoring/common/pagination/PaginationResponse.java
package com.subhash.apimonitoring.common.pagination;

import lombok.Builder;
import lombok.Data;
import org.springframework.data.domain.Page;

import java.util.List;

/**
 * Standardized Pagination Response
 *
 * WHY: Frontend needs complete pagination metadata
 * Enables infinite scroll, pagination controls, and data fetching strategies
 *
 * RESULT:
 * - Frontend can show "X of Y items"
 * - Supports "Load More" functionality
 * - Enables efficient data fetching strategies
 */
@Data
@Builder
public class PaginationResponse<T> {

    private List<T> content;
    private Integer currentPage;
    private Integer totalPages;
    private Long totalElements;
    private Integer pageSize;
    private Boolean hasNext;
    private Boolean hasPrevious;

    public static <T> PaginationResponse<T> fromPage(Page<T> page) {
        return PaginationResponse.<T>builder()
                .content(page.getContent())
                .currentPage(page.getNumber())
                .totalPages(page.getTotalPages())
                .totalElements(page.getTotalElements())
                .pageSize(page.getSize())
                .hasNext(page.hasNext())
                .hasPrevious(page.hasPrevious())
                .build();
    }
}