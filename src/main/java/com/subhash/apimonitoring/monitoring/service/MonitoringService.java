package com.subhash.apimonitoring.monitoring.service;



import com.subhash.apimonitoring.common.pagination.PaginationResponse;
import com.subhash.apimonitoring.exception.BusinessException;
import com.subhash.apimonitoring.exception.ResourceNotFoundException;
import com.subhash.apimonitoring.monitoring.dto.HealthCheckDTO;
import com.subhash.apimonitoring.monitoring.dto.HealthCheckResult;
import com.subhash.apimonitoring.monitoring.dto.MonitoredApiDTO;
import com.subhash.apimonitoring.monitoring.dto.MonitoringStatsDTO;
import com.subhash.apimonitoring.monitoring.entity.HealthCheck;
import com.subhash.apimonitoring.monitoring.entity.MonitoredApi;
import com.subhash.apimonitoring.monitoring.enums.ApiStatus;
import com.subhash.apimonitoring.monitoring.enums.HealthCheckType;
import com.subhash.apimonitoring.monitoring.enums.HealthStatus;
import com.subhash.apimonitoring.monitoring.enums.HttpMethod;
import com.subhash.apimonitoring.monitoring.mapper.MonitoredApiMapper;
import com.subhash.apimonitoring.monitoring.repository.HealthCheckRepository;
import com.subhash.apimonitoring.monitoring.repository.MonitoredApiRepository;
import com.subhash.apimonitoring.monitoring.specification.HealthCheckSpecification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class MonitoringService {

    private final MonitoredApiRepository apiRepository;
    private final HealthCheckRepository healthCheckRepository;
    private final MonitoredApiMapper apiMapper;

    @Transactional
    public MonitoredApiDTO createApi(MonitoredApiDTO apiDTO) {
        log.info("Creating new monitored API: {}", apiDTO.getName());

        // Check for duplicate name
        if (apiRepository.findByName(apiDTO.getName()).isPresent()) {
            throw new BusinessException("API with name '" + apiDTO.getName() + "' already exists", "DUPLICATE_API_NAME");
        }


        MonitoredApi api = apiMapper.toEntity(apiDTO);
        if (api.getCheckType() == null) {
            api.setCheckType(HealthCheckType.HTTP);
        }
        if (api.getMethod() == null) {
            api.setMethod(HttpMethod.GET);
        }
        api.setCurrentStatus(ApiStatus.UNKNOWN);
        api.setTotalChecks(0L);
        api.setFailedChecks(0L);
        api.setUptimePercentage(100.0);
        MonitoredApi savedApi = apiRepository.save(api);
        log.info("Monitored API created with ID: {}", savedApi.getId());

        return apiMapper.toDto(savedApi);
    }

    @Transactional(readOnly = true)
    public MonitoredApiDTO getApiById(Long id) {
        MonitoredApi api = apiRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Monitored API", id));
        return apiMapper.toDto(api);
    }

    @Transactional(readOnly = true)
    public PaginationResponse<MonitoredApiDTO> getAllApis(Pageable pageable) {
        Page<MonitoredApi> apis = apiRepository.findAll(pageable);
        return PaginationResponse.fromPage(apis.map(apiMapper::toDto));
    }

    @Transactional(readOnly = true)
    public PaginationResponse<MonitoredApiDTO> getActiveApis(Pageable pageable) {
        Page<MonitoredApi> apis = apiRepository.findByIsActiveTrue(pageable);
        return PaginationResponse.fromPage(apis.map(apiMapper::toDto));
    }

    @Transactional
    public MonitoredApiDTO updateApi(Long id, MonitoredApiDTO apiDTO) {
        MonitoredApi api = apiRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Monitored API", id));

        api.setName(apiDTO.getName());
        api.setUrl(apiDTO.getUrl());
        api.setHealthEndpoint(apiDTO.getHealthEndpoint());
        if (api.getCheckType() == null) {
            api.setCheckType(HealthCheckType.HTTP);
        }
        if (api.getMethod() == null) {
            api.setMethod(HttpMethod.GET);
        }
        api.setExpectedResponseBody(apiDTO.getExpectedResponseBody());
        api.setExpectedStatusCode(apiDTO.getExpectedStatusCode());
        api.setCustomHeaders(apiDTO.getCustomHeaders());
        api.setCheckInterval(apiDTO.getCheckInterval());
        api.setTimeout(apiDTO.getTimeout());
        api.setRetryCount(apiDTO.getRetryCount());
        api.setIsActive(apiDTO.getIsActive());
        api.setDescription(apiDTO.getDescription());
        api.setCategory(apiDTO.getCategory());
        api.setPriority(apiDTO.getPriority());

        MonitoredApi updatedApi = apiRepository.save(api);
        log.info("Monitored API updated: {}", updatedApi.getName());

        return apiMapper.toDto(updatedApi);
    }

    @Transactional
    public void deleteApi(Long id) {
        MonitoredApi api = apiRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Monitored API", id));

        apiRepository.delete(api);
        log.info("Monitored API deleted: {}", api.getName());
    }

    @Transactional
    public void toggleApiStatus(Long id, boolean active) {
        MonitoredApi api = apiRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Monitored API", id));

        api.setIsActive(active);
        apiRepository.save(api);
        log.info("API {} status toggled to active={}", api.getName(), active);
    }

    @Transactional(readOnly = true)
    public List<HealthCheckDTO> getHealthHistory(Long apiId, int limit) {
        MonitoredApi api = apiRepository.findById(apiId)
                .orElseThrow(() -> new ResourceNotFoundException("Monitored API", apiId));

        Pageable pageable = Pageable.ofSize(limit);
        List<HealthCheck> healthChecks = healthCheckRepository.findByApiIdOrderByCheckedAtDesc(apiId, pageable);

        return healthChecks.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    @Transactional(readOnly = true)
    public PaginationResponse<HealthCheckDTO> getAllHealthHistory(
            Long apiId,
            String status,
            LocalDateTime startDate,
            LocalDateTime endDate,
            Pageable pageable) {

        log.info("Fetching health history with filters - apiId: {}, status: {}, startDate: {}, endDate: {}",
                apiId, status, startDate, endDate);

        // Validate API if apiId is provided
        if (apiId != null) {
            apiRepository.findById(apiId)
                    .orElseThrow(() -> new ResourceNotFoundException("Monitored API", apiId));
        }

        // Parse status enum
        HealthStatus healthStatus = null;
        if (status != null && !status.isEmpty()) {
            try {
                healthStatus = HealthStatus.valueOf(status.toUpperCase());
            } catch (IllegalArgumentException e) {
                log.warn("Invalid status value: {}, ignoring filter", status);
            }
        }

        // ⭐ Build specification using the fluent API
        Specification<HealthCheck> spec = Specification
                .where(HealthCheckSpecification.byApiId(apiId))
                .and(HealthCheckSpecification.byStatus(healthStatus))
                .and(HealthCheckSpecification.fromDate(startDate))
                .and(HealthCheckSpecification.toDate(endDate));

        // Execute query with specification and pagination
        Page<HealthCheck> healthCheckPage = healthCheckRepository.findAll(spec, pageable);

        // Convert to DTOs
        Page<HealthCheckDTO> dtoPage = healthCheckPage.map(this::convertToHealthCheckDTO);

        return PaginationResponse.fromPage(dtoPage);
    }

    private HealthCheckDTO convertToHealthCheckDTO(HealthCheck healthCheck) {
        return HealthCheckDTO.builder()
                .id(healthCheck.getId())
                .apiId(healthCheck.getApi() != null ? healthCheck.getApi().getId() : null)
                .apiName(healthCheck.getApi() != null ? healthCheck.getApi().getName() : null)
                .status(healthCheck.getStatus())
                .responseTimeMs(healthCheck.getResponseTimeMs())
                .statusCode(healthCheck.getStatusCode())
                .responseBody(healthCheck.getResponseBody())
                .errorMessage(healthCheck.getErrorMessage())
                .checkedAt(healthCheck.getCheckedAt())
                .checkedBy(healthCheck.getCheckedBy())
                .retryCount(healthCheck.getRetryCount())
                .build();
    }

    @Transactional(readOnly = true)
    public MonitoringStatsDTO getMonitoringStats() {
        long totalApis = apiRepository.count();
        long activeApis = apiRepository.countByIsActiveTrue();
        long upApis = apiRepository.countByCurrentStatus(ApiStatus.UP);
        long downApis = apiRepository.countByCurrentStatus(ApiStatus.DOWN);
        long degradedApis = apiRepository.countByCurrentStatus(ApiStatus.DEGRADED);

        // Calculate overall uptime
        double overallUptime = 0.0;
        if (totalApis > 0) {
            overallUptime = (double) upApis / totalApis * 100;
        }

        // Calculate average response time
        LocalDateTime lastHour = LocalDateTime.now().minusHours(1);
        Double avgResponseTime = healthCheckRepository.getAverageResponseTime(null, lastHour)
                .orElse(0.0);

        // Category distribution
        var categoryDistribution = apiRepository.findAll().stream()
                .filter(api -> api.getCategory() != null)
                .collect(Collectors.groupingBy(
                        MonitoredApi::getCategory,
                        Collectors.counting()
                ));

        return MonitoringStatsDTO.builder()
                .totalApis(totalApis)
                .activeApis(activeApis)
                .upApis(upApis)
                .downApis(downApis)
                .degradedApis(degradedApis)
                .overallUptime(overallUptime)
                .averageResponseTime(avgResponseTime)
                .categoryDistribution(categoryDistribution)
                .build();
    }

    private HealthCheckDTO convertToDTO(HealthCheck healthCheck) {
        return HealthCheckDTO.builder()
                .id(healthCheck.getId())
                .apiId(healthCheck.getApi().getId())
                .apiName(healthCheck.getApi().getName())
                .status(healthCheck.getStatus())
                .responseTimeMs(healthCheck.getResponseTimeMs())
                .statusCode(healthCheck.getStatusCode())
                .errorMessage(healthCheck.getErrorMessage())
                .checkedAt(healthCheck.getCheckedAt())
                .checkedBy(healthCheck.getCheckedBy())
                .build();
    }



}