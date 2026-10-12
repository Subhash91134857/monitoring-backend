package com.subhash.apimonitoring.monitoring.engine;


import com.subhash.apimonitoring.event.monitoring.ApiDownEvent;
import com.subhash.apimonitoring.event.monitoring.ApiRecoveredEvent;
import com.subhash.apimonitoring.exception.ResourceNotFoundException;
import com.subhash.apimonitoring.monitoring.dto.HealthCheckResult;
import com.subhash.apimonitoring.monitoring.entity.HealthCheck;
import com.subhash.apimonitoring.monitoring.entity.MonitoredApi;
import com.subhash.apimonitoring.monitoring.enums.ApiStatus;
import com.subhash.apimonitoring.monitoring.enums.HealthStatus;
import com.subhash.apimonitoring.monitoring.factory.HealthCheckStrategyFactory;
import com.subhash.apimonitoring.monitoring.repository.HealthCheckRepository;
import com.subhash.apimonitoring.monitoring.repository.MonitoredApiRepository;
import com.subhash.apimonitoring.monitoring.strategy.HealthCheckStrategy;
import com.subhash.apimonitoring.monitoring.thread.MonitoringThreadPool;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.context.ApplicationEventPublisher;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

/**
 * Monitoring Engine Core
 *
 * WHY - Single Responsibility:
 * Orchestrates the entire monitoring process
 * Coordinates strategy execution and result storage
 *
 * HOW:
 * - Uses thread pool for concurrent API checks
 * - Processes results and updates API status
 * - Triggers events for downstream processing
 *
 * RESULT: High-performance, concurrent monitoring system
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class MonitoringEngine {

    private final HealthCheckStrategyFactory strategyFactory;
    private final HealthCheckRepository healthCheckRepository;
    private final MonitoredApiRepository apiRepository;
    private final MonitoringThreadPool threadPool;
    private final ApplicationEventPublisher eventPublisher;

    @Value("${app.monitoring.health-check.default-retry-count:2}")
    private int defaultRetryCount;

    private final String instanceId = UUID.randomUUID().toString();

    /**
     * Monitor multiple APIs concurrently
     */
    public void monitorAllApis(List<MonitoredApi> apis) {
        Executor executor = threadPool.monitoringExecutor();

        List<CompletableFuture<Void>> futures = apis.stream()
                .map(api -> CompletableFuture.runAsync(() -> monitorApi(api), executor))
                .toList();

        // Wait for all monitoring tasks to complete
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                .join();
    }

    /**
     * Monitor a single API with retry logic
     */
    @Transactional
    public void monitorApi(MonitoredApi api) {
        log.debug("Monitoring API: {}", api.getName());

        HealthCheckResult result = null;
        int maxRetries = api.getRetryCount() != null ? api.getRetryCount() : defaultRetryCount;

        // Attempt health check with retries
        for (int attempt = 1; attempt <= maxRetries + 1; attempt++) {
            try {
                HealthCheckStrategy strategy = strategyFactory.getStrategy(api);
                result = strategy.check(api);
                result.setRetryCount(attempt - 1);

                // If successful, break retry loop
                if (result.isSuccess()) {
                    break;
                }

                // If not successful and we have retries left, wait before retry
                if (attempt <= maxRetries) {
                    log.debug("Retry {}/{} for API {}", attempt, maxRetries, api.getName());
                    Thread.sleep(1000L * attempt); // Exponential backoff
                }

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.error("Monitoring interrupted for API {}", api.getName());
                break;
            } catch (Exception e) {
                log.error("Unexpected error monitoring API {}", api.getName(), e);
                if (result == null) {
                    result = HealthCheckResult.builder()
                            .apiId(api.getId())
                            .apiName(api.getName())
                            .status(HealthStatus.DOWN)
                            .errorMessage(e.getMessage())
                            .isSuccess(false)
                            .timestamp(System.currentTimeMillis())
                            .build();
                }
            }
        }

        if (result != null) {
            processHealthCheckResult(api, result);
        }
    }

    /**
     * Process health check result and update API status
     */
    private void processHealthCheckResult(MonitoredApi api, HealthCheckResult result) {
        MonitoredApi freshApi = apiRepository.findById(api.getId())
                .orElseThrow(() -> new ResourceNotFoundException("API", api.getId()));

        // Save health check log snapshot
        HealthCheck healthCheck = HealthCheck.builder()
                .api(freshApi)
                .status(result.getStatus())
                .responseTimeMs(result.getResponseTimeMs())
                .statusCode(result.getStatusCode())
                .responseBody(truncateBody(result.getResponseBody()))
                .errorMessage(result.getErrorMessage())
                .checkedAt(LocalDateTime.now())
                .checkedBy(instanceId)
                .retryCount(result.getRetryCount())
                .build();

        healthCheckRepository.save(healthCheck);

        // 1. Calculate statistics in memory
        updateApiStatistics(freshApi, result);

        // 2. Determine status state changes in memory
        updateApiStatus(freshApi, result);

        // === FIX: Persist ALL structural updates exactly once ===
        apiRepository.save(freshApi);

        log.info("Health check completed for {}: {} ({}ms)",
                freshApi.getName(), result.getStatus(), result.getResponseTimeMs());
    }

    /**
     /**
     * Update API statistics (In-Memory Only)
     */
    private void updateApiStatistics(MonitoredApi api, HealthCheckResult result) {
        api.setTotalChecks(
                (api.getTotalChecks() == null ? 0 : api.getTotalChecks()) + 1
        );
        api.setLastCheckTime(LocalDateTime.now());
        api.setLastResponseTimeMs(result.getResponseTimeMs());

        if (!result.isSuccess()) {
            api.setFailedChecks(
                    (api.getFailedChecks() == null ? 0 : api.getFailedChecks()) + 1
            );
            api.setConsecutiveFailures(
                    (api.getConsecutiveFailures() == null ? 0 : api.getConsecutiveFailures()) + 1
            );
            api.setLastError(result.getErrorMessage());

        } else {
            api.setConsecutiveFailures(0);
            api.setLastError(null);
        }
        double uptime =
                ((double) (api.getTotalChecks() - api.getFailedChecks())
                        / api.getTotalChecks()) * 100;

        api.setUptimePercentage(uptime);
    }

    /**
     * Update API status based on health check results (In-Memory Only)
     */
    private void updateApiStatus(MonitoredApi api, HealthCheckResult result) {
        System.out.println("UpdateAPI status: " +api);
        ApiStatus oldStatus = api.getCurrentStatus();
        ApiStatus newStatus;

        if (result.isSuccess()) {
            if (result.getStatus() == HealthStatus.UP) {
                newStatus = ApiStatus.UP;
            } else if (result.getStatus() == HealthStatus.DEGRADED) {
                newStatus = ApiStatus.DEGRADED;
            } else {
                newStatus = ApiStatus.UP;
            }
        } else {
            if (api.getConsecutiveFailures() >= 3) {
                newStatus = ApiStatus.DOWN;
            } else {
                newStatus = ApiStatus.DEGRADED;
            }
        }

        if (oldStatus != newStatus) {
            log.info("API {} status changed from {} to {}", api.getName(), oldStatus, newStatus);

            //  PUBLISH EVENT - Status changed to DOWN
            if (newStatus == ApiStatus.DOWN) {
                ApiDownEvent event = new ApiDownEvent(
                        this,
                        api,
                        result.getErrorMessage(),
                        api.getConsecutiveFailures()
                );
                eventPublisher.publishEvent(event);
                log.info("Published ApiDownEvent for API: {}", api.getName());
            }

            //  PUBLISH EVENT - Status changed from DOWN to UP (Recovery)
            if (oldStatus == ApiStatus.DOWN) {
                long downDuration = calculateDownDuration(api);
                ApiRecoveredEvent event = new ApiRecoveredEvent(this, api, downDuration);
                eventPublisher.publishEvent(event);
                log.info("Published ApiRecoveredEvent for API: {}", api.getName());
            }
        }

        api.setCurrentStatus(newStatus);
    }

    private long calculateDownDuration(MonitoredApi api) {
        return 0;
    }
    private String truncateBody(String body) {
        if (body == null || body.length() <= 1000) {
            return body;
        }
        return body.substring(0, 1000) + "... [truncated]";
    }
}