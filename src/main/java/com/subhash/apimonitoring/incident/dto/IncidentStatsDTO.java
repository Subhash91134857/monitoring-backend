package com.subhash.apimonitoring.incident.dto;

import lombok.Builder;
import lombok.Data;
import java.util.Map;

@Data
@Builder
public class IncidentStatsDTO {

    private long openIncidents;
    private long acknowledgedIncidents;
    private long investigatingIncidents;
    private long resolvedIncidents;
    private long closedIncidents;

    private Map<String, Long> incidentsBySeverity;
    private Map<String, Long> incidentsByApi;

    private double averageTimeToAcknowledge;
    private double averageTimeToResolve;
    private double mttrMeanTimeToResolve;
    private double mtbfMeanTimeBetweenFailures;

    private long totalIncidentsLast30Days;
    private long resolvedIncidentsLast30Days;
}
