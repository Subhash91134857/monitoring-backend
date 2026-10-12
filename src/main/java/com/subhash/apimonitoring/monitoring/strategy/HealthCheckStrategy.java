package com.subhash.apimonitoring.monitoring.strategy;

import com.subhash.apimonitoring.monitoring.dto.HealthCheckResult;
import com.subhash.apimonitoring.monitoring.entity.MonitoredApi;

/**
 * Health Check Strategy Interface
 *
 * WHY - Strategy Pattern & Open-Closed Principle:
 * Allows different health check implementations without modifying existing code
 * New check types can be added by implementing this interface
 *
 * HOW:
 * - Each strategy implements its own health check logic
 * - Factory pattern selects appropriate strategy
 *
 * RESULT: Extensible, maintainable health check system
 */
public interface HealthCheckStrategy {

    /**
     * Perform health check on the given API
     */
    HealthCheckResult check(MonitoredApi api);

    /**
     * Get the type of health check this strategy handles
     */
    String getType();

    /**
     * Check if this strategy supports the given API
     */
    boolean supports(MonitoredApi api);
}