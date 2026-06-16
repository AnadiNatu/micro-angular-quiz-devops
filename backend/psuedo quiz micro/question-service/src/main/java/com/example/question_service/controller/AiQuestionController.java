package com.example.question_service.controller;

import com.example.question_service.dto.aiDTO.AiGeneratedQuestionDTO;
import com.example.question_service.dto.aiDTO.AiQuestionGenerateRequest;
import com.example.question_service.service.OpenAIService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/questions/ai")
@RequiredArgsConstructor
@CrossOrigin("*")
@Slf4j
public class AiQuestionController {

    private final OpenAIService openAIService;

    @PostMapping("/generate")
    @PreAuthorize("hasAnyAuthority('ADMIN','CURATOR')")
    public ResponseEntity<List<AiGeneratedQuestionDTO>> generatePreview(
            @RequestBody AiQuestionGenerateRequest request) {

        log.info("[AI-QUESTION] Generate preview: topic={}, category={}, difficulty={}, count={}",
                request.getTopic(), request.getCategory(),
                request.getDifficulty(), request.getCount());

        List<AiGeneratedQuestionDTO> preview = openAIService.generatePreview(request);

        log.info("[AI-QUESTION] Preview generated, count={}", preview.size());

        return ResponseEntity.ok(preview);
    }

}
