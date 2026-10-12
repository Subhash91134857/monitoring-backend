package com.subhash.apimonitoring.notification.entity;


import com.subhash.apimonitoring.common.base.BaseEntity;
import com.subhash.apimonitoring.incident.entity.Incident;
import com.subhash.apimonitoring.monitoring.entity.MonitoredApi;
import com.subhash.apimonitoring.notification.enums.AlertChannel;
import com.subhash.apimonitoring.notification.enums.AlertStatus;
import com.subhash.apimonitoring.notification.enums.AlertType;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

/**
 * Alert Entity
 *
 * WHY - Single Responsibility:
 * Tracks every notification sent by the system
 * Provides audit trail for all alerts
 *
 * HOW:
 * - Links to incident or API that triggered alert
 * - Tracks channel, status, and delivery attempts
 * - Stores retry information for failed alerts
 *
 * RESULT: Complete notification history for compliance
 */
@Entity
@Table(name = "alerts", indexes = {
        @Index(name = "idx_alert_incident", columnList = "incident_id"),
        @Index(name = "idx_alert_status", columnList = "status"),
        @Index(name = "idx_alert_channel", columnList = "channel"),
        @Index(name = "idx_alert_created", columnList = "created_at")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class Alert extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "incident_id")
    private Incident incident;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "api_id")
    private MonitoredApi api;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AlertType alertType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AlertChannel channel;

    @Column(nullable = false, length = 500)
    private String recipient;

    @Column(length = 500)
    private String subject;

    @Column(length = 4000)
    private String content;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AlertStatus status;

    @Column(name = "sent_at")
    private LocalDateTime sentAt;

    @Column(name = "delivered_at")
    private LocalDateTime deliveredAt;

    @Column(name = "failed_at")
    private LocalDateTime failedAt;

    @Column(name = "error_message", length = 1000)
    private String errorMessage;

    @Column(name = "retry_count")
    private Integer retryCount = 0;

    @Column(name = "max_retries")
    private Integer maxRetries = 3;

    @Column(name = "next_retry_at")
    private LocalDateTime nextRetryAt;

    @Column(name = "external_id")
    private String externalId;  // e.g., Message ID from email provider
}