package com.subhash.apimonitoring.incident.entity;


import com.subhash.apimonitoring.common.base.BaseEntity;
import com.subhash.apimonitoring.incident.enums.TimelineActionType;
import com.subhash.apimonitoring.user.entity.User;
import io.hypersistence.utils.hibernate.type.json.JsonType;
import org.hibernate.annotations.Type;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Incident Timeline Entity
 *
 * WHY - Single Responsibility:
 * Tracks every action taken on an incident
 * Provides complete audit trail for incident lifecycle
 *
 * HOW:
 * - Each timeline entry represents one action
 * - Links to incident and user who performed action
 * - Stores before/after state for status changes
 *
 * RESULT: Complete audit trail for compliance and post-mortem analysis
 */
@Entity
@Table(name = "incident_timelines", indexes = {
        @Index(name = "idx_timeline_incident", columnList = "incident_id"),
        @Index(name = "idx_timeline_created", columnList = "created_at"),
        @Index(name = "idx_timeline_action", columnList = "action_type")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class IncidentTimeline extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "incident_id", nullable = false)
    private Incident incident;

    @Enumerated(EnumType.STRING)
    @Column(name = "action_type", nullable = false)
    private TimelineActionType actionType;

    @Column(length = 4000)
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "performed_by")
    private User performedBy;

    @Column(name = "performed_at", nullable = false)
    private LocalDateTime performedAt;

    @Column(name = "old_status")
    private String oldStatus;

    @Column(name = "new_status")
    private String newStatus;

    @Column(name = "old_severity")
    private String oldSeverity;

    @Column(name = "new_severity")
    private String newSeverity;

    @Column(name = "assigned_to_name")
    private String assignedToName;

    @Column(name = "ip_address")
    private String ipAddress;

    @Column(name = "user_agent")
    private String userAgent;



    @Type(JsonType.class)
    @Column(columnDefinition = "jsonb")
    private Map<String, Object> additionalData;
}