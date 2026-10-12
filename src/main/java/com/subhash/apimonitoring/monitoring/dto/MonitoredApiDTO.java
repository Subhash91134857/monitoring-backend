package com.subhash.apimonitoring.monitoring.dto;


import com.fasterxml.jackson.annotation.JsonInclude;
import com.subhash.apimonitoring.monitoring.enums.ApiStatus;
import com.subhash.apimonitoring.monitoring.enums.HttpMethod;
import com.subhash.apimonitoring.monitoring.enums.HealthCheckType;
import jakarta.validation.constraints.*;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MonitoredApiDTO {

    private Long id;

    @NotBlank(message = "API name is required")
    @Size(min = 2, max = 200, message = "Name must be between 2 and 200 characters")
    private String name;

    @NotBlank(message = "API URL is required")
    @Pattern(regexp = "^(http|https)://.*$", message = "URL must start with http:// or https://")
    private String url;

    private String healthEndpoint;

    @Builder.Default
    private HttpMethod method = HttpMethod.GET;

    @Builder.Default
    private HealthCheckType checkType = HealthCheckType.HTTP;

    private String expectedResponseBody;

    private Integer expectedStatusCode;

    private Map<String, String> customHeaders;

    private Map<String, Object> requestBody;

    @Min(value = 10, message = "Check interval must be at least 10 seconds")
    @Max(value = 3600, message = "Check interval cannot exceed 3600 seconds")
    private Integer checkInterval;

    @Min(value = 1000, message = "Timeout must be at least 1000ms")
    @Max(value = 30000, message = "Timeout cannot exceed 30000ms")
    private Integer timeout;

    private Integer retryCount;

    private Boolean isActive;

    private String description;

    private String category;

    @Min(value = 1, message = "Priority must be between 1 and 4")
    @Max(value = 4, message = "Priority must be between 1 and 4")
    private Integer priority;

    // Status fields (read-only)
    private ApiStatus currentStatus;
    private LocalDateTime lastCheckTime;
    private Integer lastResponseTimeMs;
    private String lastError;
    private Double uptimePercentage;
}
