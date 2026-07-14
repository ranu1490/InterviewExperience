package com.interviewportal.interview.config;

import com.interviewportal.common.security.JwtProperties;
import com.interviewportal.common.security.JwtService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Wires the shared {@link JwtService} for token validation in this service. */
@Configuration
public class BeanConfig {

    @Bean
    public JwtService jwtService(JwtProperties properties) {
        return new JwtService(properties);
    }
}
