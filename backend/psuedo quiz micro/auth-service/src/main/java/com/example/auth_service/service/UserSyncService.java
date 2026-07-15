package com.example.auth_service.service;

import com.example.auth_service.dto.UserSyncDTO;
import com.example.auth_service.entity.Users;
import com.example.auth_service.feign.QuestionServiceFeignClient;
import com.example.auth_service.feign.QuizServiceFeignClient;
import com.example.auth_service.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


@Service
@RequiredArgsConstructor
@Slf4j
public class UserSyncService {

    private final QuestionServiceFeignClient questionClient;

    private final QuizServiceFeignClient quizClient;

    private final UserRepository userRepository;

    public void syncUser(Users user) {

        UserSyncDTO dto = UserSyncDTO.builder()
                .authServiceId(user.getAuthServiceId())
                .username(user.getUsername())
                .password(user.getPassword())
                .email(user.getEmail())
                .phoneNumber(user.getPhoneNumber())
                .profilePicture(user.getProfilePicture())
                .resetToken(user.getResetToken())
                .roles(user.getRoles())
                .enabled(user.isEnabled())
                .accountNonExpired(user.isAccountNonExpired())
                .accountNonLocked(user.isAccountNonLocked())
                .credentialsNonExpired(user.isCredentialsNonExpired())
                .build();

        log.info("------------------------------------------------");
        log.info("Synchronizing user : {}", user.getUsername());

        syncQuestionService(dto);

        syncQuizService(dto);

        log.info("Finished synchronization : {}", user.getUsername());
        log.info("------------------------------------------------");
    }

    @Retryable(
            retryFor = Exception.class,
            maxAttempts = 5,
            backoff = @Backoff(delay = 3000)
    )
    public void syncQuestionService(UserSyncDTO dto) {

        questionClient.syncUser(dto);

        log.info("Question-Service synchronized successfully.");
    }

    @Retryable(
            retryFor = Exception.class,
            maxAttempts = 5,
            backoff = @Backoff(delay = 3000)
    )
    public void syncQuizService(UserSyncDTO dto) {

        quizClient.syncUser(dto);

        log.info("Quiz-Service synchronized successfully.");
    }

    /**
     * Synchronize every user.
     */
    @Transactional
    public void synchronizeAllUsers() {

        log.info("========================================================");
        log.info("Running Full User Synchronization");
        log.info("========================================================");

        List<Users> users = userRepository.findAll();

        for (Users user : users) {

            try {

                syncUser(user);

            } catch (Exception ex) {

                log.error(
                        "Synchronization failed for {}",
                        user.getUsername(),
                        ex);

            }

        }

        log.info("========================================================");
        log.info("Full synchronization completed.");
        log.info("========================================================");
    }

    /**
     * Validate startup users.
     */
    @Transactional
    public void validateUsers() {

        List<Users> users = userRepository.findAll();

        log.info("========================================================");
        log.info("Validation");
        log.info("========================================================");

        log.info("Total Users : {}", users.size());

        users.forEach(user ->

                log.info(
                        "AUTH_ID={} | USERNAME={} | EMAIL={} | ROLE={}",
                        user.getAuthServiceId(),
                        user.getUsername(),
                        user.getEmail(),
                        user.getRoles())
        );

        log.info("========================================================");
    }
}
