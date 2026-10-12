package com.subhash.apimonitoring.role.entity;



import com.subhash.apimonitoring.common.base.BaseEntity;
import com.subhash.apimonitoring.role.enums.RoleType;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.Set;

/**
 * Role Entity
 *
 * WHY - Open-Closed Principle:
 * Roles can be extended with new permissions without modifying existing code
 * System can add custom roles beyond the predefined enums
 *
 * HOW:
 * - Extends BaseEntity to inherit audit fields
 * - Stores role name and permissions
 * - Supports soft delete through BaseEntity
 *
 * RESULT:
 * - Flexible role management
 * - Complete audit trail for role changes
 * - Support for custom roles
 */
@Entity
@Table(name = "roles", indexes = {
        @Index(name = "idx_role_name", columnList = "name"),
        @Index(name = "idx_role_default", columnList = "is_default")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class Role extends BaseEntity {

    @Column(nullable = false, unique = true, length = 50)
    private String name;

    @Column(length = 255)
    private String description;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
            name = "role_permissions",
            joinColumns = @JoinColumn(name = "role_id")
    )
    @Column(name = "permission")
    private Set<String> permissions;

    @Column(name = "is_default")
    private boolean defaultRole;

    @Column(name = "is_system_role")
    private boolean systemRole; // ADMIN, MANAGER, VIEWER are system roles

    /**
     * Create role from enum
     */
    public static Role fromEnum(RoleType roleType) {
        return Role.builder()
                .name(roleType.name())
                .description(roleType.getDescription())
                .permissions(roleType.getPermissions())
                .systemRole(true)
                .build();
    }
}