package com.example.question_service.mapper;

import com.example.question_service.dto.authDTO.LoginResponseDTO;
import com.example.question_service.dto.authDTO.SignUpRequestDTO;
import com.example.question_service.dto.authDTO.SignUpResponseDTO;
import com.example.question_service.dto.authDTO.UserDto;
import com.example.question_service.entity.Users;
import com.example.question_service.enums.UserRoles;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class UserMapper {

    // ================= ENTITY → DTO =================

    public static UserDto toDTO(Users user) {
        if (user == null) return null;

        return UserDto.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .phoneNumber(user.getPhoneNumber())
                .profilePicture(user.getProfilePicture())
                .role(user.getRoles() != null ? user.getRoles().name() : null)
                .enabled(user.isEnabled())
                .accountNonExpired(user.isAccountNonExpired())
                .accountNonLocked(user.isAccountNonLocked())
                .credentialsNonExpired(user.isCredentialsNonExpired())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }

    public static SignUpResponseDTO toSignUpResponse(Users user) {
        if (user == null) return null;

        return SignUpResponseDTO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .role(user.getRoles() != null ? user.getRoles().name() : null)
                .enabled(user.isEnabled())
                .build();
    }

    public static LoginResponseDTO toLoginResponse(Users user, String token) {
        if (user == null) return null;

        return LoginResponseDTO.builder()
                .token(token)
                .username(user.getUsername())
                .role(user.getRoles() != null ? user.getRoles().name() : null)
                .build();
    }

    // ================= DTO → ENTITY =================

    public static Users toEntity(SignUpRequestDTO dto) {
        if (dto == null) return null;

        Users user = new Users();
        user.setUsername(dto.getUsername());
        user.setPassword(dto.getPassword()); // ⚠️ encode in service layer
        user.setEmail(dto.getEmail());
        user.setPhoneNumber(dto.getPhoneNumber());

        user.setEnabled(true);
        user.setAccountNonExpired(true);
        user.setAccountNonLocked(true);
        user.setCredentialsNonExpired(true);

        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());

        return user;
    }

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
}
