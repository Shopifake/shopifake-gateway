package com.shopifake.microservice.config;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.cors.reactive.CorsWebFilter;

/**
 * Tests for CORS configuration
 */
@SpringBootTest
@ActiveProfiles("test")
class CorsConfigTest {

    @Autowired
    private CorsConfig corsConfig;

    @Autowired
    private CorsWebFilter corsWebFilter;

    @Test
    void contextLoads() {
        assertThat(corsConfig).isNotNull();
    }

    @Test
    void corsWebFilterBeanIsCreated() {
        assertThat(corsWebFilter).isNotNull();
    }

    @Test
    void corsConfigPropertiesAreLoaded() {
        // Test profile should have permissive CORS
        assertThat(corsConfig.getAllowedOrigins()).isEqualTo("*");
        assertThat(corsConfig.getAllowedMethods()).contains("GET", "POST");
        assertThat(corsConfig.getAllowedHeaders()).isEqualTo("*");
        assertThat(corsConfig.getAllowCredentials()).isFalse();
    }
}
