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
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.time.LocalDateTime;

/**
 * WebSocket Alert Strategy for Real-time Dashboard Updates
 *
 * WHY - Single Responsibility:
 * Pushes real-time notifications to connected dashboard clients
 *
 * RESULT: Instant visual alerts on the dashboard
 */
@Slf4j
@Component
@RequiredArgsConstructor
class WebSocketAlertStrategy implements AlertStrategy {

    private final SimpMessagingTemplate messagingTemplate;
    private final AlertRepository alertRepository;
    private final IncidentRepository incidentRepository;
    private final MonitoredApiRepository apiRepository;

    @Override
    public Alert send(NotificationRequest request) {
        log.info("Sending WebSocket alert to topic: {}", request.getRecipient());

        Alert alert = createAlertRecord(request);

        try {
            // Prepare WebSocket message
            Map<String, Object> wsMessage = new HashMap<>();
            wsMessage.put("type", request.getAlertType().name());
            wsMessage.put("timestamp", LocalDateTime.now().toString());
            wsMessage.put("message", request.getContent());
            wsMessage.put("incidentId", request.getIncidentId());
            wsMessage.put("apiId", request.getApiId());

            if (request.getTemplateData() != null) {
                wsMessage.putAll(request.getTemplateData());
            }

            // Send to WebSocket topic
            String topic = request.getRecipient() != null ?
                    request.getRecipient() : "/topic/alerts";
            messagingTemplate.convertAndSend(topic, wsMessage);

            alert.setStatus(AlertStatus.DELIVERED);
            alert.setSentAt(LocalDateTime.now());
            alert.setDeliveredAt(LocalDateTime.now());

            log.debug("WebSocket alert sent to topic: {}", topic);

        } catch (Exception e) {
            log.error("Failed to send WebSocket alert", e);
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
        return AlertChannel.WEBSOCKET;
    }

    @Override
    public boolean isValidDestination(String destination) {
        return true;  // Always valid for WebSocket
    }
}