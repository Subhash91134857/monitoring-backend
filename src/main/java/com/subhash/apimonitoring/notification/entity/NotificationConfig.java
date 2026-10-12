package com.subhash.apimonitoring.notification.entity;


import com.subhash.apimonitoring.common.base.BaseEntity;
import com.subhash.apimonitoring.notification.enums.AlertChannel;
import com.subhash.apimonitoring.notification.enums.AlertType;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

/**
 * Notification Configuration Entity
 *
 * WHY - Single Responsibility:
 * Stores user preferences for notifications
 * Allows per-user/per-team configuration
 *
 * RESULT: Flexible notification routing
 */
@Entity
@Table(name = "notification_configs", indexes = {
        @Index(name = "idx_notif_user", columnList = "user_id"),
        @Index(name = "idx_notif_channel", columnList = "channel")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class NotificationConfig extends BaseEntity {

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "team_id")
    private Long teamId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AlertChannel channel;

    @Column(nullable = false, length = 500)
    private String destination;  // Email address, webhook URL, etc.

    @Enumerated(EnumType.STRING)
    private AlertType alertType;

    @Column(name = "is_enabled")
    private Boolean enabled = true;

    @Column(name = "severity_threshold")
    private String severityThreshold;  // Only send alerts above this severity

    @Column(name = "rate_limit_minutes")
    private Integer rateLimitMinutes = 60;  // Max alerts per hour

    @Column(name = "rate_limit_count")
    private Integer rateLimitCount = 10;  // Max number of alerts
}
