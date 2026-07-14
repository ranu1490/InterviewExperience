package com.interviewportal.user.config;

import com.interviewportal.user.entity.AuthProvider;
import com.interviewportal.user.entity.Role;
import com.interviewportal.user.entity.User;
import com.interviewportal.user.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Set;

/**
 * Seeds a demo ADMIN account on first startup so the admin features are usable out of the box.
 * Idempotent: it does nothing if the account already exists. In production you would create the
 * first admin via a migration or an ops runbook instead.
 */
@Component
public class DataSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataSeeder.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final String adminEmail;
    private final String adminUsername;
    private final String adminPassword;

    public DataSeeder(UserRepository userRepository,
                      PasswordEncoder passwordEncoder,
                      @Value("${app.seed.admin-email}") String adminEmail,
                      @Value("${app.seed.admin-username}") String adminUsername,
                      @Value("${app.seed.admin-password}") String adminPassword) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.adminEmail = adminEmail;
        this.adminUsername = adminUsername;
        this.adminPassword = adminPassword;
    }

    @Override
    public void run(String... args) {
        if (userRepository.existsByEmail(adminEmail)) {
            return;
        }
        User admin = User.builder()
                .username(adminUsername)
                .email(adminEmail)
                .password(passwordEncoder.encode(adminPassword))
                .fullName("Portal Administrator")
                .provider(AuthProvider.LOCAL)
                .roles(Set.of(Role.USER, Role.ADMIN))
                .build();
        userRepository.save(admin);
        log.info("Seeded default admin account: {} / {}", adminUsername, adminEmail);
    }
}
