package com.kltn.scsms_api_service.core.filters;

import com.kltn.scsms_api_service.core.utils.LoggingUtils;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Filter to automatically set correlation ID for each request
 */
@Component
@Order(1)
public class RequestLoggingFilter extends OncePerRequestFilter {
    
    private static final Logger logger = LoggerFactory.getLogger(RequestLoggingFilter.class);
    private static final String CORRELATION_ID_HEADER = "X-Correlation-ID";
    
    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        
        // Set correlation ID from header or generate new one
        String correlationId = request.getHeader(CORRELATION_ID_HEADER);
        if (correlationId == null || correlationId.trim().isEmpty()) {
            correlationId = LoggingUtils.generateCorrelationId();
        }
        
        LoggingUtils.setCorrelationId(correlationId);
        response.setHeader(CORRELATION_ID_HEADER, correlationId);
        
        long startTime = System.currentTimeMillis();
        
        try {
            logger.info("REQUEST START: {} {} from {}",
                request.getMethod(),
                request.getRequestURI(),
                getClientIP(request));
            
            filterChain.doFilter(request, response);
            
        } finally {
            long duration = System.currentTimeMillis() - startTime;
            logger.info("REQUEST END: {} {} - Status: {} - Duration: {}ms",
                request.getMethod(),
                request.getRequestURI(),
                response.getStatus(),
                duration);
            
            // Clean up MDC
            LoggingUtils.clearMDC();
        }
    }
    
    private String getClientIP(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        
        String xRealIP = request.getHeader("X-Real-IP");
        if (xRealIP != null && !xRealIP.isEmpty()) {
            return xRealIP;
        }
        
        return request.getRemoteAddr();
    }
}
