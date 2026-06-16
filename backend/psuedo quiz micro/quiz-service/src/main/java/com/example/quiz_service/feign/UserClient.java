package com.example.quiz_service.feign;

import com.example.quiz_service.dto.applicationDTO.UserResponseDTO;
import com.example.quiz_service.dto.authDTO.UserStatSyncDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "auth-service")
public interface UserClient {
    @GetMapping("/api/auth/internal/{id}") // The feign interface of endpoint for the user sync has been changed
    UserResponseDTO getUserById(@PathVariable Long id);

    // Increment a stat counter for a user in auth-service
    @PostMapping("/api/auth/internal/sync-stat")
    void syncStat(@RequestBody UserStatSyncDTO dto);
}
