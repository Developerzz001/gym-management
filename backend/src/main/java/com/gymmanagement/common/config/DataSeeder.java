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

    private static final String DEFAULT_SUPER_ADMIN_EMAIL = "superadmin@gymmanagement.com";
    private static final String DEFAULT_SUPER_ADMIN_PASSWORD = "SuperAdmin@123";

    @Override
    public void run(String... args) {
        seedUser(DEFAULT_SUPER_ADMIN_EMAIL, DEFAULT_SUPER_ADMIN_PASSWORD, "Super", "Administrator", Role.SUPER_ADMIN);
    }

    private void seedUser(String email, String password, String firstName, String lastName, Role role) {
        if (userRepository.findByEmailIgnoreCase(email).isPresent()) {
            return;
        }
        User user = User.builder()
            .firstName(firstName)
                .lastName(lastName)
                .email(email)
                .mobileNumber("0000000000")
                .password(passwordEncoder.encode(password))
                .role(role)
                .active(true)
                .build();
        userRepository.save(user);
        log.info("Default {} account created -> email: {}", role, email);
    }
}
