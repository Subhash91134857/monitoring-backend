package com.subhash.apimonitoring.incident.controller;


import com.subhash.apimonitoring.common.pagination.PaginationRequest;
import com.subhash.apimonitoring.common.pagination.PaginationResponse;
import com.subhash.apimonitoring.common.response.ApiResponse;
import com.subhash.apimonitoring.incident.dto.*;
import com.subhash.apimonitoring.incident.entity.Incident;
import com.subhash.apimonitoring.incident.entity.IncidentTimeline;
import com.subhash.apimonitoring.incident.mapper.IncidentTimelineMapper;
import com.subhash.apimonitoring.incident.service.IncidentService;
import com.subhash.apimonitoring.security.util.SecurityUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/incidents")
@RequiredArgsConstructor
public class IncidentController {

    private final IncidentService incidentService;
    private final SecurityUtils securityUtils;
    private final IncidentTimelineMapper timelineMapper;

    /**
     * Create a new incident (manual creation)
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<ApiResponse<IncidentDTO>> createIncident(
            @Valid @RequestBody CreateIncidentRequest request) {
        log.info("REST request to create incident for API: {}", request.getApiId());
        IncidentDTO incident = convertToDTO(incidentService.createIncident(request));
        return ResponseEntity.ok(ApiResponse.success("Incident created successfully", incident));
    }

    /**
     * Get incident by ID
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'VIEWER')")
    public ResponseEntity<ApiResponse<IncidentDTO>> getIncidentById(@PathVariable Long id) {
        log.info("REST request to get incident by id: {}", id);
        IncidentDTO incident = incidentService.getIncidentById(id);
        return ResponseEntity.ok(ApiResponse.success(incident));
    }

    /**
     * Get all incidents with pagination
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'VIEWER')")
    public ResponseEntity<ApiResponse<PaginationResponse<IncidentDTO>>> getAllIncidents(
            @Valid PaginationRequest paginationRequest) {

        log.info("REST request to get all incidents");
        PaginationResponse<IncidentDTO> incidents = incidentService.getAllIncidents(
                paginationRequest.toPageable());
        return ResponseEntity.ok(ApiResponse.success(incidents));
    }

    /**
     * Get active incidents
     */
    @GetMapping("/active")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'VIEWER')")
    public ResponseEntity<ApiResponse<List<IncidentDTO>>> getActiveIncidents() {
        log.info("REST request to get active incidents");
        List<IncidentDTO> incidents = incidentService.getActiveIncidents();
        return ResponseEntity.ok(ApiResponse.success(incidents));
    }

    /**
     * Get incidents by API
     */
    @GetMapping("/api/{apiId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'VIEWER')")
    public ResponseEntity<ApiResponse<List<IncidentDTO>>> getIncidentsByApi(@PathVariable Long apiId) {
        log.info("REST request to get incidents by API: {}", apiId);
        List<IncidentDTO> incidents = incidentService.getIncidentsByApi(apiId);
        return ResponseEntity.ok(ApiResponse.success(incidents));
    }

    /**
     * Acknowledge incident
     */
    @PostMapping("/{id}/acknowledge")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<ApiResponse<IncidentDTO>> acknowledgeIncident(@PathVariable Long id) {
        log.info("REST request to acknowledge incident: {}", id);
        Long userId = securityUtils.getCurrentUserId();
        IncidentDTO incident = incidentService.acknowledgeIncident(id, userId);
        return ResponseEntity.ok(ApiResponse.success("Incident acknowledged", incident));
    }

    /**
     * Start investigating incident
     */
    @PostMapping("/{id}/investigate")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<ApiResponse<IncidentDTO>> startInvestigating(@PathVariable Long id) {
        log.info("REST request to start investigating incident: {}", id);
        IncidentDTO incident = incidentService.startInvestigating(id);
        return ResponseEntity.ok(ApiResponse.success("Investigation started", incident));
    }

    /**
     * Resolve incident
     */
    @PostMapping("/{id}/resolve")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<ApiResponse<IncidentDTO>> resolveIncident(
            @PathVariable Long id,
            @RequestParam String resolution) {

        log.info("REST request to resolve incident: {}", id);
        Long userId = securityUtils.getCurrentUserId();
        IncidentDTO incident = incidentService.resolveIncident(id, resolution, userId);
        return ResponseEntity.ok(ApiResponse.success("Incident resolved", incident));
    }

    /**
     * Close incident
     */
    @PostMapping("/{id}/close")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<IncidentDTO>> closeIncident(
            @PathVariable Long id,
            @RequestParam String notes) {

        log.info("REST request to close incident: {}", id);
        IncidentDTO incident = incidentService.closeIncident(id, notes);
        return ResponseEntity.ok(ApiResponse.success("Incident closed", incident));
    }

    /**
     * Update root cause
     */
    @PutMapping("/{id}/root-cause")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<ApiResponse<IncidentDTO>> updateRootCause(
            @PathVariable Long id,
            @RequestParam String rootCause) {

        log.info("REST request to update root cause for incident: {}", id);
        IncidentDTO incident = incidentService.updateRootCause(id, rootCause);
        return ResponseEntity.ok(ApiResponse.success("Root cause updated", incident));
    }

    /**
     * Get incident timeline
     */
    @GetMapping("/{id}/timeline")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'VIEWER')")
    public ResponseEntity<ApiResponse<List<IncidentTimelineDTO>>> getIncidentTimeline(@PathVariable Long id) {
        log.info("REST request to get timeline for incident: {}", id);
        List<IncidentTimeline> timeline = incidentService.getIncidentTimeline(id);
        List<IncidentTimelineDTO> timelineDTOs = timelineMapper.toDtoList(timeline);
        return ResponseEntity.ok(ApiResponse.success(timelineDTOs));
    }

    /**
     * Get incident statistics
     */
    @GetMapping("/stats")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'VIEWER')")
    public ResponseEntity<ApiResponse<IncidentStatsDTO>> getIncidentStats() {
        log.info("REST request to get incident statistics");
        IncidentStatsDTO stats = incidentService.getIncidentStats();
        return ResponseEntity.ok(ApiResponse.success(stats));
    }
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
