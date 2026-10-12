package com.subhash.apimonitoring.notification.enums;

public enum AlertType {
    API_DOWN("API Down", "Service is unavailable"),
    API_RECOVERED("API Recovered", "Service is back online"),
    API_DEGRADED("API Degraded", "Service performance issues"),
    INCIDENT_CREATED("Incident Created", "New incident opened"),
    INCIDENT_ACKNOWLEDGED("Incident Acknowledged", "Team has acknowledged"),
    INCIDENT_RESOLVED("Incident Resolved", "Incident has been resolved"),
    INCIDENT_ESCALATED("Incident Escalated", "Incident requires attention"),
    REMINDER("Reminder", "Open incident reminder"),
    MAINTENANCE("Maintenance", "Scheduled maintenance"),
    REPORT("Report", "Periodic status report");

    private final String displayName;
    private final String description;

    AlertType(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }

    public String getDisplayName() { return displayName; }
    public String getDescription() { return description; }
}