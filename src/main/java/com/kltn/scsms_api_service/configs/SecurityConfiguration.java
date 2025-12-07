package com.kltn.scsms_api_service.configs;

import com.kltn.scsms_api_service.configs.filters.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.firewall.HttpFirewall;
import org.springframework.security.web.firewall.StrictHttpFirewall;
import org.springframework.web.cors.CorsConfigurationSource;

@Slf4j
@Configuration
@EnableMethodSecurity(prePostEnabled = true)
@EnableAspectJAutoProxy
@RequiredArgsConstructor
public class SecurityConfiguration {
    
    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final CorsConfigurationSource corsConfigurationSource;
    
    /**
     * Configure HttpFirewall to allow large cookie headers
     * StrictHttpFirewall by default rejects cookie headers that are too long
     * or contain special characters (like JSON in cookies)
     */
    @Bean
    public HttpFirewall httpFirewall() {
        StrictHttpFirewall firewall = new StrictHttpFirewall();
        // Allow large header values (default validation is very strict)
        // Cookie header can contain JWT tokens and JSON data, so we need to allow larger values
        // setAllowedHeaderValues accepts a Predicate<String> that validates header values
        // We allow values up to 8192 characters (typical HTTP header limit)
        firewall.setAllowedHeaderValues(headerValue -> {
            // Allow header values up to 8192 characters
            // This is necessary for cookies containing JWT tokens and JSON data
            return headerValue.length() <= 8192;
        });
        log.info("SecurityConfiguration: HttpFirewall configured to allow large header values (up to 8192 characters)");
        return firewall;
    }
    
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        log.info("SecurityConfiguration: Configuring SecurityFilterChain");
        return http.csrf(AbstractHttpConfigurer::disable)
            .cors(cors -> {
                cors.configurationSource(corsConfigurationSource);
                log.info("SecurityConfiguration: CORS configured with source: {}", corsConfigurationSource.getClass().getName());
            })
            .sessionManagement(
                configurer -> configurer.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .headers(
                configurer -> configurer.frameOptions(HeadersConfigurer.FrameOptionsConfig::disable))
            // Configure anonymous authentication explicitly for permitAll() to work with STATELESS
            .anonymous(anonymous -> anonymous
                .principal("anonymousUser")
                .authorities(org.springframework.security.core.authority.AuthorityUtils.createAuthorityList("ROLE_ANONYMOUS")))
            .authorizeHttpRequests(auth -> {
                log.info("SecurityConfiguration: Configuring authorization rules");
                auth
                    // CRITICAL: Allow OPTIONS requests (CORS preflight) for all paths
                    .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                    // WebSocket endpoints - phải đặt trước các pattern khác
                    // Cho phép cả với và không có context-path
                    // SockJS info endpoint (/api/ws/info) là HTTP GET request, cần permitAll
                    .requestMatchers("/ws/**", "/ws-native/**", "/api/ws/**", "/api/ws-native/**").permitAll()
                    // Auth endpoints
                    .requestMatchers("/auth/login", "/auth/register", "/auth/refresh-token", 
                        "/auth/logout", "/auth/forgot-password", "/auth/reset-password",
                        "/auth/verify-token", "/auth/oauth2/**").permitAll()
                    // OTP endpoints
                    .requestMatchers("/otp/**").permitAll()
                    // Actuator endpoints
                    .requestMatchers("/actuator/**").permitAll()
                    // Swagger/Docs endpoints
                    .requestMatchers("/docs/**", "/swagger-ui/**", "/swagger-resources/**", 
                        "/webjars/**", "/swagger-ui.html", "/v3/api-docs/**").permitAll()
                    // Error endpoint - must be permitted for error handling
                    .requestMatchers("/error").permitAll()
                    // Public GET endpoints - Guest can view without authentication
                    // NOTE: Context-path is /api, so Spring Security removes it before pattern matching
                    // Products - GET only (Guest can view products)
                    .requestMatchers(HttpMethod.GET, "/products/**").permitAll()
                    // Services - GET only (Guest can view services)
                    .requestMatchers(HttpMethod.GET, "/services/**").permitAll()
                    // Service Types - GET dropdown and active (Guest can view service types)
                    .requestMatchers(HttpMethod.GET, "/service-types/dropdown").permitAll()
                    .requestMatchers(HttpMethod.GET, "/service-types/active").permitAll()
                    // Centers - GET get-all only (Guest can view centers list)
                    .requestMatchers(HttpMethod.GET, "/centers/get-all").permitAll()
                    // Branches - GET only (Guest can view branches)
                    .requestMatchers(HttpMethod.GET, "/branches/**").permitAll()
                    // Pricing endpoints - Guest can view prices (POST because they have request body)
                    .requestMatchers(HttpMethod.POST, "/pricing/preview").permitAll()
                    .requestMatchers(HttpMethod.POST, "/pricing/preview-batch").permitAll()
                    .requestMatchers(HttpMethod.POST, "/pricing/batch-service-prices").permitAll()
                    // Media endpoints - Guest can view media/images (GET only)
                    .requestMatchers(HttpMethod.GET, "/media/entity/**").permitAll()
                    // All other requests require authentication
                    .anyRequest().authenticated();
                log.info("SecurityConfiguration: Public GET endpoints permitted, anyRequest authenticated");
            })
            // JWT filter chỉ áp dụng cho các request không phải WebSocket
            // Add JWT filter AFTER AnonymousAuthenticationFilter to allow anonymous auth to be set first
            // This ensures permitAll() works correctly with STATELESS session
            .addFilterAfter(jwtAuthenticationFilter, org.springframework.security.web.authentication.AnonymousAuthenticationFilter.class)
            .build();
    }
    
    /**
     * Apply custom HttpFirewall to WebSecurity
     * This ensures the firewall configuration is used for all requests
     */
    @Bean
    public WebSecurityCustomizer webSecurityCustomizer() {
        return (web) -> {
            web.httpFirewall(httpFirewall());
            log.info("SecurityConfiguration: WebSecurityCustomizer configured with custom HttpFirewall");
        };
    }
    
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
