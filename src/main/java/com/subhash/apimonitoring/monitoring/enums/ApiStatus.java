package com.subhash.apimonitoring.monitoring.enums;



public enum ApiStatus {
    UP("UP", "Operational"),
    DOWN("DOWN", "Down"),
    DEGRADED("DEGRADED", "Degraded Performance"),
    UNKNOWN("UNKNOWN", "Unknown");

    private final String code;
    private final String displayName;

    ApiStatus(String code, String displayName) {
        this.code = code;
        this.displayName = displayName;
    }

    public String getCode() { return code; }
    public String getDisplayName() { return displayName; }
}