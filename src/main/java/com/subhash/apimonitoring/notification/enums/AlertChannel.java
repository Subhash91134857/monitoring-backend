package com.subhash.apimonitoring.notification.enums;



public enum AlertChannel {
    EMAIL("Email", "Send via SMTP email"),
    SLACK("Slack", "Send to Slack channel"),
    WEBHOOK("Webhook", "HTTP POST to webhook URL"),
    SMS("SMS", "Text message via Twilio"),
    WEBSOCKET("WebSocket", "Real-time dashboard update");

    private final String displayName;
    private final String description;

    AlertChannel(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }

    public String getDisplayName() { return displayName; }
    public String getDescription() { return description; }
}