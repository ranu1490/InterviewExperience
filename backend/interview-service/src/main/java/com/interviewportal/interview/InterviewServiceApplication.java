package com.interviewportal.interview;

import com.interviewportal.common.security.JwtProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

/**
 * Interview Service — owns interview experiences and all engagement around them (likes, comments,
 * reports) plus AI difficulty analysis.
 *
 * <p>Separated from the user-service because content is the read-heavy, high-volume part of the
 * system (anonymous browsing) and benefits from independent scaling, caching and a schema tuned
 * for search. It stores only a denormalised {@code authorId}/{@code authorUsername}, never a
 * foreign key into the user database — each service owns its own data (the microservice rule).
 */
@SpringBootApplication
@EnableConfigurationProperties(JwtProperties.class)
public class InterviewServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(InterviewServiceApplication.class, args);
    }
}
