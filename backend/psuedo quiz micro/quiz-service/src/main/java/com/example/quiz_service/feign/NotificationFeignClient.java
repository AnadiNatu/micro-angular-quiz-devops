package com.example.quiz_service.feign;

import com.example.quiz_service.dto.notificationDTO.QuizSubmittedNotificationDTO;
import com.example.quiz_service.dto.notificationDTO.QuizTakenNotificationDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "notification-service")
public interface NotificationFeignClient {

    @PostMapping("/api/notify/internal/quiz-taken")
    void notifyQuizTaken(@RequestBody QuizTakenNotificationDTO dto);

    @PostMapping("/api/notify/internal/quiz-submitted")
    void notifyQuizSubmitted(@RequestBody QuizSubmittedNotificationDTO dto);

}
