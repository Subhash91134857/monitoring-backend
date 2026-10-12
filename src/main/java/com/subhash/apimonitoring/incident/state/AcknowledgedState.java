package com.subhash.apimonitoring.incident.state;


import com.subhash.apimonitoring.incident.entity.Incident;
import com.subhash.apimonitoring.incident.enums.IncidentStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * ACKNOWLEDGED State - Team is aware and investigating
 *
 * Allowed transitions:
 * - ACKNOWLEDGED → INVESTIGATING
 * - ACKNOWLEDGED → RESOLVED (if quick fix)
 */
@Slf4j
@Component
public class AcknowledgedState implements IncidentState {

    @Override
    public IncidentStatus getStatus() {
        return IncidentStatus.ACKNOWLEDGED;
    }

    @Override
    public void acknowledge(Incident incident) {
        throw new IllegalStateException("Incident already acknowledged");
    }

    @Override
    public void startInvestigating(Incident incident) {
        log.info("Starting investigation for incident: {}", incident.getIncidentNumber());
        incident.setStatus(IncidentStatus.INVESTIGATING);
        incident.setInvestigatingAt(LocalDateTime.now());
    }

    @Override
    public void resolve(Incident incident, String resolution) {
        log.info("Resolving incident from acknowledged state: {}", incident.getIncidentNumber());
        incident.setStatus(IncidentStatus.RESOLVED);
        incident.setResolvedAt(LocalDateTime.now());
        incident.setResolutionNotes(resolution);
        incident.calculateDuration();
    }

    @Override
    public void close(Incident incident) {
        throw new IllegalStateException("Cannot close without resolving first");
    }

    @Override
    public void reopen(Incident incident) {
        throw new IllegalStateException("Cannot reopen an active incident");
    }

    @Override
    public void escalate(Incident incident) {
        log.warn("Escalating incident (still open): {}", incident.getIncidentNumber());
        incident.setEscalatedAt(LocalDateTime.now());
        incident.setEscalationCount(incident.getEscalationCount() + 1);
    }

    @Override
    public boolean canTransitionTo(IncidentStatus newStatus) {
        return newStatus == IncidentStatus.INVESTIGATING ||
                newStatus == IncidentStatus.RESOLVED;
    }

    @Override
    public String getStateDescription() {
        return "Team acknowledged, investigating root cause";
    }
}