package com.subhash.apimonitoring.notification.service;


import com.subhash.apimonitoring.exception.BusinessException;
import com.subhash.apimonitoring.incident.entity.Incident;
import com.subhash.apimonitoring.incident.enums.IncidentSeverity;
import com.subhash.apimonitoring.incident.repository.IncidentRepository;
import com.subhash.apimonitoring.monitoring.entity.MonitoredApi;
import com.subhash.apimonitoring.monitoring.repository.MonitoredApiRepository;
import com.subhash.apimonitoring.notification.dto.NotificationRequest;
import com.subhash.apimonitoring.notification.entity.Alert;
import com.subhash.apimonitoring.notification.entity.NotificationConfig;
import com.subhash.apimonitoring.notification.enums.AlertChannel;
import com.subhash.apimonitoring.notification.enums.AlertStatus;
import com.subhash.apimonitoring.notification.enums.AlertType;
import com.subhash.apimonitoring.notification.factory.AlertStrategyFactory;
import com.subhash.apimonitoring.notification.repository.AlertRepository;
import com.subhash.apimonitoring.notification.repository.NotificationConfigRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {

    private final AlertStrategyFactory strategyFactory;
    private final AlertRepository alertRepository;
    private final NotificationConfigRepository configRepository;
    private final IncidentRepository incidentRepository;
    private final MonitoredApiRepository apiRepository;

    /**
     * Send notification asynchronously
     */
    @Async
    @Transactional
    public CompletableFuture<Alert> sendNotification(NotificationRequest request) {
        log.info("Sending notification via {} to {}", request.getChannel(), request.getRecipient());

        return CompletableFuture.supplyAsync(() -> {
            var strategy = strategyFactory.getStrategy(request.getChannel());

            // Validate destination
            if (!strategy.isValidDestination(request.getRecipient())) {
                throw new BusinessException(
                        "Invalid destination for channel: " + request.getChannel(),
                        "INVALID_DESTINATION"
                );
            }

            // Check rate limiting
            if (isRateLimited(request.getRecipient(), request.getChannel())) {
                log.warn("Rate limit exceeded for {} via {}", request.getRecipient(), request.getChannel());
                return createRateLimitedAlert(request);
            }

            // Send notification
            return strategy.send(request);
        });
    }

    /**
     * Send incident notification to all configured recipients
     */
    @Async
    public void sendIncidentNotification(Incident incident, AlertType alertType) {
        log.info("Sending incident notification for incident: {}", incident.getIncidentNumber());

        // Get all configured recipients for this severity level
        List<NotificationConfig> configs = configRepository.findAll();

        for (NotificationConfig config : configs) {
            // Check severity threshold
            if (config.getSeverityThreshold() != null) {
                IncidentSeverity threshold = IncidentSeverity.valueOf(config.getSeverityThreshold());
                if (incident.getSeverity().compareTo(threshold) > 0) {
                    continue;  // Skip if severity is lower than threshold
                }
            }

            // Check alert type filter
            if (config.getAlertType() != null && config.getAlertType() != alertType) {
                continue;
            }

            // Build notification request
            NotificationRequest request = NotificationRequest.builder()
                    .alertType(alertType)
                    .channel(config.getChannel())
                    .recipient(config.getDestination())
                    .subject(buildSubject(incident, alertType))
                    .content(buildContent(incident, alertType))
                    .incidentId(incident.getId())
                    .apiId(incident.getApi().getId())
                    .templateData(buildTemplateData(incident, alertType))
                    .build();

            // Send notification asynchronously
            sendNotification(request);
        }
    }

    /**
     * Send API status notification
     */
    @Async
    public void sendApiNotification(MonitoredApi api, AlertType alertType, String errorMessage) {
        log.info("Sending API notification for API: {}", api.getName());

        NotificationRequest request = NotificationRequest.builder()
                .alertType(alertType)
                .channel(AlertChannel.WEBSOCKET)  // Always send to dashboard
                .recipient("/topic/alerts")
                .subject(String.format("%s: %s", alertType.getDisplayName(), api.getName()))
                .content(buildApiContent(api, alertType, errorMessage))
                .apiId(api.getId())
                .templateData(buildApiTemplateData(api, alertType, errorMessage))
                .build();

        sendNotification(request);
    }

    /**
     * Retry failed alerts
     */
    @Scheduled(fixedDelay = 60000)  // Every minute
    @Transactional
    public void retryFailedAlerts() {
        List<Alert> failedAlerts = alertRepository.findByStatusAndNextRetryAtBefore(
                AlertStatus.RETRYING, LocalDateTime.now());

        for (Alert alert : failedAlerts) {
            if (alert.getRetryCount() >= alert.getMaxRetries()) {
                alert.setStatus(AlertStatus.FAILED);
                alertRepository.save(alert);
                continue;
            }

            log.info("Retrying alert {} (attempt {}/{})",
                    alert.getId(), alert.getRetryCount() + 1, alert.getMaxRetries());

            NotificationRequest request = NotificationRequest.builder()
                    .alertType(alert.getAlertType())
                    .channel(alert.getChannel())
                    .recipient(alert.getRecipient())
                    .subject(alert.getSubject())
                    .content(alert.getContent())
                    .incidentId(
                            alert.getIncident() != null
                                    ? alert.getIncident().getId()
                                    : null
                    )
                    .apiId(
                            alert.getApi() != null
                                    ? alert.getApi().getId()
                                    : null
                    )
                    .build();

            var strategy = strategyFactory.getStrategy(alert.getChannel());
            Alert result = strategy.send(request);

            alert.setStatus(result.getStatus());
            alert.setSentAt(result.getSentAt());
            alert.setErrorMessage(result.getErrorMessage());
            alert.setRetryCount(alert.getRetryCount() + 1);

            if (alert.getStatus() == AlertStatus.FAILED) {
                // Schedule next retry with exponential backoff
                long delay = (long) Math.pow(2, alert.getRetryCount()) * 60;  // 2^retry minutes
                alert.setNextRetryAt(LocalDateTime.now().plusSeconds(delay));
            }

            alertRepository.save(alert);
        }
    }

    /**
     * Send reminders for open incidents
     */
    @Scheduled(fixedDelay = 1800000)  // Every 30 minutes
    public void sendOpenIncidentReminders() {
        List<Incident> openIncidents = incidentRepository.findAllActiveIncidents();

        for (Incident incident : openIncidents) {
            // Check if reminder already sent recently
            if (incident.getReminderSentAt() != null &&
                    incident.getReminderSentAt().isAfter(LocalDateTime.now().minusMinutes(30))) {
                continue;
            }

            sendIncidentNotification(incident, AlertType.REMINDER);
            incident.setReminderSentAt(LocalDateTime.now());
            incidentRepository.save(incident);
        }
    }

    private boolean isRateLimited(String recipient, AlertChannel channel) {
        LocalDateTime oneHourAgo = LocalDateTime.now().minusHours(1);
        long count = alertRepository.countByRecipientAndCreatedAtAfter(recipient, oneHourAgo);

        // Get rate limit from config (default: 10 per hour)
        List<NotificationConfig> configs = configRepository.findByChannelAndEnabledTrue(channel);
        int rateLimit = configs.stream()
                .findFirst()
                .map(NotificationConfig::getRateLimitCount)
                .orElse(10);

        return count >= rateLimit;
    }

    private Alert createRateLimitedAlert(NotificationRequest request) {
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
                .channel(request.getChannel())
                .recipient(request.getRecipient())
                .subject(request.getSubject())
                .content(request.getContent())
                .status(AlertStatus.FAILED)
                .failedAt(LocalDateTime.now())
                .errorMessage("Rate limit exceeded")
                .build();
    }

    private String buildSubject(Incident incident, AlertType alertType) {
        return String.format("[%s] %s - %s",
                incident.getSeverity().getCode(),
                alertType.getDisplayName(),
                incident.getTitle()
        );
    }

    private String buildContent(Incident incident, AlertType alertType) {
        return String.format("""
            Incident: %s
            Status: %s
            Severity: %s
            Started: %s
            API: %s
            """,
                incident.getIncidentNumber(),
                incident.getStatus().getDisplayName(),
                incident.getSeverity().getDisplayName(),
                incident.getStartedAt(),
                incident.getApi().getName()
        );
    }

    private Map<String, Object> buildTemplateData(Incident incident, AlertType alertType) {
        Map<String, Object> data = new HashMap<>();
        data.put("incident", incident);
        data.put("incidentNumber", incident.getIncidentNumber());
        data.put("apiName", incident.getApi().getName());
        data.put("alertType", alertType);
        data.put("dashboardUrl", "http://localhost:8080/incidents/" + incident.getId());
        return data;
    }

    private String buildApiContent(MonitoredApi api, AlertType alertType, String errorMessage) {
        return String.format("""
            API: %s
            Status: %s
            URL: %s
            Error: %s
            Time: %s
            """,
                api.getName(),
                alertType.getDisplayName(),
                api.getUrl(),
                errorMessage != null ? errorMessage : "No error details",
                LocalDateTime.now()
        );
    }

    private Map<String, Object> buildApiTemplateData(MonitoredApi api, AlertType alertType, String errorMessage) {
        Map<String, Object> data = new HashMap<>();
        data.put("api", api);
        data.put("apiName", api.getName());
        data.put("alertType", alertType);
        data.put("errorMessage", errorMessage);
        data.put("timestamp", LocalDateTime.now());
        data.put("dashboardUrl", "http://localhost:8080/apis/" + api.getId());
        return data;
    }
}