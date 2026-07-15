package com.example.auth_service.service;

import com.example.auth_service.entity.Users;
import com.example.auth_service.enums.UserRoles;
import com.example.auth_service.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
@ConditionalOnProperty(
        name = "app.test-users.enabled",
        havingValue = "true",
        matchIfMissing = false
)
public class TestUserInitializer implements ApplicationRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserSyncService userSyncService;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {

        log.info("========================================================");
        log.info("Initializing Demo/Test Users");
        log.info("========================================================");

        createUser(
                "admin",
                "anadINatu2001+admin@gmail.com",
                "8318428125",
                "Admin@123",
                UserRoles.ADMIN
        );

        createUser(
                "participant",
                "anadINatu2001+participant@gmail.com",
                "8707625812",
                "Participant@123",
                UserRoles.PARTICIPANT
        );

        createUser(
                "curator",
                "anadINatu2001+curator@gmail.com",
                "9415022771",
                "Curator@123",
                UserRoles.CURATOR
        );

        log.info("Demo users initialized.");

        userSyncService.synchronizeAllUsers();

        userSyncService.validateUsers();

        log.info("Demo initialization completed successfully.");
    }

    private Users createUser(
            String username,
            String email,
            String phone,
            String password,
            UserRoles role) {

        return userRepository.findByEmailIgnoreCase(email)
                .map(existing -> {

                    log.info("{} already exists.", username);

                    if (existing.getAuthServiceId() == null) {

                        existing.setAuthServiceId(existing.getId());

                        return userRepository.save(existing);
                    }

                    return existing;

                })
                .orElseGet(() -> {

                    Users user = Users.builder()
                            .username(username)
                            .email(email)
                            .phoneNumber(phone)
                            .password(passwordEncoder.encode(password))
                            .roles(role)
                            .enabled(true)
                            .accountNonExpired(true)
                            .accountNonLocked(true)
                            .credentialsNonExpired(true)
                            .questionsCreatedCount(0)
                            .quizzesCreatedCount(0)
                            .quizzesTakenCount(0)
                            .build();

                    Users saved = userRepository.save(user);

                    saved.setAuthServiceId(saved.getId());

                    saved = userRepository.save(saved);

                    log.info(
                            "Created {} | authServiceId={} | role={}",
                            saved.getUsername(),
                            saved.getAuthServiceId(),
                            saved.getRoles());

                    return saved;

                });
    }
}
