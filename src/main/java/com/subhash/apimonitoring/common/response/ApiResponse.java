// com/monitoring/common/response/ApiResponse.java
package com.subhash.apimonitoring.common.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * Standardized API Response Wrapper
 *
 * WHY - Interface Segregation Principle:
 * Provides a consistent contract between backend and frontend
 * Frontend always knows the response structure regardless of endpoint
 *
 * HOW:
 * - Generic type T for data payload
 * - Builder pattern for flexible construction
 * - @JsonInclude excludes null fields
 * - Static factory methods for common scenarios
 *
 * RESULT:
 * - Consistent error handling on frontend
 * - Automatic timestamp for debugging
 * - Request tracking with path and execution time
 * - Easy monitoring of API performance
 */
@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {
    private Boolean success;
    private String message;
    private String errorCode;
    private T data;
    private LocalDateTime timestamp;
    private String path;
    private Long executionTimeMs;
    private String requestId;

    /**
     * Success response with data
     */
    public static <T> ApiResponse<T> success(T data) {
        return ApiResponse.<T>builder()
                .success(true)
                .data(data)
                .timestamp(LocalDateTime.now())
                .build();
    }

    /**
     * Success response with custom message
     */
    public static <T> ApiResponse<T> success(String message, T data) {
        return ApiResponse.<T>builder()
                .success(true)
                .message(message)
                .data(data)
                .timestamp(LocalDateTime.now())
                .build();
    }

    /**
     * Success response without data
     */
    public static <T> ApiResponse<T> success(String message) {
        return ApiResponse.<T>builder()
                .success(true)
                .message(message)
                .timestamp(LocalDateTime.now())
                .build();
    }

    /**
     * Error response
     */
    public static <T> ApiResponse<T> error(String message, String errorCode) {
        return ApiResponse.<T>builder()
                .success(false)
                .message(message)
                .errorCode(errorCode)
                .timestamp(LocalDateTime.now())
                .build();
    }

    /**
     * Detailed error response for debugging
     */
    public static <T> ApiResponse<T> error(String message, String errorCode, String path, Long executionTimeMs) {
        return ApiResponse.<T>builder()
                .success(false)
                .message(message)
                .errorCode(errorCode)
                .path(path)
                .executionTimeMs(executionTimeMs)
                .timestamp(LocalDateTime.now())
                .build();
    }
}
