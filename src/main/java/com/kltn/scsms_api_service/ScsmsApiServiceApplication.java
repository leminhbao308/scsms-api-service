package com.kltn.scsms_api_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@EnableJpaRepositories
public class ScsmsApiServiceApplication {
    
    public static void main(String[] args) {
        SpringApplication.run(ScsmsApiServiceApplication.class, args);
    }
}
