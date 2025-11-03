package com.shopifake.microservice.config;

import java.util.Arrays;
import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

import lombok.Data;

/**
 * CORS Configuration for Spring Cloud Gateway.
 * This configuration is profile-aware and reads CORS settings from application-{profile}.yml
 */
@Configuration
@ConfigurationProperties(prefix = "cors")
@Data
public class CorsConfig {

    private String allowedOrigins;
    private String allowedMethods;
    private String allowedHeaders;
    private Boolean allowCredentials;

    @Bean
    public CorsWebFilter corsWebFilter() {
        CorsConfiguration corsConfig = new CorsConfiguration();

        // Parse allowed origins
        if (allowedOrigins != null && !allowedOrigins.isEmpty()) {
            if ("*".equals(allowedOrigins.trim())) {
                corsConfig.addAllowedOriginPattern("*");
            } else {
                List<String> origins = Arrays.asList(allowedOrigins.split(","));
                origins.forEach(origin -> corsConfig.addAllowedOriginPattern(origin.trim()));
            }
        }

        // Parse allowed methods
        if (allowedMethods != null && !allowedMethods.isEmpty()) {
            List<String> methods = Arrays.asList(allowedMethods.split(","));
            methods.forEach(method -> corsConfig.addAllowedMethod(method.trim()));
        }

        // Parse allowed headers
        if (allowedHeaders != null && !allowedHeaders.isEmpty()) {
            if ("*".equals(allowedHeaders.trim())) {
                corsConfig.addAllowedHeader("*");
            } else {
                List<String> headers = Arrays.asList(allowedHeaders.split(","));
                headers.forEach(header -> corsConfig.addAllowedHeader(header.trim()));
            }
        }

        // Set allow credentials
        if (allowCredentials != null) {
            corsConfig.setAllowCredentials(allowCredentials);
        }

        // Max age for preflight requests
        corsConfig.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", corsConfig);

        return new CorsWebFilter(source);
    }
}
