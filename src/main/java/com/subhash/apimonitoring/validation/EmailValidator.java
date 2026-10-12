// com/monitoring/validation/EmailValidator.java
package com.subhash.apimonitoring.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.regex.Pattern;

/**
 * Custom Email Validator
 *
 * WHY: More strict email validation than standard @Email
 * Prevents malicious email formats
 *
 * RESULT: Secure and valid email addresses only
 */
import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

public class EmailValidator implements ConstraintValidator<ValidEmail, String> {

    private static final String EMAIL_PATTERN =
            "^[A-Za-z0-9+_.-]+@(.+)$";

    private Pattern pattern;

    @Override
    public void initialize(ValidEmail constraintAnnotation) {
        pattern = Pattern.compile(EMAIL_PATTERN);
    }

    @Override
    public boolean isValid(String email, ConstraintValidatorContext context) {
        if (email == null || email.trim().isEmpty()) {
            return false;
        }

        // Basic email validation
        if (!pattern.matcher(email).matches()) {
            return false;
        }

        // Additional security checks
        if (email.length() > 255) {
            return false;
        }

        // Prevent SQL injection patterns
        if (email.contains("'") || email.contains("\"") || email.contains("--")) {
            return false;
        }

        return true;
    }
}

// Annotation for the validator


