// com/monitoring/exception/BusinessException.java
package com.subhash.apimonitoring.exception;

import lombok.Getter;

/**
 * Business Logic Exception
 *
 * WHY: Distinguish between system errors and business rule violations
 * Frontend can show user-friendly messages for business exceptions
 *
 * RESULT: Clean separation of business rules from technical errors
 */
@Getter
public class BusinessException extends RuntimeException {

    private final String errorCode;

    public BusinessException(String message) {
        super(message);
        this.errorCode = "BUSINESS_ERROR";
    }

    public BusinessException(String message, String errorCode) {
        super(message);
        this.errorCode = errorCode;
    }

    public BusinessException(String message, Throwable cause) {
        super(message, cause);
        this.errorCode = "BUSINESS_ERROR";
    }
}