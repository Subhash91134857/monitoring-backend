package com.subhash.apimonitoring.notification.enums;



public enum AlertStatus {
    PENDING("Pending", "Queued for sending"),
    SENT("Sent", "Sent successfully"),
    DELIVERED("Delivered", "Confirmed delivered"),
    FAILED("Failed", "Failed to send"),
    RETRYING("Retrying", "Will retry later");

    private final String displayName;
    private final String description;

    AlertStatus(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }

    public String getDisplayName() { return displayName; }
    public String getDescription() { return description; }
}