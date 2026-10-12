package com.subhash.apimonitoring.monitoring.dto;


import com.subhash.apimonitoring.monitoring.enums.HealthStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class HealthCheckResult {
    private Long apiId;
    private String apiName;
    private String apiUrl;
    private HealthStatus status;
    private Integer responseTimeMs;
    private Integer statusCode;
    private String responseBody;
    private String errorMessage;
    private boolean isSuccess;
    private long timestamp;
    private String checkedBy;
    private int retryCount;

}
