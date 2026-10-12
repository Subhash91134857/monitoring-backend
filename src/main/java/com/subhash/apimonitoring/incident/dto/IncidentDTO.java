package com.subhash.apimonitoring.incident.dto;



import com.fasterxml.jackson.annotation.JsonInclude;
import com.subhash.apimonitoring.incident.enums.IncidentSeverity;
import com.subhash.apimonitoring.incident.enums.IncidentStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class IncidentDTO {

    private Long id;
    private String incidentNumber;
    private Long apiId;
    private String apiName;
    private String title;
    private String description;
    private IncidentSeverity severity;
    private IncidentStatus status;
    private LocalDateTime startedAt;
    private LocalDateTime acknowledgedAt;
    private LocalDateTime investigatingAt;
    private LocalDateTime resolvedAt;
    private LocalDateTime closedAt;
    private Integer durationSeconds;
    private Integer timeToAcknowledgeSeconds;
    private Integer timeToResolveSeconds;
    private String rootCause;
    private String resolutionNotes;
    private Long assignedToId;
    private String assignedToName;
    private Long resolvedById;
    private String resolvedByName;
    private boolean postMortemRequired;
}