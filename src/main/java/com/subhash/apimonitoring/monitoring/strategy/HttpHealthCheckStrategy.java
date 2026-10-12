package com.subhash.apimonitoring.monitoring.strategy;



import com.subhash.apimonitoring.monitoring.dto.HealthCheckResult;
import com.subhash.apimonitoring.monitoring.entity.MonitoredApi;
import com.subhash.apimonitoring.monitoring.enums.HealthStatus;
import com.subhash.apimonitoring.monitoring.enums.HttpMethod;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Map;

/**
 * HTTP Health Check Strategy
 *
 * WHY - Single Responsibility:
 * Handles ONLY HTTP-based health checks
 * Uses WebClient for non-blocking async calls
 *
 * HOW:
 * - Builds full URL with health endpoint
 * - Adds custom headers if configured
 * - Supports different HTTP methods
 * - Validates response body and status code
 *
 * RESULT: Comprehensive HTTP health checking with resilience patterns
 */
@Slf4j
@Component
public class HttpHealthCheckStrategy implements HealthCheckStrategy {

    private final WebClient.Builder webClientBuilder;

    @Value("${app.monitoring.health-check.default-timeout:5000}")
    private int defaultTimeout;

    @Value("${app.monitoring.health-check.user-agent:API-Monitor/1.0}")
    private String userAgent;

    public HttpHealthCheckStrategy(WebClient.Builder webClientBuilder) {
        this.webClientBuilder = webClientBuilder;
    }

    @Override
    @CircuitBreaker(name = "healthCheck", fallbackMethod = "fallbackHealthCheck")
    @Retry(name = "healthCheck", fallbackMethod = "fallbackHealthCheck")
    public HealthCheckResult check(MonitoredApi api) {
        long startTime = System.currentTimeMillis();
        String fullUrl = buildFullUrl(api);
        System.out.println("Health Check from httpHealthCheckStrategy: "+fullUrl);

        try {
            log.debug("Performing HTTP health check for: {}", fullUrl);

            WebClient webClient = webClientBuilder
                    .baseUrl(fullUrl)
                    .defaultHeader(HttpHeaders.USER_AGENT, userAgent)
                    .build();

            // Prepare request
            org.springframework.http.HttpMethod springMethod =
                    org.springframework.http.HttpMethod.valueOf(api.getMethod().name());

            WebClient.RequestBodySpec requestSpec =
                    webClient.method(springMethod);

            // Add custom headers
            if (api.getCustomHeaders() != null) {
                api.getCustomHeaders().forEach(requestSpec::header);
            }

            // Add request body for POST/PUT
            if ((api.getMethod() == HttpMethod.POST || api.getMethod() == HttpMethod.PUT)
                    && api.getRequestBody() != null) {
                requestSpec.bodyValue(api.getRequestBody());
            }

            // Execute request with timeout
            ResponseEntity<String> responseEntity = requestSpec
                    .retrieve()
                    .toEntity(String.class)
                    .timeout(Duration.ofMillis(api.getTimeout() != null ? api.getTimeout() : defaultTimeout))
                    .block();

            long responseTime = System.currentTimeMillis() - startTime;

            // Validate response
            boolean isValid = validateResponse(api, responseEntity);
            HealthStatus status = isValid ? HealthStatus.UP : HealthStatus.DEGRADED;

            log.debug("Health check completed for {}: {} in {}ms",
                    api.getName(), status, responseTime);

            return HealthCheckResult.builder()
                    .apiId(api.getId())
                    .apiName(api.getName())
                    .apiUrl(fullUrl)
                    .status(status)
                    .responseTimeMs((int) responseTime)
                    .statusCode(responseEntity.getStatusCode().value())
                    .responseBody(responseEntity.getBody())
                    .isSuccess(isValid)
                    .timestamp(System.currentTimeMillis())
                    .build();

        } catch (Exception e) {
            long responseTime = System.currentTimeMillis() - startTime;
            log.error("Health check failed for {}: {}", api.getName(), e.getMessage());

            return HealthCheckResult.builder()
                    .apiId(api.getId())
                    .apiName(api.getName())
                    .apiUrl(fullUrl)
                    .status(HealthStatus.DOWN)
                    .responseTimeMs((int) responseTime)
                    .errorMessage(e.getMessage())
                    .isSuccess(false)
                    .timestamp(System.currentTimeMillis())
                    .build();
        }
    }

    private String buildFullUrl(MonitoredApi api) {
        String baseUrl = api.getUrl();
        String healthEndpoint = api.getHealthEndpoint();

        if (healthEndpoint == null || healthEndpoint.isEmpty()) {
            return baseUrl;
        }

        if (baseUrl.endsWith("/") && healthEndpoint.startsWith("/")) {
            return baseUrl + healthEndpoint.substring(1);
        } else if (!baseUrl.endsWith("/") && !healthEndpoint.startsWith("/")) {
            return baseUrl + "/" + healthEndpoint;
        } else {
            return baseUrl + healthEndpoint;
        }
    }

    private boolean validateResponse(MonitoredApi api, ResponseEntity<String> response) {
        if (response == null) {
            return false;
        }

        // Check status code
        if (api.getExpectedStatusCode() != null &&
                response.getStatusCode().value() != api.getExpectedStatusCode()) {
            log.warn("Status code mismatch. Expected: {}, Got: {}",
                    api.getExpectedStatusCode(), response.getStatusCode().value());
            return false;
        }

        // Check response body if expected
        if (api.getExpectedResponseBody() != null &&
                response.getBody() != null &&
                !response.getBody().contains(api.getExpectedResponseBody())) {
            log.warn("Response body does not contain expected text");
            return false;
        }

        return true;
    }

    /**
     * Fallback method when circuit breaker is open or retries exhausted
     */
    @SuppressWarnings("unused")
    private HealthCheckResult fallbackHealthCheck(MonitoredApi api, Throwable t) {
        log.warn("Circuit breaker open for {}, using fallback", api.getName());

        return HealthCheckResult.builder()
                .apiId(api.getId())
                .apiName(api.getName())
                .apiUrl(api.getUrl())
                .status(HealthStatus.DOWN)
                .errorMessage("Circuit breaker open: " + t.getMessage())
                .isSuccess(false)
                .timestamp(System.currentTimeMillis())
                .build();
    }

    @Override
    public String getType() {
        return "HTTP";
    }

    @Override
    public boolean supports(MonitoredApi api) {
        return api.getUrl() != null &&
                (api.getUrl().startsWith("http://") || api.getUrl().startsWith("https://"));
    }
}
