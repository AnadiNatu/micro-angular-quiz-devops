package com.example.auth_service.dto;


import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SignUpRequestDTO {
    private String username;
    private String password;
    private String email;
    private String phoneNumber;
    private String role;
}