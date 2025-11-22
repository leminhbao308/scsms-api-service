package com.kltn.scsms_api_service.configs.security;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.lang.NonNull;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Configuration
public class CorsConfig implements WebMvcConfigurer {
    
    @Value("${app.cors.allowed-origin-patterns}")
    private String allowedOriginPatterns;
    
    @Override
    public void addCorsMappings(@NonNull CorsRegistry registry) {
        // Trim và loại bỏ khoảng trắng
        String[] origins = Arrays.stream(allowedOriginPatterns.split(","))
                .map(String::trim)
                .toArray(String[]::new);
        
        log.info("CORS Configuration - Allowed Origin Patterns: {}", Arrays.toString(origins));
        
        // CORS for all endpoints including WebSocket SockJS info endpoint
        registry.addMapping("/**")
            .allowedOriginPatterns(origins)
            .allowedMethods("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS")
            .allowedHeaders("*")
            .allowCredentials(true)
            .maxAge(3600);
        
        // Explicit CORS for WebSocket endpoints (SockJS info endpoint is HTTP GET)
        // Register both /ws/** and /api/ws/** to ensure compatibility with context-path
        registry.addMapping("/ws/**")
            .allowedOriginPatterns(origins)
            .allowedMethods("GET", "POST", "OPTIONS")
            .allowedHeaders("*")
            .allowCredentials(true)
            .maxAge(3600);
        
        registry.addMapping("/ws-native/**")
            .allowedOriginPatterns(origins)
            .allowedMethods("GET", "POST", "OPTIONS")
            .allowedHeaders("*")
            .allowCredentials(true)
            .maxAge(3600);
        
        // Also register with context-path prefix for compatibility
        registry.addMapping("/api/ws/**")
            .allowedOriginPatterns(origins)
            .allowedMethods("GET", "POST", "OPTIONS")
            .allowedHeaders("*")
            .allowCredentials(true)
            .maxAge(3600);
        
        registry.addMapping("/api/ws-native/**")
            .allowedOriginPatterns(origins)
            .allowedMethods("GET", "POST", "OPTIONS")
            .allowedHeaders("*")
            .allowCredentials(true)
            .maxAge(3600);
    }
    
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        
        // Cho phép các origin patterns từ config - trim và loại bỏ khoảng trắng
        List<String> origins = Arrays.stream(allowedOriginPatterns.split(","))
                .map(String::trim)
                .collect(Collectors.toList());
        
        log.info("CORS Configuration Source - Allowed Origin Patterns: {}", origins);
        
        configuration.setAllowedOriginPatterns(origins);
        
        // Cho phép các HTTP methods
        configuration.setAllowedMethods(Arrays.asList(
            "GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"
        ));
        
        // Cho phép tất cả headers
        configuration.setAllowedHeaders(List.of("*"));
        
        // Cho phép credentials (cookies, authorization headers)
        configuration.setAllowCredentials(true);
        
        // Expose headers cho client
        configuration.setExposedHeaders(Arrays.asList(
            "Authorization",
            "Content-Type",
            "X-Total-Count",
            "X-Total-Pages"
        ));
        
        // Cache preflight response trong 1 giờ
        configuration.setMaxAge(3600L);
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        
        // Explicit CORS configuration for WebSocket endpoints
        // Register both /ws/** and /api/ws/** to ensure compatibility with context-path
        CorsConfiguration wsConfiguration = new CorsConfiguration();
        wsConfiguration.setAllowedOriginPatterns(origins);
        wsConfiguration.setAllowedMethods(Arrays.asList("GET", "POST", "OPTIONS"));
        wsConfiguration.setAllowedHeaders(List.of("*"));
        wsConfiguration.setAllowCredentials(true);
        wsConfiguration.setMaxAge(3600L);
        source.registerCorsConfiguration("/ws/**", wsConfiguration);
        source.registerCorsConfiguration("/ws-native/**", wsConfiguration);
        source.registerCorsConfiguration("/api/ws/**", wsConfiguration);
        source.registerCorsConfiguration("/api/ws-native/**", wsConfiguration);
        
        return source;
    }
}
