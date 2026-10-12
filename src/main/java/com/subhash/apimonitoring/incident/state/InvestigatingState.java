package com.subhash.apimonitoring.incident.state;

import com.subhash.apimonitoring.incident.entity.Incident;
import com.subhash.apimonitoring.incident.enums.IncidentStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Slf4j
@Component
public class InvestigatingState implements IncidentState {

    @Override
    public IncidentStatus getStatus() {
        return IncidentStatus.INVESTIGATING;
    }

    @Override
    public void acknowledge(Incident incident) {
        throw new IllegalStateException("Incident already acknowledged");
    }

    @Override
    public void startInvestigating(Incident incident) {
        throw new IllegalStateException("Already investigating");
    }

    @Override
    public void resolve(Incident incident, String resolution) {
        log.info("Resolving incident after investigation: {}", incident.getIncidentNumber());
        incident.setStatus(IncidentStatus.RESOLVED);
        incident.setResolvedAt(LocalDateTime.now());
        incident.setResolutionNotes(resolution);
        incident.calculateDuration();
        incident.calculateTimeToResolve();
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
        log.warn("Escalating incident (investigation taking too long): {}", incident.getIncidentNumber());
        incident.setEscalatedAt(LocalDateTime.now());
        incident.setEscalationCount(incident.getEscalationCount() + 1);
    }

    @Override
    public boolean canTransitionTo(IncidentStatus newStatus) {
        return newStatus == IncidentStatus.RESOLVED;
    }

    @Override
    public String getStateDescription() {
        return "Actively investigating root cause and working on fix";
    }
}
