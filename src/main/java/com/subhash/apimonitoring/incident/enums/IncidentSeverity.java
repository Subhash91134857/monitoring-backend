package com.subhash.apimonitoring.incident.enums;


/**
 * Incident Severity Enum
 *
 * WHY - Single Responsibility:
 * Defines impact levels for incidents
 * Different SLAs based on severity
 *
 * RESULT: Proper prioritization of incidents
 */
public enum IncidentSeverity {

    CRITICAL("Critical", "SEV0", "Complete service outage, customer impact", 15, 60),
    HIGH("High", "SEV1", "Major functionality broken, partial outage", 30, 120),
    MEDIUM("Medium", "SEV2", "Minor functionality broken, workaround exists", 60, 240),
    LOW("Low", "SEV3", "Cosmetic issues, no customer impact", 120, 480);

    private final String displayName;
    private final String code;
    private final String description;
    private final int acknowledgeMinutes;  // SLA for acknowledgment
    private final int resolveMinutes;       // SLA for resolution

    IncidentSeverity(String displayName, String code, String description,
                     int acknowledgeMinutes, int resolveMinutes) {
        this.displayName = displayName;
        this.code = code;
        this.description = description;
        this.acknowledgeMinutes = acknowledgeMinutes;
        this.resolveMinutes = resolveMinutes;
    }

    public String getDisplayName() { return displayName; }
    public String getCode() { return code; }
    public String getDescription() { return description; }
    public int getAcknowledgeMinutes() { return acknowledgeMinutes; }
    public int getResolveMinutes() { return resolveMinutes; }
}