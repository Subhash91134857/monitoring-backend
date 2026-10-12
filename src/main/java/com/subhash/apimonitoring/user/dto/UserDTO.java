package com.subhash.apimonitoring.user.dto;


import com.fasterxml.jackson.annotation.JsonInclude; // jackson is used to convert JAVA object ro JSON and vice versa
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Set;

/**
 * User Data Transfer Object
 *
 * WHY - Separation of Concerns:
 * Decouples API contract from entity structure
 * Prevents exposing sensitive fields (passwordHash)
 *
 * HOW:
 * - Only includes fields needed for API responses
 * - @JsonInclude excludes null fields
 * - Builder pattern for flexible construction
 *
 * RESULT: Clean API contract that can evolve independently
 */
@Data
@Builder // without this we need so many setters functions for setting the private field, but with this we can set like this:-
/*
UserDTO dto = UserDTO.builder()
        .username("john")
        .email("john@gmail.com")
        .firstName("John")
        .build();
*/
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserDTO {
    private Long id;
    private String username;
    private String email;
    private String firstName;
    private String lastName;
    private String fullName;
    private String phoneNumber;
    private String department;
    private String profileImageUrl;
    private String role;
    private Set<String> permissions;
    private Boolean active;
    private Boolean emailVerified;
    private LocalDateTime lastLogin;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}