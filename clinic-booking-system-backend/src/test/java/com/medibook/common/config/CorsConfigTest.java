package com.medibook.common.config;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class CorsConfigTest {

    @Test
    void exposesConfiguredOriginsToSpringSecurity() {
        CorsConfigurationSource source = new CorsConfig(
                "https://clinic.example, http://localhost:5173").corsConfigurationSource();

        CorsConfiguration configuration = source.getCorsConfiguration(
                new MockHttpServletRequest("GET", "/api/health"));

        assertNotNull(configuration);
        assertEquals(2, configuration.getAllowedOrigins().size());
        assertEquals("https://clinic.example", configuration.getAllowedOrigins().get(0));
    }
}