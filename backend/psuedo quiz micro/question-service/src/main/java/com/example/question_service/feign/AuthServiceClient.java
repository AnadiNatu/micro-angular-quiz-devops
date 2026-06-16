package com.example.question_service.feign;


import com.example.question_service.dto.authDTO.UserStatSyncDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "auth-service")
public interface AuthServiceClient {

    @PostMapping("/api/auth/internal/sync-stat")
    void syncStat(@RequestBody UserStatSyncDTO dto);

}
