package com.studyhub.controllers;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfig {

    // Solo il frontend può chiamare l'API dal browser (app.cors.allowed-origins,
    // lista separata da virgole). In sviluppo il proxy di Vite rende frontend e
    // API la stessa origine e il CORS non entra in gioco; serve se il frontend
    // viene servito da un dominio diverso dall'API.
    @Value("${app.cors.allowed-origins}")
    private String[] allowedOrigins;

    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/api/**")
                        .allowedOrigins(allowedOrigins)
                        .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
                        .allowedHeaders("*")
                        // Necessario perché il browser invii i cookie di
                        // sessione e CSRF; incompatibile con l'origine "*"
                        .allowCredentials(true);
            }
        };
    }
}
