package com.example.quiz_service.feign;

import com.example.quiz_service.dto.applicationDTO.QuestionResponseDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient(name = "question-service")
public interface QuestionClient {
    @GetMapping("/api/questions/generate")
    List<Long> generateQuestions(@RequestParam String category , @RequestParam Integer numQuestions);

    @PostMapping("/api/questions/fetch")
    List<QuestionResponseDTO> getQuestionsByIds(@RequestBody List<Long> ids);

    @GetMapping("/api/questions/category")
    List<QuestionResponseDTO> getQuestionsByCategory(@RequestParam String category);
}
