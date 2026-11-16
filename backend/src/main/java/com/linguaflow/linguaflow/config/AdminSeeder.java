package com.linguaflow.linguaflow.config;

import com.linguaflow.linguaflow.entity.User;
import com.linguaflow.linguaflow.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AdminSeeder implements CommandLineRunner {

    private static final String ADMIN_USERNAME = "admin";
    private static final String DEFAULT_PASSWORD = "123456";

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        if (userRepository.findByUsername(ADMIN_USERNAME).isPresent()) {
            return;
        }

        userRepository.save(User.builder()
                .username(ADMIN_USERNAME)
                .passwordHash(passwordEncoder.encode(DEFAULT_PASSWORD))
                .fullName("Super Admin")
                .role("ROLE_SUPER_ADMIN")
                .active(true)
                .build());
    }
}
