package com.kltn.scsms_api_service.core.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

import java.util.UUID;
import java.util.function.Supplier;

/**
 * Lightweight logging utilities that work with native SLF4J/Logback
 */
@Component
public class LoggingUtils {
    
    private static final String CORRELATION_ID = "correlationId";
    private static final Logger PERFORMANCE_LOGGER = LoggerFactory.getLogger("PERFORMANCE");
    
    /**
     * Set correlation ID for request tracking
     */
    public static void setCorrelationId(String correlationId) {
        MDC.put(CORRELATION_ID, correlationId != null ? correlationId : generateCorrelationId());
    }
    
    /**
     * Generate new correlation ID
     */
    public static String generateCorrelationId() {
        return UUID.randomUUID().toString().substring(0, 8);
    }
    
    /**
     * Get current correlation ID
     */
    public static String getCorrelationId() {
        return MDC.get(CORRELATION_ID);
    }
    
    /**
     * Clear MDC for current thread
     */
    public static void clearMDC() {
        MDC.clear();
    }
    
    /**
     * Execute with timing and automatic performance logging
     */
    public static <T> T executeWithTiming(Logger logger, String operation, Supplier<T> execution) {
        long startTime = System.currentTimeMillis();
        try {
            T result = execution.get();
            long executionTime = System.currentTimeMillis() - startTime;
            
            // Log performance based on threshold
            if (executionTime > 1000L) { // > 1s = warning
                PERFORMANCE_LOGGER.warn("SLOW_OPERATION: {} took {}ms", operation, executionTime);
                logger.warn("{} took {}ms (performance warning)", operation, executionTime);
            } else if (executionTime > 100L) { // > 100ms = info
                PERFORMANCE_LOGGER.info("OPERATION: {} took {}ms", operation, executionTime);
                logger.debug("{} completed in {}ms", operation, executionTime);
            } else {
                logger.trace("{} completed in {}ms", operation, executionTime);
            }
            
            return result;
        } catch (Exception e) {
            long executionTime = System.currentTimeMillis() - startTime;
            logger.error("{} failed after {}ms", operation, executionTime, e);
            throw e;
        }
    }
    
    /**
     * Execute void operation with timing
     */
    public static void executeWithTiming(Logger logger, String operation, Runnable execution) {
        executeWithTiming(logger, operation, () -> {
            execution.run();
            return null;
        });
    }
    
    /**
     * Log database operation with standard format
     */
    public static void logDatabaseOperation(Logger logger, String operation, String entity, Object id) {
        logger.info("DB_OPERATION: {} {} with id: {}", operation, entity, id);
    }
    
    /**
     * Log method entry (only in TRACE level)
     */
    public static void logMethodEntry(Logger logger, String methodName, Object... params) {
        if (logger.isTraceEnabled()) {
            logger.trace("ENTRY -> {}() with params: {}", methodName, formatParams(params));
        }
    }
    
    /**
     * Log method exit (only in TRACE level)
     */
    public static void logMethodExit(Logger logger, String methodName, Object result) {
        if (logger.isTraceEnabled()) {
            logger.trace("EXIT <- {}() returned: {}", methodName,
                result != null ? result.toString() : "null");
        }
    }
    
    private static String formatParams(Object... params) {
        if (params == null || params.length == 0) {
            return "[]";
        }
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < params.length; i++) {
            if (i > 0) sb.append(", ");
            sb.append(params[i] != null ? params[i].toString() : "null");
        }
        sb.append("]");
        return sb.toString();
    }
}
