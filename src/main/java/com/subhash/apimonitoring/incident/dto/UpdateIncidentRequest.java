package com.subhash.apimonitoring.incident.dto;


import com.subhash.apimonitoring.incident.enums.IncidentSeverity;
import com.subhash.apimonitoring.incident.enums.IncidentStatus;
import lombok.Data;

@Data
public class UpdateIncidentRequest {

    private String title;
    private String description;
    private IncidentSeverity severity;
    private IncidentStatus status;
    private String rootCause;
    private String resolutionNotes;
    private Long assignedToId;
}
