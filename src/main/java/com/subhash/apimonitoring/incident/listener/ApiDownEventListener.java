package com.subhash.apimonitoring.incident.listener;

import com.subhash.apimonitoring.event.monitoring.ApiDownEvent;
import com.subhash.apimonitoring.incident.dto.CreateIncidentRequest;
import com.subhash.apimonitoring.incident.dto.IncidentDTO;
import com.subhash.apimonitoring.incident.entity.Incident;
import com.subhash.apimonitoring.incident.enums.IncidentSeverity;
import com.subhash.apimonitoring.incident.service.IncidentService;
import com.subhash.apimonitoring.monitoring.entity.MonitoredApi;
import com.subhash.apimonitoring.monitoring.repository.MonitoredApiRepository;
import com.subhash.apimonitoring.notification.dto.NotificationRequest;
import com.subhash.apimonitoring.notification.enums.AlertChannel;
import com.subhash.apimonitoring.notification.enums.AlertType;
import com.subhash.apimonitoring.notification.factory.AlertStrategyFactory;
import com.subhash.apimonitoring.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class ApiDownEventListener {

    private final IncidentService incidentService;
    private final MonitoredApiRepository apiRepository;
    private final AlertStrategyFactory alertStrategyFactory;
    private final NotificationService notificationService;

    @Value("${app.notification.email.enabled:true}")
    private boolean emailEnabled;

    @Value("${app.notification.email.to:admin@company.com}")
    private String emailRecipient;

    @Value("${app.notification.slack.enabled:false}")
    private boolean slackEnabled;

    @Value("${app.notification.slack.channel:#alerts}")
    private String slackChannel;

    @Value("${app.notification.webhook.enabled:false}")
    private boolean webhookEnabled;

    @Value("${app.notification.webhook.url:}")
    private String webhookUrl;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Async
    @EventListener
    public void handleApiDownEvent(ApiDownEvent event) {
        log.info("Received API DOWN event for API ID: {}", event.getApiId());

        MonitoredApi api = apiRepository.findById(event.getApiId()).orElse(null);
        if (api == null) {
            log.warn("API not found for ID: {}", event.getApiId());
            return;
        }

        log.info("API Details: Name={}, URL={}, Priority={}", api.getName(), api.getUrl(), api.getPriority());

        // Determine severity based on API priority
        IncidentSeverity severity = determineSeverity(api);

        // Build incident creation request
        CreateIncidentRequest request = new CreateIncidentRequest();
        request.setApiId(event.getApiId());
        request.setTitle(String.format("%s is DOWN", api.getName()));
        request.setDescription(buildDescription(api, event));
        request.setSeverity(severity);

        try {
            // 1️ Create the incident
            Incident createdIncident = incidentService.createIncident(request);
            log.info("Incident created: {} with ID: {}",
                    createdIncident.getIncidentNumber(), createdIncident.getId());

            notificationService.sendIncidentNotification(
                    createdIncident,
                    AlertType.INCIDENT_CREATED
            );

            notificationService.sendApiNotification(
                    api,
                    AlertType.API_DOWN,
                    event.getErrorMessage()
            );

        } catch (Exception e) {
            log.error("Failed to create incident for API: {}", api.getName(), e);
        }
    }


    /**
     * Build incident description
     */
    private String buildDescription(MonitoredApi api, ApiDownEvent event) {
        return String.format(
                "Service %s has been detected as DOWN.\n\n" +
                        "URL: %s\n" +
                        "Error: %s\n" +
                        "Consecutive failures: %d\n" +
                        "Time: %s\n\n" +
                        "Please investigate immediately.",
                api.getName(),
                api.getUrl(),
                event.getErrorMessage(),
                event.getConsecutiveFailures(),
                new java.util.Date(event.getTimestamp())
        );
    }

    /**
     * Determine severity based on API priority
     */
    private IncidentSeverity determineSeverity(MonitoredApi api) {
        if (api.getPriority() == null) {
            return IncidentSeverity.MEDIUM;
        }
        return switch (api.getPriority()) {
            case 4 -> IncidentSeverity.CRITICAL;
            case 3 -> IncidentSeverity.HIGH;
            case 2 -> IncidentSeverity.MEDIUM;
            default -> IncidentSeverity.LOW;
        };
    }
}