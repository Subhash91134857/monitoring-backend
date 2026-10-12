// com/monitoring/common/constants/AppConstants.java
package com.subhash.apimonitoring.common.constants;

/**
 * Application Constants
 *
 * WHY: Centralize all magic numbers and strings
 * Easy to update values across the entire application
 *
 * RESULT: Maintainable and consistent configuration
 */
public final class AppConstants {

    private AppConstants() {
        // Private constructor to prevent instantiation
    }

    // API Paths
    public static final String API_BASE_PATH = "/api";
    public static final String API_AUTH_PATH = API_BASE_PATH + "/auth";
    public static final String API_USERS_PATH = API_BASE_PATH + "/users";
    public static final String API_MONITORING_PATH = API_BASE_PATH + "/monitoring";
    public static final String API_INCIDENTS_PATH = API_BASE_PATH + "/incidents";
    public static final String API_ANALYTICS_PATH = API_BASE_PATH + "/analytics";
    public static final String API_ADMIN_PATH = API_BASE_PATH + "/admin";

    // Pagination
    public static final int DEFAULT_PAGE_SIZE = 20;
    public static final int MAX_PAGE_SIZE = 100;

    // Monitoring
    public static final int DEFAULT_HEALTH_CHECK_INTERVAL = 60; // seconds
    public static final int DEFAULT_TIMEOUT = 5; // seconds
    public static final int DEFAULT_RETRY_COUNT = 2;

    // Cache
    public static final String CACHE_API_CONFIGS = "apiConfigs";
    public static final String CACHE_HEALTH_CHECKS = "healthChecks";
    public static final int CACHE_TTL_SECONDS = 30;

    // Messages
    public static final String MSG_OPERATION_SUCCESS = "Operation completed successfully";
    public static final String MSG_OPERATION_FAILED = "Operation failed";
    public static final String MSG_RESOURCE_NOT_FOUND = "Resource not found";
    public static final String MSG_UNAUTHORIZED = "Unauthorized access";
    public static final String MSG_FORBIDDEN = "Access denied";
}