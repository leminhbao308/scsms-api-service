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
                .filter(s -> !s.isEmpty())
                .toArray(String[]::new);
        
        log.info("CORS Configuration - Allowed Origin Patterns: {}", Arrays.toString(origins));
        
        // Kiểm tra nếu có "*" pattern
        boolean useWildcard = origins.length == 1 && origins[0].equals("*");
        
        // CORS for all endpoints including WebSocket SockJS info endpoint
        if (useWildcard) {
            // Với "*", không thể dùng allowCredentials(true) - browser sẽ reject
            registry.addMapping("/**")
                .allowedOriginPatterns(origins)
                .allowedMethods("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(false)
                .maxAge(3600);
        } else {
            // Với origins cụ thể, có thể dùng allowCredentials(true)
            registry.addMapping("/**")
                .allowedOriginPatterns(origins)
                .allowedMethods("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600);
        }
        
        // Explicit CORS for WebSocket endpoints (SockJS info endpoint is HTTP GET)
        // Register both /ws/** and /api/ws/** to ensure compatibility with context-path
        if (useWildcard) {
            registry.addMapping("/ws/**")
                .allowedOriginPatterns(origins)
                .allowedMethods("GET", "POST", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(false)
                .maxAge(3600);
            
            registry.addMapping("/ws-native/**")
                .allowedOriginPatterns(origins)
                .allowedMethods("GET", "POST", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(false)
                .maxAge(3600);
            
            registry.addMapping("/api/ws/**")
                .allowedOriginPatterns(origins)
                .allowedMethods("GET", "POST", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(false)
                .maxAge(3600);
            
            registry.addMapping("/api/ws-native/**")
                .allowedOriginPatterns(origins)
                .allowedMethods("GET", "POST", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(false)
                .maxAge(3600);
        } else {
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
    }
    
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        
        // Cho phép các origin patterns từ config - trim và loại bỏ khoảng trắng
        List<String> origins = Arrays.stream(allowedOriginPatterns.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());
        
        log.info("CORS Configuration Source - Allowed Origin Patterns: {}", origins);
        log.info("CORS Configuration Source - Number of origins: {}", origins.size());
        
        // Nếu có "*", xử lý đặc biệt để tương thích với allowCredentials
        if (origins.size() == 1 && origins.get(0).equals("*")) {
            // Với "*", không thể dùng allowCredentials(true)
            // Nhưng có thể dùng allowedOriginPatterns("*") với allowCredentials(false)
            // Hoặc set cụ thể các origins
            configuration.setAllowedOriginPatterns(origins);
            // Tạm thời set allowCredentials = false nếu dùng "*"
            // Hoặc có thể set cụ thể localhost:3000, localhost:8081, etc.
            configuration.setAllowCredentials(false);
            log.warn("CORS: Using '*' pattern with allowCredentials=false for compatibility");
        } else {
            configuration.setAllowedOriginPatterns(origins);
            configuration.setAllowCredentials(true);
            log.info("CORS: Using specific origins with allowCredentials=true");
        }
        
        // Cho phép các HTTP methods
        configuration.setAllowedMethods(Arrays.asList(
            "GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"
        ));
        
        // Cho phép tất cả headers
        configuration.setAllowedHeaders(List.of("*"));
        
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
        if (origins.size() == 1 && origins.get(0).equals("*")) {
            wsConfiguration.setAllowedOriginPatterns(origins);
            wsConfiguration.setAllowCredentials(false);
        } else {
            wsConfiguration.setAllowedOriginPatterns(origins);
            wsConfiguration.setAllowCredentials(true);
        }
        wsConfiguration.setAllowedMethods(Arrays.asList("GET", "POST", "OPTIONS"));
        wsConfiguration.setAllowedHeaders(List.of("*"));
        wsConfiguration.setMaxAge(3600L);
        source.registerCorsConfiguration("/ws/**", wsConfiguration);
        source.registerCorsConfiguration("/ws-native/**", wsConfiguration);
        source.registerCorsConfiguration("/api/ws/**", wsConfiguration);
        source.registerCorsConfiguration("/api/ws-native/**", wsConfiguration);
        
        log.info("CORS Configuration Source - Registered WebSocket CORS configs for: /ws/**, /ws-native/**, /api/ws/**, /api/ws-native/**");
        log.info("CORS Configuration Source - WebSocket CORS allowCredentials: {}", wsConfiguration.getAllowCredentials());
        log.info("CORS Configuration Source - WebSocket CORS allowedMethods: {}", wsConfiguration.getAllowedMethods());
        
        return source;
    }
}
