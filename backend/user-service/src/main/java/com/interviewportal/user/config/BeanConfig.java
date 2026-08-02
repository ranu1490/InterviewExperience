package com.interviewportal.user.config;

import com.interviewportal.common.security.JwtProperties;
import com.interviewportal.common.security.JwtService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

/**
 * Wiring for shared infrastructure beans.
 */
@Configuration
public class BeanConfig {

    @Bean
    public JwtService jwtService(JwtProperties properties) {
        return new JwtService(properties);
    }

    /** Used by the Google ID-token verifier to call Google's public tokeninfo endpoint. */
    @Bean
    public RestClient restClient() {
        return RestClient.create();
    }
}
