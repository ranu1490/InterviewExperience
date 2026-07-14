package com.interviewportal.gateway;

import com.interviewportal.common.security.JwtProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

/**
 * Entry point for the API Gateway.
 *
 * <p>The gateway is the single, stable front door for every client request. It hides the
 * internal service topology, centralises cross-cutting concerns (CORS, edge authentication,
 * routing) and lets us scale/relocate services without touching clients.
 */
@SpringBootApplication
@EnableConfigurationProperties(JwtProperties.class)
public class ApiGatewayApplication {

    public static void main(String[] args) {
        SpringApplication.run(ApiGatewayApplication.class, args);
    }
}
