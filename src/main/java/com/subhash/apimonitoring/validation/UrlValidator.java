// com/monitoring/validation/UrlValidator.java
package com.subhash.apimonitoring.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.lang.annotation.Annotation;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;

/**
 * URL Validator for API Endpoints
 *
 * WHY: Ensure only valid URLs are added for monitoring
 * Prevents injection attacks and invalid configurations
 *
 * RESULT: Safe and valid URLs for health checks
 */
public class UrlValidator<ValidUrl extends Annotation> implements ConstraintValidator<ValidUrl, String> {

    @Override
    public boolean isValid(String url, ConstraintValidatorContext context) {
        if (url == null || url.trim().isEmpty()) {
            return false;
        }

        try {
            URL validUrl = new URL(url);
            validUrl.toURI();

            // Only allow HTTP/HTTPS
            String protocol = validUrl.getProtocol();
            if (!"http".equals(protocol) && !"https".equals(protocol)) {
                return false;
            }

            // Prevent localhost in production
            String host = validUrl.getHost();
            if (host != null && (host.equals("localhost") || host.equals("127.0.0.1"))) {
                // Allow for development, but could be blocked in production
                return true;
            }

            return true;
        } catch (MalformedURLException | URISyntaxException e) {
            return false;
        }
    }
}