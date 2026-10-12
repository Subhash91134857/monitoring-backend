package com.subhash.apimonitoring.monitoring.entity;

import com.subhash.apimonitoring.common.base.BaseEntity;
import com.subhash.apimonitoring.monitoring.enums.HealthStatus;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

/**
 * Health Check Result Entity
 *
 * WHY - Single Responsibility:
 * Stores individual health check results
 * Separate from API configuration
 *
 * HOW:
 * - Each record represents one health check execution
 * - Stores response time, status, and error details
 * - Used for analytics and reporting
 *
 * RESULT: Complete historical data for uptime analytics
 */
@Entity
@Table(name = "health_checks", indexes = {
        @Index(name = "idx_check_api_time", columnList = "api_id, checked_at"),
        @Index(name = "idx_check_status", columnList = "status"),
        @Index(name = "idx_check_checked_at", columnList = "checked_at")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class HealthCheck extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "api_id", nullable = false)
    private MonitoredApi api;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private HealthStatus status;

    @Column(name = "response_time_ms")
    private Integer responseTimeMs;

    @Column(name = "status_code")
    private Integer statusCode;

    @Column(length = 2000)
    private String responseBody;

    @Column(length = 1000)
    private String errorMessage;

    @Column(name = "checked_at", nullable = false)
    private LocalDateTime checkedAt;

    @Column(name = "checked_by")
    private String checkedBy; // Instance ID that performed the check

    @Column(name = "retry_count")
    private Integer retryCount = 0;
}
