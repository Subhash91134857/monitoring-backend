package com.subhash.apimonitoring.role.enums;

import java.util.Set;

/**
 * Role Types Enumeration
 *
 * WHY - Single Responsibility:
 * Centralizes all role definitions and their permissions
 * Easy to add new roles without modifying existing code
 *
 * HOW:
 * - Each role has a name and a set of permissions
 * - Permissions define what actions users with this role can perform
 *
 * RESULT:
 * - Type-safe role management
 * - Clear permission boundaries
 * - Easy to extend for new roles
 */
public enum RoleType {

    ADMIN(
            "Administrator",
            Set.of(
                    "USER_CREATE", "USER_UPDATE", "USER_DELETE", "USER_VIEW",
                    "ROLE_CREATE", "ROLE_UPDATE", "ROLE_DELETE", "ROLE_VIEW",
                    "API_CREATE", "API_UPDATE", "API_DELETE", "API_VIEW",
                    "MONITORING_VIEW", "INCIDENT_VIEW", "INCIDENT_ACKNOWLEDGE",
                    "ANALYTICS_VIEW", "AUDIT_VIEW", "SYSTEM_CONFIG"
            )
    ),

    MANAGER(
            "Manager",
            Set.of(
                    "USER_CREATE", "USER_UPDATE", "USER_VIEW",
                    "API_CREATE", "API_UPDATE", "API_VIEW",
                    "MONITORING_VIEW", "INCIDENT_VIEW", "INCIDENT_ACKNOWLEDGE",
                    "ANALYTICS_VIEW"
            )
    ),

    VIEWER(
            "Viewer",
            Set.of(
                    "USER_VIEW",
                    "API_VIEW",
                    "MONITORING_VIEW",
                    "INCIDENT_VIEW",
                    "ANALYTICS_VIEW"
            )
    );

    private final String description;
    private final Set<String> permissions;

    RoleType(String description, Set<String> permissions) {
        this.description = description;
        this.permissions = permissions;
    }

    public String getDescription() {
        return description;
    }

    public Set<String> getPermissions() {
        return permissions;
    }
}
