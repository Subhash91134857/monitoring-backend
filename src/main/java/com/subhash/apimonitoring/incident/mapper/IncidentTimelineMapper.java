package com.subhash.apimonitoring.incident.mapper;

import com.subhash.apimonitoring.incident.dto.IncidentTimelineDTO;
import com.subhash.apimonitoring.incident.entity.IncidentTimeline;
import org.springframework.stereotype.Component;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class IncidentTimelineMapper {
    public IncidentTimelineDTO toDto(IncidentTimeline entity) {
        if (entity == null) return null;
        return IncidentTimelineDTO.builder()
                .id(entity.getId())
                .incidentId(entity.getIncident() != null ? entity.getIncident().getId() : null)
                .incidentNumber(entity.getIncident() != null ? entity.getIncident().getIncidentNumber() : null)
                .actionType(entity.getActionType())
                .description(entity.getDescription())
                .performedById(entity.getPerformedBy() != null ? entity.getPerformedBy().getId() : null)
                .performedByName(entity.getPerformedBy() != null ? entity.getPerformedBy().getFullName() : null)
                .performedAt(entity.getPerformedAt())
                .oldStatus(entity.getOldStatus())
                .newStatus(entity.getNewStatus())
                .oldSeverity(entity.getOldSeverity())
                .newSeverity(entity.getNewSeverity())
                .assignedToName(entity.getAssignedToName())
                .ipAddress(entity.getIpAddress())
                .userAgent(entity.getUserAgent())
                .additionalData(entity.getAdditionalData())
                .build();
    }

    public List<IncidentTimelineDTO> toDtoList(List<IncidentTimeline> entities) {
        return entities.stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }
}
