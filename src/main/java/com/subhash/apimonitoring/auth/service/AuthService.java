package com.subhash.apimonitoring.auth.service;



import com.subhash.apimonitoring.auth.dto.*;
import com.subhash.apimonitoring.common.constants.AppConstants;
import com.subhash.apimonitoring.exception.BusinessException;
import com.subhash.apimonitoring.role.entity.Role;
import com.subhash.apimonitoring.role.repository.RoleRepository;
import com.subhash.apimonitoring.security.jwt.JwtTokenProvider;
import com.subhash.apimonitoring.user.dto.UserDTO;
import com.subhash.apimonitoring.user.entity.User;
import com.subhash.apimonitoring.user.mapper.UserMapper;
import com.subhash.apimonitoring.user.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Authentication Service
 *
 * WHY - Single Responsibility:
 * Handles ONLY authentication operations (register, login, refresh)
 *
 * HOW:
 * - Uses repository for data access
 * - Uses mapper for DTO conversion
 * - Implements security best practices (BCrypt, lockout)
 *
 * RESULT: Secure, maintainable authentication logic
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final UserMapper userMapper;

    @Transactional
    public UserDTO register(RegisterRequest request, HttpServletRequest httpRequest) {
        log.info("Registering new user with email: {}", request.getEmail());

        // Validate unique constraints
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BusinessException("Email already registered", "DUPLICATE_EMAIL");
        }

        if (userRepository.existsByUsername(request.getUsername())) {
            throw new BusinessException("Username already taken", "DUPLICATE_USERNAME");
        }

        // Get default role
        Role defaultRole = roleRepository.findByDefaultRoleTrue()
                .orElseThrow(() -> new BusinessException("Default role not configured", "ROLE_NOT_FOUND"));

        // Create user entity
        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .phoneNumber(request.getPhoneNumber())
                .department(request.getDepartment())
                .role(defaultRole)
                .active(true)
                .emailVerified(false)
                .build();

        User savedUser = userRepository.save(user);
        log.info("User registered successfully with ID: {}", savedUser.getId());

        return userMapper.toDto(savedUser);
    }

    @Transactional
    public AuthResponse login(LoginRequest request, String clientIp) {
        log.info("Login attempt for email: {}", request.getEmail());

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new BusinessException("Invalid credentials", "INVALID_CREDENTIALS"));

        // Check account lock
        if (user.isAccountLockedAndValid()) {
            throw new BusinessException("Account locked. Try again later", "ACCOUNT_LOCKED");
        }

        // Verify password
        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            user.incrementFailedAttempts();
            userRepository.save(user);
            throw new BusinessException("Invalid credentials", "INVALID_CREDENTIALS");
        }

        // Reset failed attempts on success
        user.resetFailedAttempts();
        user.setLastLogin(LocalDateTime.now());
        user.setLastLoginIp(clientIp);
        userRepository.save(user);

        // Generate tokens
        String accessToken = jwtTokenProvider.generateAccessToken(user.getEmail(), user.getRole().getName());
        String refreshToken = jwtTokenProvider.generateRefreshToken(user.getEmail());

        log.info("User logged in successfully: {}", user.getEmail());

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(3600000)
                .user(userMapper.toDto(user))
                .build();
    }

    @Transactional
    public AuthResponse refreshToken(String refreshToken) {
        if (!jwtTokenProvider.validateToken(refreshToken)) {
            throw new BusinessException("Invalid refresh token", "INVALID_TOKEN");
        }

        String email = jwtTokenProvider.getEmailFromToken(refreshToken);
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException("User not found", "USER_NOT_FOUND"));

        String newAccessToken = jwtTokenProvider.generateAccessToken(user.getEmail(), user.getRole().getName());

        return AuthResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(3600000)
                .user(userMapper.toDto(user))
                .build();
    }

    @Transactional
    public void logout(String email) {
        log.info("Logout for user: {}", email);
        // In a real implementation, you might add token to blacklist
        // For now, just log the logout
    }
}