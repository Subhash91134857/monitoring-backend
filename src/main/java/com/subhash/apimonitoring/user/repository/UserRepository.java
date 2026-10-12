package com.subhash.apimonitoring.user.repository;


import com.subhash.apimonitoring.user.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * User Repository
 *
 * WHY - Interface Segregation:
 * Defines specific queries needed for user management
 *
 * RESULT: Efficient data access with optimized queries
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    // ========== Basic Queries ==========
    Optional<User> findByEmail(String email);
    Optional<User> findByUsername(String username);
    Optional<User> findByVerificationToken(String token);
    Optional<User> findByPasswordResetToken(String token);
    boolean existsByEmail(String email);
    boolean existsByUsername(String username);
    // ========== Status Queries ==========
    @Query("SELECT u FROM User u WHERE u.active = true AND u.role.name = :roleName")
    List<User> findActiveUsersByRole(@Param("roleName") String roleName);
    @Query("SELECT u FROM User u WHERE u.emailVerified = false AND u.createdAt < :date")
    List<User> findUnverifiedUsersOlderThan(@Param("date") LocalDateTime date);
    @Query("SELECT u FROM User u WHERE u.accountLocked = true AND u.lockTime < :date")
    List<User> findLockedUsersForAutoUnlock(@Param("date") LocalDateTime date);

    // ========== Activity Queries ==========
    @Query("SELECT COUNT(u) FROM User u WHERE u.lastLogin >= :since")
    long countActiveUsersSince(@Param("since") LocalDateTime since);
    @Query("SELECT u FROM User u WHERE u.lastLogin < :date AND u.active = true")
    Page<User> findInactiveUsers(@Param("date") LocalDateTime date, Pageable pageable);

    // ========== Search Queries ==========
    @Query("SELECT u FROM User u WHERE " +
            "LOWER(u.username) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(u.email) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(u.firstName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(u.lastName) LIKE LOWER(CONCAT('%', :search, '%'))")
    Page<User> searchUsers(@Param("search") String search, Pageable pageable);

    // ========== Update Queries ==========
    @Modifying
    @Transactional
    @Query("UPDATE User u SET u.lastLogin = :loginTime, u.lastLoginIp = :ip WHERE u.email = :email")
    int updateLastLogin(@Param("email") String email,
                        @Param("loginTime") LocalDateTime loginTime,
                        @Param("ip") String ip);
    @Modifying
    @Transactional
    @Query("UPDATE User u SET u.passwordHash = :newPassword WHERE u.email = :email")
    int updatePassword(@Param("email") String email, @Param("newPassword") String newPassword);
    @Modifying
    @Transactional
    @Query("UPDATE User u SET u.emailVerified = true WHERE u.id = :id")
    int verifyEmail(@Param("id") Long id);
}