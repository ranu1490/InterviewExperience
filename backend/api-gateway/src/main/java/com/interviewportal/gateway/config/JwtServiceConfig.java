package com.interviewportal.gateway.config;

import com.interviewportal.common.security.JwtProperties;
import com.interviewportal.common.security.JwtService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Exposes the shared {@link JwtService} as a Spring bean so the gateway filter can validate tokens.
 */
@Configuration
public class JwtServiceConfig {

    @Bean
    public JwtService jwtService(JwtProperties properties) {
        return new JwtService(properties);
    }
}
