package com.subhash.apimonitoring.security.jwt;



import com.fasterxml.jackson.databind.ObjectMapper;
import com.subhash.apimonitoring.common.response.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * JWT Authentication Entry Point
 *
 * WHY - Single Responsibility:
 * Handles unauthorized access attempts
 *
 * RESULT: Consistent error response for unauthenticated requests
 * if user is unauthorized then this handle it,
 */

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {
    private final ObjectMapper objectMapper;
    @Override
    public void commence(HttpServletRequest request,
                         HttpServletResponse response,
                         AuthenticationException authException) throws IOException {

        log.error("Unauthorized access attempt to: {}", request.getRequestURL());
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType("application/json");
        ApiResponse<Void> apiResponse = ApiResponse.error(
                "Authentication required. Please login and provide valid token.",
                "UNAUTHORIZED"
        );
        response.getWriter().write(objectMapper.writeValueAsString(apiResponse));
    }
}
