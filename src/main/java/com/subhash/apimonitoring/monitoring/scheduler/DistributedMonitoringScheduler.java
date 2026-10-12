package com.subhash.apimonitoring.monitoring.scheduler;



import com.subhash.apimonitoring.monitoring.engine.MonitoringEngine;
import com.subhash.apimonitoring.monitoring.repository.MonitoredApiRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * Distributed Monitoring Scheduler
 *
 * WHY - Single Responsibility:
 * Handles ONLY scheduling coordination across multiple instances
 * Uses Redis distributed lock to prevent duplicate executions
 *
 * HOW:
 * - Acquires Redis lock before executing
 * - Only one instance executes at a time
 * - Automatic lock expiration prevents deadlocks
 *
 * RESULT: Reliable monitoring without duplicate checks in clustered environment
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DistributedMonitoringScheduler {

    private final RedisTemplate<String, String> redisTemplate;
    private final MonitoringEngine monitoringEngine;
    private final MonitoredApiRepository apiRepository;

    @Value("${app.monitoring.scheduler.lock-lease-time:55}")
    private long lockLeaseTime;

    private static final String SCHEDULER_LOCK_KEY = "monitoring:scheduler:lock";
    private final String instanceId = UUID.randomUUID().toString();

    @Scheduled(fixedDelayString = "${app.monitoring.scheduler.fixed-rate:60000}",
            initialDelayString = "${app.monitoring.scheduler.initial-delay:5000}")
    public void executeMonitoring() {
        boolean lockAcquired = false;

        try {
            // Try to acquire distributed lock
            lockAcquired = Boolean.TRUE.equals(
                    redisTemplate.opsForValue()
                            .setIfAbsent(SCHEDULER_LOCK_KEY, instanceId, Duration.ofSeconds(lockLeaseTime))
            );

            if (lockAcquired) {
                log.info("Instance {} acquired scheduler lock, starting monitoring cycle", instanceId);
                performMonitoringCycle();
            } else {
                log.debug("Instance {} did not acquire lock, skipping this cycle", instanceId);
            }

        } catch (Exception e) {
            log.error("Error in monitoring scheduler", e);
        } finally {
            if (lockAcquired) {
                releaseLock();
            }
        }
    }

    private void performMonitoringCycle() {
        long startTime = System.currentTimeMillis();

        try {
            // Get all active APIs
            var activeApis = apiRepository.findByIsActiveTrue();
            log.info("Starting monitoring cycle for {} APIs", activeApis.size());

            if (activeApis.isEmpty()) {
                log.debug("No active APIs to monitor");
                return;
            }

            // Execute monitoring
            monitoringEngine.monitorAllApis(activeApis);

            long duration = System.currentTimeMillis() - startTime;
            log.info("Monitoring cycle completed in {}ms", duration);

        } catch (Exception e) {
            log.error("Monitoring cycle failed", e);
        }
    }

    private void releaseLock() {
        try {
            String currentLockValue = redisTemplate.opsForValue().get(SCHEDULER_LOCK_KEY);
            if (instanceId.equals(currentLockValue)) {
                redisTemplate.delete(SCHEDULER_LOCK_KEY);
                log.debug("Instance {} released scheduler lock", instanceId);
            }
        } catch (Exception e) {
            log.error("Failed to release scheduler lock", e);
        }
    }
}