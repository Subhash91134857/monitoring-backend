package com.subhash.apimonitoring.incident.enums;

public enum TimelineActionType {
    CREATED("Incident created"),
    STATUS_CHANGE("Status changed"),
    SEVERITY_CHANGE("Severity changed"),
    ASSIGNED("User assigned"),
    COMMENT("Comment added"),
    ESCALATED("Incident escalated"),
    REMINDER_SENT("Reminder sent"),
    ROOT_CAUSE_UPDATED("Root cause updated"),
    RESOLUTION_UPDATED("Resolution notes updated"),
    MERGED("Merged with another incident"),
    REOPENED("Incident reopened"),
    RESOLVED("Incident Resolved");
    private final String description;

    TimelineActionType(String description) {
        this.description = description;
    }

    public String getDescription() { return description; }
}