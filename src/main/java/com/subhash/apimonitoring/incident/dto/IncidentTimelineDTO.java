package com.subhash.apimonitoring.incident.dto;

import com.subhash.apimonitoring.incident.enums.TimelineActionType;
import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.Map;

@Data
@Builder
public class IncidentTimelineDTO {
    private Long id;
    private Long incidentId;
    private String incidentNumber;
    private TimelineActionType actionType;
    private String description;
    private Long performedById;
    private String performedByName;
    private LocalDateTime performedAt;
    private String oldStatus;
    private String newStatus;
    private String oldSeverity;
    private String newSeverity;
    private String assignedToName;
    private String ipAddress;
    private String userAgent;
    private Map<String, Object> additionalData;
}