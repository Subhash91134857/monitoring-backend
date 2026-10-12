package com.subhash.apimonitoring.notification.dto;

;

import com.subhash.apimonitoring.notification.enums.AlertChannel;
import com.subhash.apimonitoring.notification.enums.AlertStatus;
import com.subhash.apimonitoring.notification.enums.AlertType;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class AlertDTO {

    private Long id;
    private Long incidentId;
    private Long apiId;
    private String apiName;
    private AlertType alertType;
    private AlertChannel channel;
    private String recipient;
    private String subject;
    private String content;
    private AlertStatus status;
    private LocalDateTime sentAt;
    private LocalDateTime deliveredAt;
    private String errorMessage;
    private Integer retryCount;
}