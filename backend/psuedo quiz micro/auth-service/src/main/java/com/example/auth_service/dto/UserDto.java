package com.example.auth_service.dto;

import lombok.*;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserDto {
    private Long id;
    private Long authServiceId;
    private String username;
    private String email;
    private String phoneNumber;
    private String profilePicture;
    private String role;

    private int questionsCreatedCount;
    private int quizzesCreatedCount;
    private int quizzesTakenCount;

    private boolean enabled;
    private boolean accountNonExpired;
    private boolean accountNonLocked;
    private boolean credentialsNonExpired;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
