package com.kltn.scsms_api_service.configs.audit;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@Slf4j
@Configuration
@EnableJpaAuditing(auditorAwareRef = "auditorAwareImpl")
public class JpaAuditingConfig {
    
    public JpaAuditingConfig() {
        log.info("JpaAuditingConfig initialized - JPA Auditing is enabled");
    }
}
