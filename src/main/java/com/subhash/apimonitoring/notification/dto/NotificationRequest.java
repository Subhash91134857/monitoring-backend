package com.subhash.apimonitoring.notification.dto;


import com.subhash.apimonitoring.notification.enums.AlertChannel;
import com.subhash.apimonitoring.notification.enums.AlertType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

import java.util.Map;

@Data
@Builder
public class NotificationRequest {

    @NotNull(message = "Alert type is required")
    private AlertType alertType;

    @NotNull(message = "Channel is required")
    private AlertChannel channel;

    @NotBlank(message = "Recipient is required")
    private String recipient;

    private String subject;

    @NotBlank(message = "Content is required")
    private String content;

    private Map<String, Object> templateData;
    private Long incidentId;
    private Long apiId;
    private String correlationId;
}
