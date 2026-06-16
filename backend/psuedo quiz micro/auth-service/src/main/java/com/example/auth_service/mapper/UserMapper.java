package com.example.auth_service.mapper;

import com.example.auth_service.dto.LoginResponseDTO;
import com.example.auth_service.dto.SignUpRequestDTO;
import com.example.auth_service.dto.SignUpResponseDTO;
import com.example.auth_service.dto.UserDto;
import com.example.auth_service.entity.Users;
import com.example.auth_service.enums.UserRoles;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class UserMapper {

    // ================= ENTITY → DTO =================


    // ================= DTO → ENTITY =================


    public static Users toEntity(UserDto dto) {
        if (dto == null) return null;

        Users user = new Users();
        user.setId(dto.getId());
        user.setUsername(dto.getUsername());
        user.setEmail(dto.getEmail());
        user.setPhoneNumber(dto.getPhoneNumber());
        user.setProfilePicture(dto.getProfilePicture());

        if (dto.getRole() != null) {
            user.setRoles(UserRoles.valueOf(dto.getRole()));
        }

        user.setEnabled(dto.isEnabled());
        user.setAccountNonExpired(dto.isAccountNonExpired());
        user.setAccountNonLocked(dto.isAccountNonLocked());
        user.setCredentialsNonExpired(dto.isCredentialsNonExpired());

        user.setCreatedAt(dto.getCreatedAt());
        user.setUpdatedAt(dto.getUpdatedAt());

        return user;
    }

    // ================= UPDATE HELPER =================

    public static void updateEntity(Users user, UserDto dto) {
        if (user == null || dto == null) return;

        user.setUsername(dto.getUsername());
        user.setEmail(dto.getEmail());
        user.setPhoneNumber(dto.getPhoneNumber());
        user.setProfilePicture(dto.getProfilePicture());

        if (dto.getRole() != null) {
            user.setRoles(UserRoles.valueOf(dto.getRole()));
        }

        user.setEnabled(dto.isEnabled());
        user.setAccountNonExpired(dto.isAccountNonExpired());
        user.setAccountNonLocked(dto.isAccountNonLocked());
        user.setCredentialsNonExpired(dto.isCredentialsNonExpired());

        user.setUpdatedAt(LocalDateTime.now());
    }


    public static Users toEntity(SignUpRequestDTO dto) {
        Users user = new Users();
        user.setUsername(dto.getUsername());
        user.setEmail(dto.getEmail());
        user.setPhoneNumber(dto.getPhoneNumber());
        user.setPassword(dto.getPassword()); // raw — encoded in service

        user.setEnabled(true);
        user.setAccountNonExpired(true);
        user.setAccountNonLocked(true);
        user.setCredentialsNonExpired(true);

        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());

        return user;
    }

    // Map entity to full UserDto including synced counters
    public static UserDto toDTO(Users user) {
        return UserDto.builder()
                .id(user.getId())
                .authServiceId(user.getAuthServiceId()) // expose global sync ID
                .username(user.getUsername())
                .email(user.getEmail())
                .role(user.getRoles() != null ? user.getRoles().name() : null)
                .phoneNumber(user.getPhoneNumber())
                .profilePicture(user.getProfilePicture())
                .questionsCreatedCount(user.getQuestionsCreatedCount())
                .quizzesCreatedCount(user.getQuizzesCreatedCount())
                .quizzesTakenCount(user.getQuizzesTakenCount())
                .enabled(user.isEnabled())
                .accountNonExpired(user.isAccountNonExpired())
                .accountNonLocked(user.isAccountNonLocked())
                .credentialsNonExpired(user.isCredentialsNonExpired())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }

    public static SignUpResponseDTO toSignUpResponse(Users user) {
        return SignUpResponseDTO.builder()
                .id(user.getId())
                .authServiceId(user.getAuthServiceId())
                .username(user.getUsername())
                .email(user.getEmail())
                .role(user.getRoles() != null ? user.getRoles().name() : null)
                .build();
    }

    public static LoginResponseDTO toLoginResponse(Users user, String token) {
        return LoginResponseDTO.builder()
                .id(user.getId())
                .authServiceId(user.getAuthServiceId())
                .username(user.getUsername())
                .email(user.getEmail())
                .role(user.getRoles() != null ? user.getRoles().name() : null)
                .token(token)
                .build();
    }
}
