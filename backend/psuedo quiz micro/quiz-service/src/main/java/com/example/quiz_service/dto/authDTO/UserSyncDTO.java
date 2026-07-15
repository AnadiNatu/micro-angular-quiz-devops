package com.example.quiz_service.dto.authDTO;


import com.example.quiz_service.enums.UserRoles;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserSyncDTO {
    private Long authServiceId;

    private String username;

    private String password;

    private String email;

    private String phoneNumber;

    private String profilePicture;

    private String resetToken;

    private UserRoles roles;

    private boolean enabled;

    private boolean accountNonExpired;

    private boolean accountNonLocked;

    private boolean credentialsNonExpired;

}