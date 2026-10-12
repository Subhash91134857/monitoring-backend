// com/monitoring/exception/ResourceNotFoundException.java
package com.subhash.apimonitoring.exception;

/**
 * Resource Not Found Exception
 *
 * WHY: Specific exception for missing resources
 * Returns 404 status code for REST compliance
 *
 * RESULT: Consistent handling of missing resources across the application
 */
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String resourceName, Long id) {
        super(String.format("%s not found with id: %d", resourceName, id));
    }

    public ResourceNotFoundException(String resourceName, String identifier) {
        super(String.format("%s not found with identifier: %s", resourceName, identifier));
    }

    public ResourceNotFoundException(String message) {
        super(message);
    }
}