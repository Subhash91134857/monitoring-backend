package com.subhash.apimonitoring.monitoring.factory;

import com.subhash.apimonitoring.monitoring.entity.MonitoredApi;
import com.subhash.apimonitoring.monitoring.strategy.HealthCheckStrategy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Health Check Strategy Factory
 *
 * WHY - Factory Pattern & Dependency Injection:
 * Centralized creation and selection of health check strategies
 * New strategies can be added without modifying existing code
 *
 * HOW:
 * - All strategies are injected via constructor
 * - Cache strategies by type for performance
 * - Selects appropriate strategy based on API configuration
 *
 * RESULT: Clean, extensible strategy selection mechanism
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class HealthCheckStrategyFactory {

    private final List<HealthCheckStrategy> strategies;
    private final Map<String, HealthCheckStrategy> strategyCache = new ConcurrentHashMap<>();

    /**
     * Get appropriate health check strategy for the given API
     */
    public HealthCheckStrategy getStrategy(MonitoredApi api) {
        String type = api.getCheckType().name();

        return strategyCache.computeIfAbsent(type, t -> {
            HealthCheckStrategy strategy = strategies.stream()
                    .filter(s -> s.supports(api))
                    .findFirst()
                    .orElseThrow(() -> new IllegalStateException(
                            "No health check strategy found for API: " + api.getName()));

            log.debug("Selected strategy {} for API {}", strategy.getType(), api.getName());
            return strategy;
        });
    }
}