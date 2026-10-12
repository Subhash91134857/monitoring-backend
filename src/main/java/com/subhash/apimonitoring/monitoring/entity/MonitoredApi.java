package com.subhash.apimonitoring.monitoring.entity;



import com.subhash.apimonitoring.common.base.BaseEntity;
import com.subhash.apimonitoring.monitoring.enums.ApiStatus;
import com.subhash.apimonitoring.monitoring.enums.HttpMethod;
import com.subhash.apimonitoring.monitoring.enums.HealthCheckType;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Monitored API Entity
 *
 * WHY - Single Responsibility:
 * Stores configuration for each API to be monitored
 * Separate from health check results
 *
 * HOW:
 * - Extends BaseEntity for audit fields
 * - Stores all configuration needed for health checks
 * - Supports multiple health check types
 * - Tracks current status for quick access
 *
 * RESULT: Complete API configuration management with audit trail
 */
@Entity
@Table(name = "monitored_apis", indexes = {
        @Index(name = "idx_api_name", columnList = "name"),
        @Index(name = "idx_api_url", columnList = "url"),
        @Index(name = "idx_api_status", columnList = "current_status"),
        @Index(name = "idx_api_active", columnList = "is_active")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class MonitoredApi extends BaseEntity {

    @Column(nullable = false, length = 200)
    private String name;

    @Column(nullable = false, length = 500)
    private String url;

    @Column(length = 200)
    private String healthEndpoint = "/health";

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private HttpMethod method = HttpMethod.GET;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @ColumnDefault("'HTTP'")
    private HealthCheckType checkType = HealthCheckType.HTTP;

    @Column(length = 200)
    private String expectedResponseBody;

    private Integer expectedStatusCode = 200;

    @Column(columnDefinition = "jsonb")
    @JdbcTypeCode(SqlTypes.JSON)
    private Map<String, String> customHeaders;

    @Column(columnDefinition = "jsonb")
    @JdbcTypeCode(SqlTypes.JSON)
    private Map<String, Object> requestBody;

    private Integer checkInterval = 60; // seconds

    private Integer timeout = 5000; // milliseconds

    private Integer retryCount = 2;

    private Boolean isActive = true;

    private String description;

    private String category; // e.g., "Payment", "Auth", "Inventory"

    private Integer priority = 1; // 1=Low, 2=Medium, 3=High, 4=Critical

    @Enumerated(EnumType.STRING)
    @Column(name = "current_status")
    private ApiStatus currentStatus = ApiStatus.UNKNOWN;

    @Column(name = "last_check_time")
    private LocalDateTime lastCheckTime;

    @Column(name = "last_response_time_ms")
    private Integer lastResponseTimeMs;

    @Column(name = "last_error")
    private String lastError;

    @Column(name = "uptime_percentage")
    private Double uptimePercentage = 100.0;

    @Column(name = "total_checks")
    private Long totalChecks = 0L;

    @Column(name = "failed_checks")
    private Long failedChecks = 0L;

    @Column(name = "consecutive_failures")
    private Integer consecutiveFailures = 0;
}
