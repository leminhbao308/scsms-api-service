package com.kltn.scsms_api_service.configs;

import com.kltn.scsms_api_service.configs.filters.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfigurationSource;

@Configuration
@EnableMethodSecurity(prePostEnabled = true)
@EnableAspectJAutoProxy
@RequiredArgsConstructor
public class SecurityConfiguration {
    
    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final CorsConfigurationSource corsConfigurationSource;
    
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http.csrf(AbstractHttpConfigurer::disable)
            .cors(cors -> cors.configurationSource(corsConfigurationSource))
            .sessionManagement(
                configurer -> configurer.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .headers(
                configurer -> configurer.frameOptions(HeadersConfigurer.FrameOptionsConfig::disable))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/ws/**", "/ws-native/**", "/api/ws/**", "/api/ws-native/**").permitAll()
                .requestMatchers("/auth/login", "/auth/register", "/auth/refresh-token", 
                    "/auth/logout", "/auth/forgot-password", "/auth/reset-password",
                    "/auth/verify-token", "/auth/oauth2/**").permitAll()
                .requestMatchers("/otp/**").permitAll()
                .requestMatchers("/actuator/**").permitAll()
                .requestMatchers("/docs/**", "/swagger-ui/**", "/swagger-resources/**", 
                    "/webjars/**", "/swagger-ui.html", "/v3/api-docs/**").permitAll()
                .anyRequest().authenticated()
            )
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
            .build();
    }
    
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
