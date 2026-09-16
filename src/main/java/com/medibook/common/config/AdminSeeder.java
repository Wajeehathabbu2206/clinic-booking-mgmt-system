package com.medibook.common.config;

import com.medibook.common.util.Role;
import com.medibook.user.entity.User;
import com.medibook.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class AdminSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${medibook.admin.email}")
    private String adminEmail;

    @Value("${medibook.admin.password}")
    private String adminPassword;

    @Value("${medibook.admin.full-name}")
    private String adminFullName;

    @Override
    public void run(String... args) {
        if (userRepository.existsByEmail(adminEmail)) {
            log.info("Admin user already exists, skipping seed");
            return;
        }

        User admin = new User();
        admin.setEmail(adminEmail);
        admin.setPassword(passwordEncoder.encode(adminPassword));
        admin.setFullName(adminFullName);
        admin.setRole(Role.SUPER_ADMIN);
        admin.setEnabled(true);

        userRepository.save(admin);
        log.info("Seeded SUPER_ADMIN user: {}", adminEmail);
    }
}