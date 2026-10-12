package com.subhash.apimonitoring.incident.entity;



import com.subhash.apimonitoring.common.base.BaseEntity;
import com.subhash.apimonitoring.incident.enums.IncidentSeverity;
import com.subhash.apimonitoring.incident.enums.IncidentStatus;
import com.subhash.apimonitoring.monitoring.entity.MonitoredApi;
import com.subhash.apimonitoring.user.entity.User;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

/**
 * Incident Entity
 *
 * WHY - Single Responsibility:
 * Tracks complete incident lifecycle from detection to resolution
 * Separate from health checks for better incident management
 *
 * HOW:
 * - Links to MonitoredApi that caused the incident
 * - Tracks severity, status, and timestamps
 * - Stores root cause and resolution notes for post-mortem
 * - Supports assignment to team members
 *
 * RESULT: Complete incident tracking with audit trail for SLA compliance
 */
@Entity
@Table(name = "incidents", indexes = {
        @Index(name = "idx_incident_api", columnList = "api_id"),
        @Index(name = "idx_incident_status", columnList = "status"),
        @Index(name = "idx_incident_severity", columnList = "severity"),
        @Index(name = "idx_incident_started", columnList = "started_at"),
        @Index(name = "idx_incident_assigned", columnList = "assigned_to")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class Incident extends BaseEntity {

    @Column(nullable = false, unique = true, length = 50)
    private String incidentNumber;  // Format: INC-2024-00001

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "api_id", nullable = false)
    private MonitoredApi api;

    @Column(nullable = false, length = 500)
    private String title;

    @Column(length = 4000)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private IncidentSeverity severity;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private IncidentStatus status;

    @Column(name = "started_at", nullable = false)
    private LocalDateTime startedAt;

    @Column(name = "acknowledged_at")
    private LocalDateTime acknowledgedAt;

    @Column(name = "investigating_at")
    private LocalDateTime investigatingAt;

    @Column(name = "resolved_at")
    private LocalDateTime resolvedAt;

    @Column(name = "closed_at")
    private LocalDateTime closedAt;

    @Column(name = "duration_seconds")
    private Integer durationSeconds;

    @Column(name = "time_to_acknowledge_seconds")
    private Integer timeToAcknowledgeSeconds;

    @Column(name = "time_to_resolve_seconds")
    private Integer timeToResolveSeconds;

    @Column(length = 4000)
    private String rootCause;

    @Column(length = 4000)
    private String resolutionNotes;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_to")
    private User assignedTo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "resolved_by")
    private User resolvedBy;

    @Column(name = "escalated_at")
    private LocalDateTime escalatedAt;

    @Column(name = "escalation_count")
    private Integer escalationCount = 0;

    @Column(name = "reminder_sent_at")
    private LocalDateTime reminderSentAt;

    @Column(name = "is_post_mortem_required")
    private boolean postMortemRequired = false;

    /**
     * Calculate incident duration in seconds
     */
    public void calculateDuration() {
        if (startedAt != null && resolvedAt != null) {
            this.durationSeconds = (int) java.time.Duration.between(startedAt, resolvedAt).getSeconds();
        }
    }

    /**
     * Calculate time to acknowledge
     */
    public void calculateTimeToAcknowledge() {
        if (startedAt != null && acknowledgedAt != null) {
            this.timeToAcknowledgeSeconds = (int) java.time.Duration.between(startedAt, acknowledgedAt).getSeconds();
        }
    }

    /**
     * Calculate time to resolve
     */
    public void calculateTimeToResolve() {
        if (startedAt != null && resolvedAt != null) {
            this.timeToResolveSeconds = (int) java.time.Duration.between(startedAt, resolvedAt).getSeconds();
        }
    }
}