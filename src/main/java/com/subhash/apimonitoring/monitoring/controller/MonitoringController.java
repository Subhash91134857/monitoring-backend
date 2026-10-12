package com.subhash.apimonitoring.monitoring.controller;


import com.subhash.apimonitoring.common.pagination.PaginationRequest;
import com.subhash.apimonitoring.common.pagination.PaginationResponse;
import com.subhash.apimonitoring.common.response.ApiResponse;
import com.subhash.apimonitoring.monitoring.dto.HealthCheckDTO;
import com.subhash.apimonitoring.monitoring.dto.HealthCheckResult;
import com.subhash.apimonitoring.monitoring.dto.MonitoredApiDTO;
import com.subhash.apimonitoring.monitoring.dto.MonitoringStatsDTO;
import com.subhash.apimonitoring.monitoring.service.MonitoringService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/monitoring")
@RequiredArgsConstructor
public class MonitoringController {

    private final MonitoringService monitoringService;

    // ========== API Management Endpoints ==========

    @PostMapping("/apis")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<ApiResponse<MonitoredApiDTO>> createApi(
            @Valid @RequestBody MonitoredApiDTO apiDTO) {
        log.info("REST request to create monitored API: {}", apiDTO.getName());
        MonitoredApiDTO createdApi = monitoringService.createApi(apiDTO);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("API created successfully", createdApi));
    }

    @GetMapping("/apis")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'VIEWER')")
    public ResponseEntity<ApiResponse<PaginationResponse<MonitoredApiDTO>>> getAllApis(
            @Valid PaginationRequest paginationRequest) {

        log.info("REST request to get all monitored APIs");
        PaginationResponse<MonitoredApiDTO> apis = monitoringService.getAllApis(paginationRequest.toPageable());
        return ResponseEntity.ok(ApiResponse.success(apis));
    }

    @GetMapping("/apis/active")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'VIEWER')")
    public ResponseEntity<ApiResponse<PaginationResponse<MonitoredApiDTO>>> getActiveApis(
            @Valid PaginationRequest paginationRequest) {

        log.info("REST request to get active monitored APIs");
        PaginationResponse<MonitoredApiDTO> apis = monitoringService.getActiveApis(paginationRequest.toPageable());
        return ResponseEntity.ok(ApiResponse.success(apis));
    }

    @GetMapping("/apis/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'VIEWER')")
    public ResponseEntity<ApiResponse<MonitoredApiDTO>> getApiById(@PathVariable Long id) {
        log.info("REST request to get API by id: {}", id);
        MonitoredApiDTO api = monitoringService.getApiById(id);
        return ResponseEntity.ok(ApiResponse.success(api));
    }

    @PutMapping("/apis/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<ApiResponse<MonitoredApiDTO>> updateApi(
            @PathVariable Long id,
            @Valid @RequestBody MonitoredApiDTO apiDTO) {

        log.info("REST request to update API: {}", id);
        MonitoredApiDTO updatedApi = monitoringService.updateApi(id, apiDTO);
        return ResponseEntity.ok(ApiResponse.success("API updated successfully", updatedApi));
    }

    @DeleteMapping("/apis/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteApi(@PathVariable Long id) {
        log.info("REST request to delete API: {}", id);
        monitoringService.deleteApi(id);
        return ResponseEntity.ok(ApiResponse.success("API deleted successfully"));
    }

    @PatchMapping("/apis/{id}/toggle")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<ApiResponse<Void>> toggleApiStatus(
            @PathVariable Long id,
            @RequestParam boolean active) {

        log.info("REST request to toggle API status: {} active={}", id, active);
        monitoringService.toggleApiStatus(id, active);
        return ResponseEntity.ok(ApiResponse.success(
                active ? "API activated successfully" : "API deactivated successfully"));
    }

    // ========== Health History Endpoints ==========

    @GetMapping("/apis/{id}/history")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'VIEWER')")
    public ResponseEntity<ApiResponse<List<HealthCheckDTO>>> getHealthHistory(
            @PathVariable Long id,
            @RequestParam(defaultValue = "100") int limit) {
        log.info("REST request to get health history for API: {}", id);
        List<HealthCheckDTO> history = monitoringService.getHealthHistory(id, limit);
        return ResponseEntity.ok(ApiResponse.success(history));
    }

    @GetMapping("/apis/health-history")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'VIEWER')")
    public ResponseEntity<ApiResponse<PaginationResponse<HealthCheckDTO>>> getAllHealthHistory(
            @RequestParam(required = false) Long apiId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @Valid PaginationRequest paginationRequest) {
        log.info("REST request to get health history with filters");
        PaginationResponse<HealthCheckDTO> history = monitoringService.getAllHealthHistory(
                apiId, status, startDate, endDate,
                paginationRequest.toPageable());

        return ResponseEntity.ok(ApiResponse.success(history));
    }


    // ========== Statistics Endpoints ==========

    @GetMapping("/stats")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'VIEWER')")
    public ResponseEntity<ApiResponse<MonitoringStatsDTO>> getMonitoringStats() {
        log.info("REST request to get monitoring statistics");
        MonitoringStatsDTO stats = monitoringService.getMonitoringStats();
        return ResponseEntity.ok(ApiResponse.success(stats));
    }
}