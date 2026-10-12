package com.subhash.apimonitoring.incident.state;



import com.subhash.apimonitoring.incident.entity.Incident;
import com.subhash.apimonitoring.incident.enums.IncidentStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Slf4j
@Component
public class ResolvedState implements IncidentState {

    @Override
    public IncidentStatus getStatus() {
        return IncidentStatus.RESOLVED;
    }

    @Override
    public void acknowledge(Incident incident) {
        throw new IllegalStateException("Incident already resolved");
    }

    @Override
    public void startInvestigating(Incident incident) {
        throw new IllegalStateException("Incident already resolved");
    }

    @Override
    public void resolve(Incident incident, String resolution) {
        throw new IllegalStateException("Incident already resolved");
    }

    @Override
    public void close(Incident incident) {
        log.info("Closing resolved incident: {}", incident.getIncidentNumber());
        incident.setStatus(IncidentStatus.CLOSED);
        incident.setClosedAt(LocalDateTime.now());
    }

    @Override
    public void reopen(Incident incident) {
        log.warn("Reopening incident: {}", incident.getIncidentNumber());
        incident.setStatus(IncidentStatus.OPEN);
        incident.setResolvedAt(null);
        incident.setClosedAt(null);
    }

    @Override
    public void escalate(Incident incident) {
        throw new IllegalStateException("Cannot escalate resolved incident");
    }

    @Override
    public boolean canTransitionTo(IncidentStatus newStatus) {
        return newStatus == IncidentStatus.CLOSED || newStatus == IncidentStatus.OPEN;
    }

    @Override
    public String getStateDescription() {
        return "Issue resolved, pending closure and documentation";
    }
}