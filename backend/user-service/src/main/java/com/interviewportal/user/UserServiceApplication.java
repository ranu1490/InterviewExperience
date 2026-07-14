package com.interviewportal.user;

import com.interviewportal.common.security.JwtProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

/**
 * User Service — owns everything about identity: signup, login, Google login, JWT issuance,
 * refresh-token rotation, profiles and role/ban management.
 *
 * <p>Kept as a separate microservice because identity has a distinct lifecycle, stricter
 * security requirements and a different scaling profile than content. Isolating it means an
 * auth outage or deploy does not necessarily take down browsing of interview experiences.
 */
@SpringBootApplication
@EnableConfigurationProperties(JwtProperties.class)
public class UserServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(UserServiceApplication.class, args);
    }
}
