package com.kltn.scsms_api_service;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication
@EnableScheduling
@EnableTransactionManagement
public class ScsmsApiServiceApplication {
    
    public static void main(String[] args) {
        // Load .env file for local development
        // This will load environment variables from .env file into System properties
        try {
            Dotenv dotenv = Dotenv.configure()
                    .ignoreIfMissing() // Don't fail if .env doesn't exist
                    .load();
            
            // Set environment variables from .env file
            dotenv.entries().forEach(entry -> {
                String key = entry.getKey();
                String value = entry.getValue();
                // Only set if not already set as system property or environment variable
                if (System.getProperty(key) == null && System.getenv(key) == null) {
                    System.setProperty(key, value);
                }
            });
        } catch (Exception e) {
            // If .env file doesn't exist or can't be loaded, continue without it
            // This is fine for production where env vars are set differently
            System.out.println("Note: .env file not found or couldn't be loaded. Using system environment variables.");
        }
        
        SpringApplication.run(ScsmsApiServiceApplication.class, args);
    }
}
