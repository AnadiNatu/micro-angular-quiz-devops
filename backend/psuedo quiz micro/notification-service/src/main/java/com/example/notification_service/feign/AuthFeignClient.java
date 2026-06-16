package com.example.notification_service.feign;

import com.example.notification_service.config.FeignConfig;
import com.example.notification_service.dto.shared.UserDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "auth-service" , configuration = FeignConfig.class)
public interface AuthFeignClient {

    @GetMapping("/api/auth/internal/{id}")
    UserDTO getUserById(@PathVariable Long id);

}
