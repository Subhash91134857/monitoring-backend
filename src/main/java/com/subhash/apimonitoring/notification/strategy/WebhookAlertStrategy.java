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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class WebhookAlertStrategy implements AlertStrategy {

    private final AlertRepository alertRepository;
    private final RestTemplate restTemplate;
    private final IncidentRepository incidentRepository;
    private final MonitoredApiRepository apiRepository;

    @Value("${app.notification.webhook.timeout:10000}")
    private int timeout;

    @Override
    public Alert send(NotificationRequest request) {
        log.info("Sending webhook alert to: {}", request.getRecipient());

        Alert alert = createAlertRecord(request);

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            Map<String, Object> payload = new HashMap<>();
            payload.put("alertType", request.getAlertType().name());
            payload.put("timestamp", LocalDateTime.now().toString());
            payload.put("content", request.getContent());
            payload.put("incidentId", request.getIncidentId());
            payload.put("apiId", request.getApiId());

            if (request.getTemplateData() != null) {
                payload.putAll(request.getTemplateData());
            }

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(payload, headers);

            ResponseEntity<String> response = restTemplate.exchange(
                    request.getRecipient(),
                    HttpMethod.POST,
                    entity,
                    String.class
            );

            if (response.getStatusCode().is2xxSuccessful()) {
                alert.setStatus(AlertStatus.SENT);
                alert.setSentAt(LocalDateTime.now());
                log.info("Webhook alert sent successfully");
            } else {
                alert.setStatus(AlertStatus.FAILED);
                alert.setFailedAt(LocalDateTime.now());
                alert.setErrorMessage("HTTP " + response.getStatusCodeValue());
            }

        } catch (Exception e) {
            log.error("Failed to send webhook alert", e);
            alert.setStatus(AlertStatus.FAILED);
            alert.setFailedAt(LocalDateTime.now());
            alert.setErrorMessage(e.getMessage());
        }

        return alertRepository.save(alert);
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

    @Override
    public AlertChannel getChannel() {
        return AlertChannel.WEBHOOK;
    }

    @Override
    public boolean isValidDestination(String destination) {
        return destination != null &&
                (destination.startsWith("http://") || destination.startsWith("https://"));
    }
}