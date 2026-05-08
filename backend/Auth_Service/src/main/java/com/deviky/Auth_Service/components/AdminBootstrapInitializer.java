package com.deviky.Auth_Service.components;

import com.deviky.Auth_Service.models.Role;
import com.deviky.Auth_Service.models.User;
import com.deviky.Auth_Service.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Slf4j
@Component
@RequiredArgsConstructor
public class AdminBootstrapInitializer implements ApplicationRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.bootstrap-admin.email:}")
    private String adminEmail;

    @Value("${app.bootstrap-admin.password:}")
    private String adminPassword;

    @Override
    public void run(ApplicationArguments args) {
        String email = adminEmail == null ? "" : adminEmail.trim();
        String password = adminPassword == null ? "" : adminPassword.trim();

        if (!StringUtils.hasText(email) || !StringUtils.hasText(password)) {
            log.info("Bootstrap admin is not configured. Set APP_BOOTSTRAP_ADMIN_EMAIL and APP_BOOTSTRAP_ADMIN_PASSWORD to enable it.");
            return;
        }

        userRepository.findByUsername(email).ifPresentOrElse(existingUser -> {
            if (existingUser.getRole() != Role.ADMIN) {
                log.warn("Bootstrap admin skipped: user {} already exists with role {}", email, existingUser.getRole());
                return;
            }

            if (!existingUser.isEmailConfirmed()) {
                existingUser.setEmailConfirmed(true);
                userRepository.save(existingUser);
                log.info("Existing bootstrap admin {} marked as email-confirmed.", email);
                return;
            }

            log.info("Bootstrap admin {} already exists.", email);
        }, () -> {
            User admin = User.builder()
                    .username(email)
                    .password(passwordEncoder.encode(password))
                    .role(Role.ADMIN)
                    .emailConfirmed(true)
                    .build();

            userRepository.save(admin);
            log.info("Bootstrap admin {} created from environment variables.", email);
        });
    }
}
