// com/monitoring/MonitoringApplication.java
package com.subhash.apimonitoring;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.cache.annotation.EnableCaching;

/**
 * Main Application Entry Point
 *
 * WHY: Centralized configuration with specific annotations for each capability
 * - @EnableJpaAuditing: Enables automatic timestamp management
 * - @EnableAsync: Enables non-blocking operations
 * - @EnableScheduling: Enables background monitoring jobs
 * - @EnableCaching: Enables Redis caching for performance
 *
 * HOW: Spring Boot auto-configuration with explicit feature toggles
 * RESULT: Production-ready application with all required capabilities enabled
 */
@SpringBootApplication
@EnableJpaAuditing
@EnableAsync
@EnableScheduling
@EnableCaching
public class ApiMonitoringApplication {

    public static void main(String[] args) {
        SpringApplication.run(ApiMonitoringApplication.class, args);
    }
}