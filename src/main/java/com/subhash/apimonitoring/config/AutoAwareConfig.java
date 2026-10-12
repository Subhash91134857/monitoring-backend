// com/monitoring/config/AuditorAwareConfig.java
package com.subhash.apimonitoring.config;

import com.subhash.apimonitoring.security.model.CustomUserPrincipal;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Optional;

/**
 * Auditor Aware Configuration for Automatic Audit Fields
 *
 * WHY - Dependency Inversion Principle:
 * Spring Data JPA needs to know who is the current user for @CreatedBy/@LastModifiedBy
 * This class provides that information without coupling to specific user implementation
 *
 * HOW:
 * - Implements AuditorAware interface
 * - Extracts current user from Spring Security context
 * - Returns user ID for audit fields
 *
 * RESULT:
 * - Automatic population of created_by and updated_by fields
 * - No need to manually set audit fields in services
 * - Complete audit trail with user information
 */
@Configuration
class AuditorAwareConfig {
    @Bean
    public AuditorAware<Long> auditorProvider() {
        return () -> {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

            if (authentication == null || !authentication.isAuthenticated()) {
                return Optional.empty();
            }

            Object principal = authentication.getPrincipal();

            if (principal instanceof CustomUserPrincipal) {
                return Optional.of(((CustomUserPrincipal) principal).getId());
            } else if (principal instanceof UserDetails) {
                // Fallback for basic authentication
                return Optional.of(0L);
            }

            return Optional.empty();
        };
    }
}