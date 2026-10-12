package com.subhash.apimonitoring.user.entity;


import com.subhash.apimonitoring.common.base.BaseEntity;
import com.subhash.apimonitoring.role.entity.Role;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

/**
 * User Entity
 *
 * WHY - Single Responsibility:
 * User entity handles ONLY user data and authentication state
 * Business logic about users is in service layer
 *
 * HOW:
 * - Extends BaseEntity for audit fields
 * - Many-to-One relationship with Role
 * - Tracks login attempts for security
 * - Supports account lockout after failed attempts
 *
 * RESULT:
 * - Complete user profile with security features
 * - Audit trail for user modifications
 * - Built-in protection against brute force attacks
 */
@Entity
@Table(name = "users", indexes = {
        @Index(name = "idx_user_email", columnList = "email"),
        @Index(name = "idx_user_username", columnList = "username"),
        @Index(name = "idx_user_role", columnList = "role_id"),
        @Index(name = "idx_user_active", columnList = "is_active"),
        @Index(name = "idx_user_email_verified", columnList = "email_verified")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class User extends BaseEntity {
    @Column(nullable = false, unique = true, length = 100)
    private String username;
    @Column(nullable = false, unique = true, length = 255)
    private String email;
    @Column(nullable = false)
    private String passwordHash;
    @Column(length = 50)
    private String firstName;
    @Column(length = 50)
    private String lastName;
    @Column(length = 20)
    private String phoneNumber;
    @Column(length = 100)
    private String department;
    @Column(length = 255)
    private String profileImageUrl;
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "role_id", nullable = false)
    private Role role;
    @Column(name = "is_active")
    private boolean active = true;
    @Column(name = "email_verified")
    private boolean emailVerified = false;
    @Column(name = "verification_token")
    private String verificationToken;
    @Column(name = "verification_token_expiry")
    private LocalDateTime verificationTokenExpiry;
    @Column(name = "password_reset_token")
    private String passwordResetToken;
    @Column(name = "password_reset_expiry")
    private LocalDateTime passwordResetExpiry;
    @Column(name = "last_login")
    private LocalDateTime lastLogin;
    @Column(name = "last_login_ip")
    private String lastLoginIp;
    @Column(name = "failed_attempts")
    private int failedLoginAttempts = 0;
    @Column(name = "account_locked")
    private boolean accountLocked = false;
    @Column(name = "lock_time")
    private LocalDateTime lockTime;

    /**
     * Check if account is locked
     */
    public boolean isAccountLockedAndValid() {
        if (!accountLocked) {
            return false;
        }
        // Auto-unlock after 30 minutes
        if (lockTime != null && lockTime.plusMinutes(30).isBefore(LocalDateTime.now())) {
            accountLocked = false;
            failedLoginAttempts = 0;
            lockTime = null;
            return false;
        }
        return true;
    }
    /**
     * Increment failed login attempts
     */
    public void incrementFailedAttempts() {
        this.failedLoginAttempts++;
        if (this.failedLoginAttempts >= 5) {
            this.accountLocked = true;
            this.lockTime = LocalDateTime.now();
        }
    }
    /**
     * Reset failed attempts after successful login
     */
    public void resetFailedAttempts() {
        this.failedLoginAttempts = 0;
        this.accountLocked = false;
        this.lockTime = null;
    }

    /**
     * Get full name
     */
    public String getFullName() {
        if (firstName == null && lastName == null) {
            return username;
        }
        return (firstName != null ? firstName : "") + " " + (lastName != null ? lastName : "");
    }
}