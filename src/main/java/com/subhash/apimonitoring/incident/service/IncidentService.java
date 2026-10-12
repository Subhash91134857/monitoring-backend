package com.subhash.apimonitoring.incident.service;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.subhash.apimonitoring.common.pagination.PaginationResponse;
import com.subhash.apimonitoring.exception.BusinessException;
import com.subhash.apimonitoring.exception.ResourceNotFoundException;
import com.subhash.apimonitoring.incident.dto.*;
import com.subhash.apimonitoring.incident.entity.Incident;
import com.subhash.apimonitoring.incident.entity.IncidentTimeline;
import com.subhash.apimonitoring.incident.enums.IncidentSeverity;
import com.subhash.apimonitoring.incident.enums.IncidentStatus;
import com.subhash.apimonitoring.incident.enums.TimelineActionType;
import com.subhash.apimonitoring.incident.repository.IncidentRepository;
import com.subhash.apimonitoring.incident.repository.IncidentTimelineRepository;
import com.subhash.apimonitoring.incident.state.IncidentStateContext;
import com.subhash.apimonitoring.monitoring.entity.MonitoredApi;
import com.subhash.apimonitoring.monitoring.repository.MonitoredApiRepository;
import com.subhash.apimonitoring.user.entity.User;
import com.subhash.apimonitoring.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class IncidentService {

    private final IncidentRepository incidentRepository;
    private final IncidentTimelineRepository timelineRepository;
    private final MonitoredApiRepository apiRepository;
    private final UserRepository userRepository;
    private final IncidentStateContext stateContext;
    public final ObjectMapper objectMapper;

    private static final DateTimeFormatter INCIDENT_NUMBER_FORMAT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd");

    /**
     * Create a new incident from API failure
     */
    @Transactional
    public Incident createIncident(CreateIncidentRequest request) {
        log.info("Creating new incident for API ID: {}", request.getApiId());

        MonitoredApi api = apiRepository.findById(request.getApiId())
                .orElseThrow(() -> new ResourceNotFoundException("API", request.getApiId()));

        // Check if there's already an open incident for this API
        List<Incident> existingIncidents = incidentRepository.findByApiId(api.getId());
        boolean hasOpenIncident = existingIncidents.stream()
                .anyMatch(i -> i.getStatus().isActive());

        if (hasOpenIncident) {
            throw new BusinessException("There is already an open incident for this API",
                    "OPEN_INCIDENT_EXISTS");
        }

        // Generate incident number
        String incidentNumber = generateIncidentNumber();

        Incident incident = Incident.builder()
                .incidentNumber(incidentNumber)
                .api(api)
                .title(request.getTitle())
                .description(request.getDescription())
                .severity(request.getSeverity())
                .status(IncidentStatus.OPEN)
                .startedAt(LocalDateTime.now())
                .postMortemRequired(request.getSeverity() == IncidentSeverity.CRITICAL)
                .build();

        Incident savedIncident = incidentRepository.save(incident);

        // Add timeline entry
        addTimelineEntry(savedIncident, TimelineActionType.CREATED,
                "Incident automatically created by monitoring system", null);

        log.info("Incident created: {}", savedIncident.getIncidentNumber());

        return savedIncident;
    }

    /**
     * Acknowledge an incident
     */
    @Transactional
    public IncidentDTO acknowledgeIncident(Long id, Long userId) {
        Incident incident = incidentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Incident", id));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));

        stateContext.transition(incident, IncidentStatus.ACKNOWLEDGED, null);
        incident.setAssignedTo(user);

        Incident updatedIncident = incidentRepository.save(incident);

        addTimelineEntry(updatedIncident, TimelineActionType.STATUS_CHANGE,
                String.format("Incident acknowledged by %s", user.getFullName()), user);

        return convertToDTO(updatedIncident);
    }

    /**
     * Start investigating an incident
     */
    @Transactional
    public IncidentDTO startInvestigating(Long id) {
        Incident incident = incidentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Incident", id));

        stateContext.transition(incident, IncidentStatus.INVESTIGATING, null);
        Incident updatedIncident = incidentRepository.save(incident);

        addTimelineEntry(updatedIncident, TimelineActionType.STATUS_CHANGE,
                "Investigation started", null);

        return convertToDTO(updatedIncident);
    }

    /**
     * Resolve an incident
     */
    @Transactional
    public IncidentDTO resolveIncident(Long id, String resolution, Long resolvedById) {
        Incident incident = incidentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Incident", id));

        User user = userRepository.findById(resolvedById)
                .orElseThrow(() -> new ResourceNotFoundException("User", resolvedById));

        stateContext.transition(incident, IncidentStatus.RESOLVED, resolution);
        incident.setResolvedBy(user);

        Incident updatedIncident = incidentRepository.save(incident);

        addTimelineEntry(updatedIncident, TimelineActionType.RESOLUTION_UPDATED,
                String.format("Incident resolved by %s: %s", user.getFullName(), resolution), user);

        return convertToDTO(updatedIncident);
    }

    /**
     * Close an incident
     */
    @Transactional
    public IncidentDTO closeIncident(Long id, String notes) {
        Incident incident = incidentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Incident", id));

        stateContext.transition(incident, IncidentStatus.CLOSED, null);
        incident.setRootCause(notes);

        Incident updatedIncident = incidentRepository.save(incident);

        addTimelineEntry(updatedIncident, TimelineActionType.COMMENT,
                "Incident closed: " + notes, null);

        return convertToDTO(updatedIncident);
    }

    /**
     * Update root cause
     */
    @Transactional
    public IncidentDTO updateRootCause(Long id, String rootCause) {
        Incident incident = incidentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Incident", id));

        incident.setRootCause(rootCause);
        Incident updatedIncident = incidentRepository.save(incident);

        addTimelineEntry(updatedIncident, TimelineActionType.ROOT_CAUSE_UPDATED,
                "Root cause identified: " + rootCause, null);

        return convertToDTO(updatedIncident);
    }

    /**
     * Auto-resolve incident when API recovers
     */

    @Transactional
    public List<Incident> autoResolveIncident(Long apiId) {

        log.info("Auto-resolving incidents for API ID: {}", apiId);

        List<Incident> openIncidents = incidentRepository.findByApiId(apiId)
                .stream()
                .filter(i -> i.getStatus().isActive())
                .toList();

        List<Incident> resolvedIncidents = new ArrayList<>();

        for (Incident incident : openIncidents) {

            incident.setStatus(IncidentStatus.RESOLVED);
            incident.setResolvedAt(LocalDateTime.now());
            incident.setResolutionNotes(
                    "API recovered automatically. Incident auto-resolved."
            );

            incident.calculateDuration();

            Incident savedIncident = incidentRepository.save(incident);

            addTimelineEntry(
                    savedIncident,
                    TimelineActionType.RESOLVED,
                    "Auto-resolved due to API recovery",
                    null
            );

            resolvedIncidents.add(savedIncident);

            log.info("Auto-resolved incident: {}",
                    savedIncident.getIncidentNumber());
        }

        return resolvedIncidents;
    }

    /**
     * Escalate incidents that haven't been acknowledged
     */
    @Transactional
    public void escalateUnacknowledgedIncidents() {
        List<Incident> unacknowledged = incidentRepository.findUnacknowledgedIncidents();
        LocalDateTime escalationThreshold = LocalDateTime.now().minusMinutes(15);

        for (Incident incident : unacknowledged) {
            if (incident.getStartedAt().isBefore(escalationThreshold)) {
                stateContext.escalate(incident);
                incidentRepository.save(incident);

                addTimelineEntry(incident, TimelineActionType.ESCALATED,
                        "Incident escalated due to no acknowledgment within 15 minutes", null);

                log.warn("Escalated incident: {}", incident.getIncidentNumber());
            }
        }
    }

    /**
     * Get incident by ID
     */
    @Transactional(readOnly = true)
    public IncidentDTO getIncidentById(Long id) {
        Incident incident = incidentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Incident", id));
        return convertToDTO(incident);
    }

    /**
     * Get all incidents with pagination
     */
    @Transactional(readOnly = true)
    public PaginationResponse<IncidentDTO> getAllIncidents(Pageable pageable) {
        Page<Incident> incidents = incidentRepository.findAll(pageable);
        return PaginationResponse.fromPage(incidents.map(this::convertToDTO));
    }

    /**
     * Get active incidents
     */
    @Transactional(readOnly = true)
    public List<IncidentDTO> getActiveIncidents() {
        return incidentRepository.findAllActiveIncidents().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get incidents by API
     */
    @Transactional(readOnly = true)
    public List<IncidentDTO> getIncidentsByApi(Long apiId) {
        return incidentRepository.findByApiId(apiId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get incident timeline
     */
    @Transactional(readOnly = true)
    public List<IncidentTimeline> getIncidentTimeline(Long incidentId) {
        return timelineRepository.findByIncidentIdOrderByPerformedAtDesc(incidentId);
    }

    /**
     * Get incident statistics
     */
    @Transactional(readOnly = true)
    public IncidentStatsDTO getIncidentStats() {
        long openIncidents = incidentRepository.findByStatus(IncidentStatus.OPEN).size();
        long acknowledgedIncidents = incidentRepository.findByStatus(IncidentStatus.ACKNOWLEDGED).size();
        long investigatingIncidents = incidentRepository.findByStatus(IncidentStatus.INVESTIGATING).size();
        long resolvedIncidents = incidentRepository.findByStatus(IncidentStatus.RESOLVED).size();
        long closedIncidents = incidentRepository.findByStatus(IncidentStatus.CLOSED).size();

        // Get incidents by severity
        LocalDateTime last30Days = LocalDateTime.now().minusDays(30);
        List<Object[]> severityCounts = incidentRepository.countIncidentsBySeverity(last30Days);
        Map<String, Long> incidentsBySeverity = severityCounts.stream()
                .collect(Collectors.toMap(
                        arr -> ((IncidentSeverity) arr[0]).getDisplayName(),
                        arr -> (Long) arr[1]
                ));

        // Get average resolution times
        Double avgAcknowledgeTime = incidentRepository.getAverageResolutionTimeBySeverity(null)
                .orElse(0.0);
        Double avgResolveTime = incidentRepository.getAverageResolutionTimeBySeverity(null)
                .orElse(0.0);

        long totalIncidentsLast30Days = incidentRepository.countResolvedIncidents(
                last30Days, LocalDateTime.now());
        long resolvedIncidentsLast30Days = incidentRepository.countResolvedIncidents(
                last30Days, LocalDateTime.now());

        return IncidentStatsDTO.builder()
                .openIncidents(openIncidents)
                .acknowledgedIncidents(acknowledgedIncidents)
                .investigatingIncidents(investigatingIncidents)
                .resolvedIncidents(resolvedIncidents)
                .closedIncidents(closedIncidents)
                .incidentsBySeverity(incidentsBySeverity)
                .averageTimeToAcknowledge(avgAcknowledgeTime / 60) // Convert to minutes
                .averageTimeToResolve(avgResolveTime / 60)
                .mttrMeanTimeToResolve(avgResolveTime / 60)
                .mtbfMeanTimeBetweenFailures(0) // Would need uptime data
                .totalIncidentsLast30Days(totalIncidentsLast30Days)
                .resolvedIncidentsLast30Days(resolvedIncidentsLast30Days)
                .build();
    }

    /**
     * Add timeline entry
     */
    private void addTimelineEntry(Incident incident, TimelineActionType actionType,
                                  String description, User user) {

        IncidentTimeline timeline = IncidentTimeline.builder()
                .incident(incident)
                .actionType(actionType)
                .description(description)
                .performedBy(user)
                .performedAt(LocalDateTime.now())
                .oldStatus(incident.getStatus().name())
                .build();

        // Add additional data as Map instead of String
        Map<String, Object> additionalData = new HashMap<>();
        additionalData.put("action", actionType.name());
        additionalData.put("timestamp", LocalDateTime.now().toString());
        additionalData.put("user",
                user != null ? user.getUsername() : "system");

        timeline.setAdditionalData(additionalData);

        timelineRepository.save(timeline);
    }

    /**
     * Generate unique incident number
     */
    private String generateIncidentNumber() {
        String datePrefix = LocalDateTime.now().format(INCIDENT_NUMBER_FORMAT);
        long count = incidentRepository.count() + 1;
        return String.format("INC-%s-%05d", datePrefix, count);
    }

    /**
     * Convert entity to DTO
     */
    private IncidentDTO convertToDTO(Incident incident) {
        return IncidentDTO.builder()
                .id(incident.getId())
                .incidentNumber(incident.getIncidentNumber())
                .apiId(incident.getApi().getId())
                .apiName(incident.getApi().getName())
                .title(incident.getTitle())
                .description(incident.getDescription())
                .severity(incident.getSeverity())
                .status(incident.getStatus())
                .startedAt(incident.getStartedAt())
                .acknowledgedAt(incident.getAcknowledgedAt())
                .investigatingAt(incident.getInvestigatingAt())
                .resolvedAt(incident.getResolvedAt())
                .closedAt(incident.getClosedAt())
                .durationSeconds(incident.getDurationSeconds())
                .timeToAcknowledgeSeconds(incident.getTimeToAcknowledgeSeconds())
                .timeToResolveSeconds(incident.getTimeToResolveSeconds())
                .rootCause(incident.getRootCause())
                .resolutionNotes(incident.getResolutionNotes())
                .assignedToId(incident.getAssignedTo() != null ? incident.getAssignedTo().getId() : null)
                .assignedToName(incident.getAssignedTo() != null ? incident.getAssignedTo().getFullName() : null)
                .resolvedById(incident.getResolvedBy() != null ? incident.getResolvedBy().getId() : null)
                .resolvedByName(incident.getResolvedBy() != null ? incident.getResolvedBy().getFullName() : null)
                .postMortemRequired(incident.isPostMortemRequired())
                .build();
    }
}
