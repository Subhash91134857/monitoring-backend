package com.subhash.apimonitoring.incident.listener;

import com.subhash.apimonitoring.event.monitoring.ApiRecoveredEvent;
import com.subhash.apimonitoring.incident.entity.Incident;
import com.subhash.apimonitoring.incident.service.IncidentService;
import com.subhash.apimonitoring.notification.enums.AlertType;
import com.subhash.apimonitoring.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class ApiRecoveredEventListener {
    private final IncidentService incidentService;
    private final NotificationService notificationService;
    @Value("${app.incident.auto-resolve.enabled:true}")
    private boolean autoResolveEnabled;

    @Async
    @EventListener
    public void handleApiRecoveredEvent(ApiRecoveredEvent event) {
        log.info("Received API RECOVERED event for API ID: {}", event.getApiId());
        log.info("API: {} was down for {} ms ({} minutes)",
                event.getApiName(),
                event.getDownDurationMs(),
                event.getDownDurationMs() / 60000);
        try {
            if (autoResolveEnabled) {
                List<Incident> resolvedIncidents =
                        incidentService.autoResolveIncident(event.getApiId());
                for (Incident incident : resolvedIncidents) {
                    notificationService.sendIncidentNotification(
                            incident,
                            AlertType.INCIDENT_RESOLVED
                    );
                }
            }
        } catch (Exception e) {
            log.error("Failed processing recovery event", e);
        }
    }

}