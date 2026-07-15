package com.example.question_service.controller;

import com.example.question_service.dto.applicationDTO.*;
import com.example.question_service.dto.quizDTO.QuestionResponseDTO;
import com.example.question_service.service.QuestionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;

import java.util.List;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

//@CrossOrigin("*")
@RestController
@RequestMapping("/api/questions")
@RequiredArgsConstructor
@Slf4j
public class QuestionController {

    private final QuestionService questionService;

    // Done
    // ADMIN / CURATOR
    @PostMapping
    @PreAuthorize("hasAnyAuthority('ADMIN','CURATOR')")
    public ResponseEntity<QuestionDto> createQuestion(@RequestBody CreateQuestionDto dto) {
        log.info("[QUESTION] Create question request: category={}", dto.getCategory());
        return ResponseEntity.ok(questionService.addQuestion(dto));
    }

    // Done
    // AUTHENTICATED
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<QuestionDto>> getAllQuestions() {
        log.debug("[QUESTION] Fetch all questions");
        return ResponseEntity.ok(questionService.getAllQuestions());
    }

    // Done
    // AUTHENTICATED
    @GetMapping("/internal/category/{category}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<QuestionResponseDTO>> getQuestionsByCategory(@PathVariable String category) {
        log.debug("[QUESTION] Fetch by category={}", category);
        return ResponseEntity.ok(questionService.getQuestionsByCategory(category));
    }

    // FEIGN (QUIZ SERVICE)
    @GetMapping("/internal/generate")
    public ResponseEntity<List<Long>> generateQuiz(
            @RequestParam String category,
            @RequestParam Integer numQuestions) {
        log.info("[QUESTION] Generate quiz: category={}, numQuestions={}", category, numQuestions);
        return ResponseEntity.ok(
                questionService.getQuestionIdsForQuiz(category, numQuestions)
        );
    }

    // FEIGN
    @PostMapping("/internal/fetch")
    public ResponseEntity<List<QuestionResponseDTO>> getQuestionsFromIds(
            @RequestBody List<Long> ids) {
        log.debug("[QUESTION] Fetch by ids, count={}", ids.size());
        return ResponseEntity.ok(questionService.getQuestionsFromIds(ids));
    }

    // AUTHENTICATED
    @PostMapping("/score")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Integer> calculateScore(
            @RequestBody List<ResponseDto> responses) {
        log.info("[QUESTION] Calculate Score");
        return ResponseEntity.ok(questionService.calculateScore(responses));
    }


    // AUTHENTICATED
    @PostMapping("/evaluate")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<ResponseEvaluationDto>> evaluate(
            @RequestBody List<ResponseDto> responses) {
        log.info("[QUESTION] Evaluate Test Score");
        return ResponseEntity.ok(questionService.evaluateResponses(responses));
    }

    // Done
//    DELETE
@DeleteMapping("/{id}")
@PostAuthorize("hasAnyAuthority('ADMIN','CURATOR')")
public ResponseEntity<QuestionDto> deleteQuestion(@PathVariable Long id) {
    log.warn("[QUESTION] Delete question request: questionId={}", id);
    return ResponseEntity.ok(questionService.deleteQuestion(id));
}

// Done
// GET CATEGORY
@GetMapping("/categories")
public ResponseEntity<List<String>> getAllCategories() {
    return ResponseEntity.ok(questionService.getAllCategories());
}


// Done
// QUESTIONS OF CREATOR
@GetMapping("/creator/{creatorAuthServiceId}")
@PreAuthorize("isAuthenticated()")
public ResponseEntity<List<QuestionDto>> getQuestionsByCreator(
        @PathVariable Long creatorAuthServiceId){
    return ResponseEntity.ok(
            questionService.getQuestionsByCreator(creatorAuthServiceId)
    );
}
}

