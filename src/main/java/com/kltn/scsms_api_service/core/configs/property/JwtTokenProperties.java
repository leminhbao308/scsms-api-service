package com.kltn.scsms_api_service.core.configs.property;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "app.jwt")
@Data
public class JwtTokenProperties {
    
    private String secretKey;
    private String publicKey;
    
    private AccessToken accessToken = new AccessToken();
    private RefreshToken refreshToken = new RefreshToken();
    private RememberMe rememberMe = new RememberMe();
    private Otp otp = new Otp();
    private ResetPassword resetPassword = new ResetPassword();
    
    public Long getAccessTokenExpiresIn() {
        return accessToken.getExpiresIn();
    }
    
    public Long getRefreshTokenExpiresIn() {
        return refreshToken.getExpiresIn();
    }
    
    public Long getRememberMeExpiresIn() {
        return rememberMe.getExpiresIn();
    }
    
    public Long getOtpExpiresIn() {
        return otp.getExpiresIn();
    }
    
    public Long getResetPasswordExpiresIn() {
        return resetPassword.getExpiresIn();
    }
    
    @Data
    private static class AccessToken {
        private Long expiresIn;
    }
    
    @Data
    private static class RefreshToken {
        private Long expiresIn;
    }
    
    @Data
    private static class RememberMe {
        private Long expiresIn;
    }
    
    @Data
    private static class Otp {
        private Long expiresIn;
    }
    
    @Data
    private static class ResetPassword {
        private Long expiresIn;
    }
}
