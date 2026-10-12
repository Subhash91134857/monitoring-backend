package com.subhash.apimonitoring.monitoring.enums;


public enum HealthStatus {
    UP("Service is operational"),
    DOWN("Service is unavailable"),
    DEGRADED("Service is partially available"),
    TIMEOUT("Health check timed out"),
    UNKNOWN("Status unknown");

    private final String description;

    HealthStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public boolean isOperational() {
        return this == UP || this == DEGRADED;
    }
}
