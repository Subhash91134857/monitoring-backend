package com.subhash.apimonitoring.notification.controller;


import com.subhash.apimonitoring.common.pagination.PaginationRequest;
import com.subhash.apimonitoring.common.pagination.PaginationResponse;
import com.subhash.apimonitoring.common.response.ApiResponse;
import com.subhash.apimonitoring.notification.dto.AlertDTO;
import com.subhash.apimonitoring.notification.dto.NotificationConfigDTO;
import com.subhash.apimonitoring.notification.dto.NotificationRequest;
import com.subhash.apimonitoring.notification.entity.Alert;
import com.subhash.apimonitoring.notification.entity.NotificationConfig;
import com.subhash.apimonitoring.notification.service.NotificationService;
import com.subhash.apimonitoring.notification.repository.AlertRepository;
import com.subhash.apimonitoring.notification.repository.NotificationConfigRepository;
import com.subhash.apimonitoring.security.util.SecurityUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;
    private final AlertRepository alertRepository;
    private final NotificationConfigRepository configRepository;
    private final SecurityUtils securityUtils;

    /**
     * Send a test notification
     */
    @PostMapping("/test")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<ApiResponse<Void>> sendTestNotification(
            @Valid @RequestBody NotificationRequest request) {

        log.info("REST request to send test notification");
        notificationService.sendNotification(request);
        return ResponseEntity.ok(ApiResponse.success("Test notification sent"));
    }

    /**
     * Get all alerts with pagination
     */
    @GetMapping("/alerts")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'VIEWER')")
    public ResponseEntity<ApiResponse<PaginationResponse<AlertDTO>>> getAllAlerts(
            @Valid PaginationRequest paginationRequest) {

        log.info("REST request to get all alerts");
        Pageable pageable = paginationRequest.toPageable();
        Page<Alert> alerts = alertRepository.findAll(pageable);

        PaginationResponse<AlertDTO> response = PaginationResponse.fromPage(
                alerts.map(this::convertToDTO)
        );

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * Get alerts by incident
     */
    @GetMapping("/alerts/incident/{incidentId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'VIEWER')")
    public ResponseEntity<ApiResponse<List<AlertDTO>>> getAlertsByIncident(@PathVariable Long incidentId) {
        log.info("REST request to get alerts by incident: {}", incidentId);
        List<Alert> alerts = alertRepository.findByIncidentId(incidentId);
        List<AlertDTO> dtos = alerts.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.success(dtos));
    }

    /**
     * Get notification configurations for current user
     */
    @GetMapping("/configs")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<List<NotificationConfigDTO>>> getUserConfigs() {
        log.info("REST request to get user notification configs");
        // In production, get current user ID from security context
       Long user_id= securityUtils.getCurrentUserId();
        List<NotificationConfig> configs = configRepository.findByUserIdAndEnabledTrue(user_id);
        List<NotificationConfigDTO> dtos = configs.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.success(dtos));
    }

    /**
     * Create notification configuration
     */
    @PostMapping("/configs")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<NotificationConfigDTO>> createConfig(
            @Valid @RequestBody NotificationConfigDTO configDTO) {

        log.info("REST request to create notification config");
        NotificationConfig config = convertToEntity(configDTO);
        // In production, set user ID from security context
        Long user_id= securityUtils.getCurrentUserId();
        System.out.println("User Id: "+user_id);
        config.setUserId(user_id);
        NotificationConfig saved = configRepository.save(config);
        return ResponseEntity.ok(ApiResponse.success("Config created", convertToDTO(saved)));
    }

    /**
     * Update notification configuration
     */
    @PutMapping("/configs/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<NotificationConfigDTO>> updateConfig(
            @PathVariable Long id,
            @Valid @RequestBody NotificationConfigDTO configDTO) {

        log.info("REST request to update notification config: {}", id);
        NotificationConfig config = configRepository.findById(id).orElseThrow();
        config.setEnabled(configDTO.getEnabled());
        config.setDestination(configDTO.getDestination());
        config.setSeverityThreshold(configDTO.getSeverityThreshold());
        config.setRateLimitMinutes(configDTO.getRateLimitMinutes());
        config.setRateLimitCount(configDTO.getRateLimitCount());

        NotificationConfig saved = configRepository.save(config);
        return ResponseEntity.ok(ApiResponse.success("Config updated", convertToDTO(saved)));
    }

    /**
     * Delete notification configuration
     */
    @DeleteMapping("/configs/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Void>> deleteConfig(@PathVariable Long id) {
        log.info("REST request to delete notification config: {}", id);
        configRepository.deleteById(id);
        return ResponseEntity.ok(ApiResponse.success("Config deleted"));
    }

    private AlertDTO convertToDTO(Alert alert) {

        return AlertDTO.builder()
                .id(alert.getId())
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
                .apiName(
                        alert.getApi() != null
                                ? alert.getApi().getName()
                                : null
                )
                .alertType(alert.getAlertType())
                .channel(alert.getChannel())
                .recipient(alert.getRecipient())
                .subject(alert.getSubject())
                .content(alert.getContent())
                .status(alert.getStatus())
                .sentAt(alert.getSentAt())
                .deliveredAt(alert.getDeliveredAt())
                .errorMessage(alert.getErrorMessage())
                .retryCount(alert.getRetryCount())
                .build();
    }

    private NotificationConfigDTO convertToDTO(NotificationConfig config) {
        return NotificationConfigDTO.builder()
                .id(config.getId())
                .userId(config.getUserId())
                .teamId(config.getTeamId())
                .channel(config.getChannel())
                .destination(config.getDestination())
                .alertType(config.getAlertType())
                .enabled(config.getEnabled())
                .severityThreshold(config.getSeverityThreshold())
                .rateLimitMinutes(config.getRateLimitMinutes())
                .rateLimitCount(config.getRateLimitCount())
                .build();
    }

    private NotificationConfig convertToEntity(NotificationConfigDTO dto) {
        return NotificationConfig.builder()
                .channel(dto.getChannel())
                .destination(dto.getDestination())
                .alertType(dto.getAlertType())
                .enabled(dto.getEnabled())
                .severityThreshold(dto.getSeverityThreshold())
                .rateLimitMinutes(dto.getRateLimitMinutes())
                .rateLimitCount(dto.getRateLimitCount())
                .build();
    }
}
