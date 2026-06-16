package com.example.auth_service.dto;

import lombok.*;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SignUpResponseDTO {

    private Long id;
    private Long authServiceId;
    private String username;
    private String email;
    private String role;
    private boolean enabled;

}