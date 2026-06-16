package com.example.notification_service.dto.shared;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserDTO {
    private Long id;
    private String username;
    private String email;
    private String phoneNumber;
    private String role;
    private boolean enabled;
}