package com.subhash.apimonitoring.monitoring.dto;

import com.subhash.apimonitoring.monitoring.enums.HealthStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class HealthCheckDTO {
    private Long id;
    private Long apiId;
    private String apiName;
    private HealthStatus status;
    private Integer responseTimeMs;
    private Integer statusCode;
    private String errorMessage;
    private LocalDateTime checkedAt;
    private String checkedBy;
    private String responseBody;
    private int retryCount;
}