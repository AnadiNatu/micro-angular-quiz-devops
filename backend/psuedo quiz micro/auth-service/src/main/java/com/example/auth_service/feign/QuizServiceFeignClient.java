package com.example.auth_service.feign;

import com.example.auth_service.dto.UserSyncDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "quiz-service")
public interface QuizServiceFeignClient {
    @PostMapping("/internal/users/sync")
    void syncUser(@RequestBody UserSyncDTO dto);
}
