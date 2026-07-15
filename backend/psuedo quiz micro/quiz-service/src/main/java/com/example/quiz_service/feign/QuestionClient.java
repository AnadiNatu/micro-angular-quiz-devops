package com.example.quiz_service.feign;

import com.example.quiz_service.dto.applicationDTO.QuestionResponseDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@FeignClient(name = "question-service")
public interface QuestionClient {
    @GetMapping("/api/questions/internal/generate")
    List<Long> generateQuestions(@RequestParam("category") String category , @RequestParam("numQuestions") Integer numQuestions);

    @PostMapping("/api/questions/internal/fetch")
    List<QuestionResponseDTO> getQuestionsByIds(@RequestBody List<Long> ids);

//    @GetMapping("/api/questions/category")
//    List<QuestionResponseDTO> getQuestionsByCategory(@RequestParam String category);

    @GetMapping("/api/questions/internal/category/{category}")
    List<QuestionResponseDTO> getQuestionsByCategory(@PathVariable String category);
}
