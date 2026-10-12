package com.subhash.apimonitoring.notification.strategy;



import com.subhash.apimonitoring.incident.entity.Incident;
import com.subhash.apimonitoring.incident.repository.IncidentRepository;
import com.subhash.apimonitoring.monitoring.entity.MonitoredApi;
import com.subhash.apimonitoring.monitoring.repository.MonitoredApiRepository;
import com.subhash.apimonitoring.notification.dto.NotificationRequest;
import com.subhash.apimonitoring.notification.entity.Alert;
import com.subhash.apimonitoring.notification.enums.AlertChannel;
import com.subhash.apimonitoring.notification.enums.AlertStatus;
import com.subhash.apimonitoring.notification.repository.AlertRepository;
import com.slack.api.Slack;
import com.slack.api.webhook.Payload;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * Slack Alert Strategy
 *
 * WHY - Single Responsibility:
 * Handles ONLY Slack-specific notification logic
 *
 * RESULT: Real-time team notifications with rich formatting
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SlackAlertStrategy implements AlertStrategy {

    private final AlertRepository alertRepository;
    private final Slack slack = Slack.getInstance();
    private final IncidentRepository incidentRepository;
    private final MonitoredApiRepository apiRepository;

    @Value("${app.notification.slack.webhook-url:}")
    private String webhookUrl;

    @Value("${app.notification.slack.enabled:false}")
    private boolean slackEnabled;

    @Override
    public Alert send(NotificationRequest request) {
        log.info("Sending Slack alert to channel: {}", request.getRecipient());

        if (!slackEnabled) {
            log.warn("Slack notifications are disabled");
            return createFailedAlert(request, "Slack notifications are disabled");
        }

        Alert alert = createAlertRecord(request);

        try {
            String webhook = webhookUrl != null ? webhookUrl : request.getRecipient();

            Payload payload = Payload.builder()
                    .channel(request.getRecipient())
                    .text(buildSlackMessage(request))
                    .username("API Monitor")
                    .iconEmoji(":warning:")
                    .build();

            com.slack.api.webhook.WebhookResponse response = slack.send(webhook, payload);

            if (response.getCode() == 200) {
                alert.setStatus(AlertStatus.SENT);
                alert.setSentAt(LocalDateTime.now());
                log.info("Slack alert sent successfully");
            } else {
                alert.setStatus(AlertStatus.FAILED);
                alert.setFailedAt(LocalDateTime.now());
                alert.setErrorMessage("HTTP " + response.getCode() + ": " + response.getMessage());
            }

        } catch (Exception e) {
            log.error("Failed to send Slack alert", e);
            alert.setStatus(AlertStatus.FAILED);
            alert.setFailedAt(LocalDateTime.now());
            alert.setErrorMessage(e.getMessage());
        }

        return alertRepository.save(alert);
    }

    private String buildSlackMessage(NotificationRequest request) {
        return String.format("""
            🚨 *API MONITORING ALERT* 🚨
            
            *Type:* %s
            *Time:* %s
            
            %s
            
            ---
            _View in Dashboard: <http://localhost:8080|Click here>_
            """,
                request.getAlertType().getDisplayName(),
                LocalDateTime.now(),
                request.getContent()
        );
    }

    private Alert createAlertRecord(NotificationRequest request) {

        Incident incident = null;
        if (request.getIncidentId() != null) {
            incident = incidentRepository.findById(request.getIncidentId())
                    .orElse(null);
        }

        MonitoredApi api = null;
        if (request.getApiId() != null) {
            api = apiRepository.findById(request.getApiId())
                    .orElse(null);
        }

        return Alert.builder()
                .incident(incident)
                .api(api)
                .alertType(request.getAlertType())
                .channel(AlertChannel.WEBHOOK)
                .recipient(request.getRecipient())
                .subject(request.getSubject())
                .content(request.getContent())
                .status(AlertStatus.PENDING)
                .retryCount(0)
                .maxRetries(3)
                .build();
    }

    private Alert createFailedAlert(NotificationRequest request, String error) {
        Alert alert = createAlertRecord(request);
        alert.setStatus(AlertStatus.FAILED);
        alert.setFailedAt(LocalDateTime.now());
        alert.setErrorMessage(error);
        return alertRepository.save(alert);
    }

    @Override
    public AlertChannel getChannel() {
        return AlertChannel.SLACK;
    }

    @Override
    public boolean isValidDestination(String destination) {
        return destination != null && (destination.startsWith("#") || destination.startsWith("@"));
    }
}