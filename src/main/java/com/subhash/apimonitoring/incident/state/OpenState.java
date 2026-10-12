package com.subhash.apimonitoring.incident.state;



import com.subhash.apimonitoring.incident.entity.Incident;
import com.subhash.apimonitoring.incident.enums.IncidentStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * OPEN State - Incident detected but not acknowledged
 *
 * Allowed transitions:
 * - OPEN → ACKNOWLEDGED
 * - OPEN → CLOSED (if false alarm)
 */
@Slf4j
@Component
public class OpenState implements IncidentState {

    @Override
    public IncidentStatus getStatus() {
        return IncidentStatus.OPEN;
    }

    @Override
    public void acknowledge(Incident incident) {
        log.info("Acknowledging incident: {}", incident.getIncidentNumber());
        incident.setStatus(IncidentStatus.ACKNOWLEDGED);
        incident.setAcknowledgedAt(LocalDateTime.now());
        incident.calculateTimeToAcknowledge();
    }

    @Override
    public void startInvestigating(Incident incident) {
        throw new IllegalStateException("Cannot investigate without first acknowledging");
    }

    @Override
    public void resolve(Incident incident, String resolution) {
        throw new IllegalStateException("Cannot resolve without first investigating");
    }

    @Override
    public void close(Incident incident) {
        log.info("Closing incident (false alarm): {}", incident.getIncidentNumber());
        incident.setStatus(IncidentStatus.CLOSED);
        incident.setClosedAt(LocalDateTime.now());
    }

    @Override
    public void reopen(Incident incident) {
        throw new IllegalStateException("Cannot reopen an open incident");
    }

    @Override
    public void escalate(Incident incident) {
        log.warn("Escalating incident (still open): {}", incident.getIncidentNumber());
        incident.setEscalatedAt(LocalDateTime.now());
        incident.setEscalationCount(incident.getEscalationCount() + 1);
    }

    @Override
    public boolean canTransitionTo(IncidentStatus newStatus) {
        return newStatus == IncidentStatus.ACKNOWLEDGED ||
                newStatus == IncidentStatus.CLOSED;
    }

    @Override
    public String getStateDescription() {
        return "Incident detected, awaiting acknowledgment";
    }
}
