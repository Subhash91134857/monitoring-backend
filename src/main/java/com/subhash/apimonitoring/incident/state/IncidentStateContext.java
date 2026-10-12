package com.subhash.apimonitoring.incident.state;



import com.subhash.apimonitoring.incident.entity.Incident;
import com.subhash.apimonitoring.incident.enums.IncidentStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Incident State Context
 *
 * WHY - State Pattern:
 * Manages state transitions and delegates to appropriate state handler
 *
 * RESULT: Clean state management with validation
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class IncidentStateContext {

    private final OpenState openState;
    private final AcknowledgedState acknowledgedState;
    private final InvestigatingState investigatingState;
    private final ResolvedState resolvedState;

    private final Map<IncidentStatus, IncidentState> stateMap = new ConcurrentHashMap<>();

    @PostConstruct
    public void init() {
        stateMap.put(IncidentStatus.OPEN, openState);
        stateMap.put(IncidentStatus.ACKNOWLEDGED, acknowledgedState);
        stateMap.put(IncidentStatus.INVESTIGATING, investigatingState);
        stateMap.put(IncidentStatus.RESOLVED, resolvedState);
    }

    public void transition(Incident incident, IncidentStatus newStatus, String notes) {
        IncidentState currentState = stateMap.get(incident.getStatus());

        if (currentState == null) {
            throw new IllegalStateException("Invalid current state: " + incident.getStatus());
        }

        if (!currentState.canTransitionTo(newStatus)) {
            throw new IllegalStateException(
                    String.format("Cannot transition from %s to %s",
                            incident.getStatus(), newStatus));
        }

        log.info("Transitioning incident {} from {} to {}",
                incident.getIncidentNumber(), incident.getStatus(), newStatus);

        // Perform the transition based on target state
        switch (newStatus) {
            case ACKNOWLEDGED -> currentState.acknowledge(incident);
            case INVESTIGATING -> currentState.startInvestigating(incident);
            case RESOLVED -> currentState.resolve(incident, notes);
            case CLOSED -> currentState.close(incident);
            case OPEN -> {
                if (currentState instanceof ResolvedState) {
                    ((ResolvedState) currentState).reopen(incident);
                }
            }
        }
    }

    public void escalate(Incident incident) {
        IncidentState currentState = stateMap.get(incident.getStatus());
        if (currentState != null) {
            currentState.escalate(incident);
        }
    }
}
