package com.example.notification_service.feign;


import com.example.notification_service.config.FeignConfig;
import com.example.notification_service.dto.shared.QuizDTO;
import com.example.notification_service.dto.shared.QuizResultDTO;
import com.example.notification_service.dto.shared.QuizStatsDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@FeignClient(name = "quiz-service", configuration = FeignConfig.class)
public interface QuizFeignClient {

    @GetMapping("/api/quiz/{id}")
    QuizDTO getQuizById(@PathVariable Long id);

    @GetMapping("/api/quiz/creator/{userId}")
    List<QuizDTO> getQuizzesByCreator(@PathVariable Long userId);

    @GetMapping("/api/quiz/{quizId}/results")
    List<QuizResultDTO> getResultsByQuiz(@PathVariable Long quizId);

    @GetMapping("/api/quiz/results/user/{userId}")
    List<QuizResultDTO> getResultsByUser(@PathVariable Long userId);

    @GetMapping("/api/quiz/{quizId}/stats")
    QuizStatsDTO getQuizStats(@PathVariable Long quizId);

}
