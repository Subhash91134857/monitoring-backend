package com.subhash.apimonitoring.auth.dto;


import com.subhash.apimonitoring.validation.ValidEmail;
import jakarta.validation.constraints.*;
import lombok.Data;

/**
 * Registration Request DTO
 *
 * WHY - Interface Segregation:
 * Only includes fields needed for registration
 * Validates input at API boundary
 *
 * RESULT: Secure and validated user input
 */
@Data
public class RegisterRequest {

    @NotBlank(message = "Username is required")
    @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
    @Pattern(regexp = "^[a-zA-Z0-9._-]+$",
            message = "Username can only contain letters, numbers, dots, underscores and hyphens")
    private String username;

    @NotBlank(message = "Email is required")
    @ValidEmail
    @Size(max = 255, message = "Email must not exceed 255 characters")
    private String email;

    @NotBlank(message = "Password is required")
    @Pattern(regexp = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=])(?=\\S+$).{8,}$",
            message = "Password must contain at least 8 characters, one digit, one lowercase, " +
                    "one uppercase, and one special character")
    private String password;

    @NotBlank(message = "First name is required")
    @Size(min = 1, max = 50, message = "First name must be between 1 and 50 characters")
    private String firstName;

    @Size(max = 50, message = "Last name must not exceed 50 characters")
    private String lastName;

    @Pattern(regexp = "^\\+?[0-9]{10,15}$", message = "Invalid phone number format")
    private String phoneNumber;

    @Size(max = 100, message = "Department must not exceed 100 characters")
    private String department;
}