package com.subhash.apimonitoring.incident.enums;

/**
 * Incident Status Enum
 *
 * WHY - Single Responsibility:
 * Defines all possible states in incident lifecycle
 * Each status has specific meaning and allowed transitions
 *
 * RESULT: Clear workflow for incident management
 */
public enum IncidentStatus {

    OPEN("Open", "Incident detected but not yet acknowledged"),
    ACKNOWLEDGED("Acknowledged", "Team is aware and investigating"),
    INVESTIGATING("Investigating", "Actively investigating root cause"),
    RESOLVED("Resolved", "Issue has been fixed, awaiting confirmation"),
    CLOSED("Closed", "Incident is complete and documented");

    private final String displayName;
    private final String description;

    IncidentStatus(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }

    public String getDisplayName() { return displayName; }
    public String getDescription() { return description; }

    public boolean isActive() {
        return this == OPEN || this == ACKNOWLEDGED || this == INVESTIGATING;
    }
}