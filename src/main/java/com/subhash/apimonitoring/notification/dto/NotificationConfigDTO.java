package com.subhash.apimonitoring.notification.dto;

import com.subhash.apimonitoring.notification.enums.AlertChannel;
import com.subhash.apimonitoring.notification.enums.AlertType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class NotificationConfigDTO {

    private Long id;
    private Long userId;
    private Long teamId;

    @NotNull(message = "Channel is required")
    private AlertChannel channel;

    @NotBlank(message = "Destination is required")
    private String destination;

    private AlertType alertType;
    private Boolean enabled;
    private String severityThreshold;
    private Integer rateLimitMinutes;
    private Integer rateLimitCount;
}