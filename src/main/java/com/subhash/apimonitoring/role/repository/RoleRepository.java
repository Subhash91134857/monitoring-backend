package com.subhash.apimonitoring.role.repository;


import com.subhash.apimonitoring.role.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.Set;

/**
 * Role Repository
 *
 * WHY - Interface Segregation:
 * Provides focused data access methods for Role entity only
 *
 * RESULT: Clean, testable, and maintainable data access layer
 */
@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {

    Optional<Role> findByName(String name);

    Optional<Role> findByDefaultRoleTrue();

    boolean existsByName(String name);

    Set<Role> findBySystemRoleTrue();

    @Query("SELECT r FROM Role r WHERE r.name IN :names")
    Set<Role> findByNameIn(@Param("names") Set<String> names);

    @Modifying
    @Transactional
    @Query("UPDATE Role r SET r.defaultRole = false WHERE r.defaultRole = true")
    void clearDefaultRole();

    @Modifying
    @Transactional
    @Query("DELETE FROM Role r WHERE r.systemRole = false AND r.id = :id")
    int deleteNonSystemRoleById(@Param("id") Long id);
}
