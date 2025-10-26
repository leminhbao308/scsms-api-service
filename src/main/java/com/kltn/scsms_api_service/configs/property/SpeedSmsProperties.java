package com.kltn.scsms_api_service.configs.property;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.Data;

@Data
@Component
@ConfigurationProperties(prefix = "speedsms")
public class SpeedSmsProperties {
    private String apiUrl;
    private String accessToken;
    private String brandname;
    private Otp otp = new Otp();

    @Data
    public static class Otp {
        private int length = 6;
        private int expiryMinutes = 5;
        private int maxAttempts = 3;
        private int cooldownMinutes = 1;
    }
}
