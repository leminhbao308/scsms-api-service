package com.kltn.scsms_api_service.scheduler;

import com.kltn.scsms_api_service.core.service.TokenService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class TokenCleanupScheduler {

    private final TokenService tokenService;

    /** Clean up expired tokens every hour */
    @Scheduled(fixedRate = 3600000) // 1 hour in milliseconds
    public void cleanupExpiredTokens() {
        log.info("Starting scheduled token cleanup");
        try {
            tokenService.cleanupExpiredTokens();
            log.info("Token cleanup completed successfully");
        } catch (Exception e) {
            log.error("Error during token cleanup: {}", e.getMessage(), e);
        }
    }
}
