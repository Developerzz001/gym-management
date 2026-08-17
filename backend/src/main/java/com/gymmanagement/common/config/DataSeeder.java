package com.gymmanagement.common.config;

import com.gymmanagement.user.Role;
import com.gymmanagement.user.User;
import com.gymmanagement.user.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    private static final String DEFAULT_ADMIN_EMAIL = "admin@gymmanagement.com";
    private static final String DEFAULT_ADMIN_PASSWORD = "Admin@123";

    @Override
    public void run(String... args) {
        if (userRepository.existsByEmailIgnoreCase(DEFAULT_ADMIN_EMAIL)) {
            return;
        }
        User admin = User.builder()
                .firstName("System")
                .lastName("Administrator")
                .email(DEFAULT_ADMIN_EMAIL)
                .mobileNumber("0000000000")
                .password(passwordEncoder.encode(DEFAULT_ADMIN_PASSWORD))
                .role(Role.ADMIN)
                .active(true)
                .build();
        userRepository.save(admin);
        log.info("==============================================================");
        log.info("Default ADMIN account created -> email: {} | password: {}", DEFAULT_ADMIN_EMAIL, DEFAULT_ADMIN_PASSWORD);
        log.info("==============================================================");
    }
}
