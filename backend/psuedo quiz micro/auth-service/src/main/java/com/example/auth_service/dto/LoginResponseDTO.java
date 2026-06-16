package com.example.auth_service.dto;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponseDTO {
    private Long id;
    private Long authServiceId;
    private String token;
    private String email;
    private String username;
    private String role;
}