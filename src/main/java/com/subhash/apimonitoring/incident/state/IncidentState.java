package com.subhash.apimonitoring.incident.state;

import com.subhash.apimonitoring.incident.entity.Incident;
import com.subhash.apimonitoring.incident.enums.IncidentStatus;

/**
 * State Pattern for Incident Lifecycle
 *
 * WHY - State Pattern & Open-Closed Principle:
 * Each status has its own behavior and allowed transitions
 * New states can be added without modifying existing state classes
 *
 * HOW:
 * - Each state implements allowed transitions
 * - Prevents invalid status changes
 *
 * RESULT: Clean, maintainable state management
 */
public interface IncidentState {

    IncidentStatus getStatus();

    void acknowledge(Incident incident);
    void startInvestigating(Incident incident);
    void resolve(Incident incident, String resolution);
    void close(Incident incident);
    void reopen(Incident incident);
    void escalate(Incident incident);

    boolean canTransitionTo(IncidentStatus newStatus);
    String getStateDescription();
}