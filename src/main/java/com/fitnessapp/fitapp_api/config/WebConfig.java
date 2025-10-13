package com.fitnessapp.fitapp_api.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

@Configuration
public class WebConfig {
    @Bean
    public CorsConfigurationSource corsConfigurationSource(@Value("${app.cors.origins}") String origins) {
        var cfg = new CorsConfiguration();
        // URLs externas (como el front) se pueden poner varias
        cfg.setAllowedOriginPatterns(Arrays.stream(origins.split(","))
                .map(String::trim).toList());
        cfg.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        cfg.setAllowedHeaders(List.of("Authorization", "Content-Type"));
        cfg.setAllowCredentials(false); // Si JWT en header => mejor false

        var source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", cfg); // Aplica a todos los endpoints
        return source;
    }
}