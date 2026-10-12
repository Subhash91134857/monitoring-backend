package com.subhash.apimonitoring.monitoring.strategy;



import com.subhash.apimonitoring.monitoring.dto.HealthCheckResult;
import com.subhash.apimonitoring.monitoring.entity.MonitoredApi;
import com.subhash.apimonitoring.monitoring.enums.HealthStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;

/**
 * TCP Health Check Strategy
 *
 * WHY - Strategy Pattern:
 * Implements health check via TCP socket connection
 * Useful for databases, message queues, etc.
 *
 * RESULT: Can check services that don't have HTTP endpoints
 */
@Slf4j
@Component
public class TcpHealthCheckStrategy implements HealthCheckStrategy {

    @Override
    public HealthCheckResult check(MonitoredApi api) {
        long startTime = System.currentTimeMillis();

        try {
            String host = extractHost(api.getUrl());
            int port = extractPort(api.getUrl());
            int timeout = api.getTimeout() != null ? api.getTimeout() : 5000;

            log.debug("Performing TCP health check for {}:{}", host, port);

            try (Socket socket = new Socket()) {
                socket.connect(new InetSocketAddress(host, port), timeout);
                long responseTime = System.currentTimeMillis() - startTime;

                return HealthCheckResult.builder()
                        .apiId(api.getId())
                        .apiName(api.getName())
                        .apiUrl(api.getUrl())
                        .status(HealthStatus.UP)
                        .responseTimeMs((int) responseTime)
                        .isSuccess(true)
                        .timestamp(System.currentTimeMillis())
                        .build();
            }
        } catch (IOException e) {
            long responseTime = System.currentTimeMillis() - startTime;
            log.error("TCP health check failed for {}: {}", api.getName(), e.getMessage());

            return HealthCheckResult.builder()
                    .apiId(api.getId())
                    .apiName(api.getName())
                    .apiUrl(api.getUrl())
                    .status(HealthStatus.DOWN)
                    .responseTimeMs((int) responseTime)
                    .errorMessage(e.getMessage())
                    .isSuccess(false)
                    .timestamp(System.currentTimeMillis())
                    .build();
        }
    }

    private String extractHost(String url) {
        // Remove protocol
        String withoutProtocol = url.replaceFirst("^(tcp://|http://|https://)", "");
        // Extract host part before colon or slash
        int colonIndex = withoutProtocol.indexOf(':');
        int slashIndex = withoutProtocol.indexOf('/');

        if (colonIndex > 0 && (slashIndex < 0 || colonIndex < slashIndex)) {
            return withoutProtocol.substring(0, colonIndex);
        } else if (slashIndex > 0) {
            return withoutProtocol.substring(0, slashIndex);
        }

        return withoutProtocol;
    }

    private int extractPort(String url) {
        // Default ports
        if (url.startsWith("tcp://")) {
            // Try to extract custom port
            String withoutProtocol = url.replaceFirst("^(tcp://)", "");
            int colonIndex = withoutProtocol.indexOf(':');
            if (colonIndex > 0) {
                int slashIndex = withoutProtocol.indexOf('/');
                if (slashIndex > colonIndex) {
                    return Integer.parseInt(withoutProtocol.substring(colonIndex + 1, slashIndex));
                } else {
                    return Integer.parseInt(withoutProtocol.substring(colonIndex + 1));
                }
            }
            return 0; // Will need to be configured
        }
        return 80;
    }

    @Override
    public String getType() {
        return "TCP";
    }

    @Override
    public boolean supports(MonitoredApi api) {
        return api.getUrl() != null && api.getUrl().startsWith("tcp://");
    }
}
