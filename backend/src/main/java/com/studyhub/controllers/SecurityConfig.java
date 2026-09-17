package com.studyhub.controllers;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@Configuration
public class SecurityConfig {
    @Bean
    public BCryptPasswordEncoder securityConfigurer() {
        return new BCryptPasswordEncoder();
    }
}
