package com.subhash.apimonitoring.monitoring.enums;

public enum HealthCheckType {
    HTTP("HTTP/HTTPS endpoint"),
    TCP("TCP port check"),
    PING("ICMP ping");

    private final String description;

    HealthCheckType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
