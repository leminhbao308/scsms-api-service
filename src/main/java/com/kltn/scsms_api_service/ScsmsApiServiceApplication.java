package com.kltn.scsms_api_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class ScsmsApiServiceApplication {
    
    public static void main(String[] args) {
        SpringApplication.run(ScsmsApiServiceApplication.class, args);
    }
}
