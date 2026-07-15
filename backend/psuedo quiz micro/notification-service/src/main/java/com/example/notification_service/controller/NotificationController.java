package com.example.notification_service.controller;

import com.example.notification_service.dto.internal.QuizSubmittedNotificationDTO;
import com.example.notification_service.dto.internal.QuizTakenNotificationDTO;
import com.example.notification_service.dto.shared.QuizResultDTO;
import com.example.notification_service.dto.shared.QuizStatsDTO;
import com.example.notification_service.dto.shared.UserDTO;
import com.example.notification_service.feign.AuthFeignClient;
import com.example.notification_service.feign.QuizFeignClient;
import com.example.notification_service.service.DocumentService;
import com.example.notification_service.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

//@CrossOrigin("*")
@RestController
@RequestMapping("/api/notify")
@RequiredArgsConstructor
@Slf4j
public class NotificationController {

    private final EmailService emailService;
    private final DocumentService documentService;
    private final QuizFeignClient quizFeignClient;
    private final AuthFeignClient authFeignClient;

    //  Sends email to CURATOR + confirmation email to PARTICIPANT
    @PostMapping("/internal/quiz-taken")
    public ResponseEntity<Void> handleQuizTaken(@RequestBody QuizTakenNotificationDTO dto){
        log.info("📩 Internal: quiz-taken → quiz={}, participant={}", dto.getQuizTitle(), dto.getParticipantUsername());
        emailService.sendCuratorQuizTakenEmail(dto);
        emailService.sendParticipantQuizStartedEmail(dto);
        return ResponseEntity.ok().build();
    }


    // Sends result email to PARTICIPANT + notification to CURATOR.
    @PostMapping("/internal/quiz-submitted")
    public ResponseEntity<Void> handleQuizSubmitted(@RequestBody QuizSubmittedNotificationDTO dto){
        log.info("📩 Internal: quiz-submitted → quiz={}, participant={}, score={}%",
                dto.getQuizTitle(), dto.getParticipantUsername(), dto.getPercentage());
        emailService.sendParticipantResultEmail(dto);
        emailService.sendCuratorSubmissionEmail(dto);
        return ResponseEntity.ok().build();
    }

//    Email Endpoint

    // Done
//    A participant can only request their own report card.
    @PostMapping("/email/report/{userId}")
    @PreAuthorize("hasAuthority('PARTICIPANT') or hasAuthority('ADMIN')")
    public ResponseEntity<String> sendReportCardEmail(@PathVariable Long userId){
        UserDTO user = authFeignClient.getUserById(userId);
        List<QuizResultDTO> results = quizFeignClient.getResultsByUser(userId);

        emailService.sendParticipantReportCardEmail(user.getEmail(), user.getUsername(), results);
        return ResponseEntity.ok(" Report card sent to " + user.getEmail());
    }

    // Done
    @PostMapping("/email/result/{userId}/quiz/{quizId}")
    @PreAuthorize("hasAuthority('PARTICIPANT') or hasAuthority('ADMIN')")
    public ResponseEntity<String> sendSingleResultEmail(@PathVariable Long userId ,@PathVariable Long quizId){
        List<QuizResultDTO> results = quizFeignClient.getResultsByUser(userId);

        QuizResultDTO result = results.stream()
                .filter(r -> r.getQuizId().equals(quizId))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Result not found for this quiz"));

        UserDTO user = authFeignClient.getUserById(userId);

        // Reuse the QuizSubmittedNotificationDTO format for email
        QuizSubmittedNotificationDTO dto = QuizSubmittedNotificationDTO.builder()
                .quizId(result.getQuizId())
                .quizTitle(result.getQuizTitle())
                .category(result.getCategory())
                .difficultyLevel(result.getDifficultyLevel())
                .participantId(result.getParticipantId())
                .participantUsername(result.getParticipantUsername())
                .participantEmail(user.getEmail())
                .curatorUsername(result.getCuratorUsername())
                .totalQuestions(result.getTotalQuestions())
                .correctAnswers(result.getCorrectAnswers())
                .incorrectAnswers(result.getIncorrectAnswers())
                .percentage(result.getPercentage())
                .build();

        emailService.sendParticipantResultEmail(dto);
        return ResponseEntity.ok("📧 Result email re-sent to " + user.getEmail());
    }

//    Document endpoints

    // Done
//    ADMIN / CURATOR: Printable result table for ONE quiz
    @GetMapping(value = "/document/quiz/{quizId}/results" , produces = MediaType.TEXT_HTML_VALUE)
    @PreAuthorize("hasAnyAuthority('ADMIN' , 'CURATOR')")
    public ResponseEntity<String> quizResultDocument(@PathVariable Long quizId){
        QuizStatsDTO stats = quizFeignClient.getQuizStats(quizId);
        return ResponseEntity.ok(documentService.generateQuizResultTable(stats));
    }

    // Done
//     PARTICIPANT: Printable personal report card.
    @GetMapping(value = "/document/participant/{userId}/report" , produces = MediaType.TEXT_HTML_VALUE)
    @PreAuthorize("hasAnyAuthority('PARTICIPANT' , 'ADMIN')")
    public ResponseEntity<String> participantReportDocument(@PathVariable Long userId){
        UserDTO user = authFeignClient.getUserById(userId);
        List<QuizResultDTO> results = quizFeignClient.getResultsByUser(userId);

        return ResponseEntity.ok(documentService.generateParticipantReportCard(user.getUsername(), user.getEmail(), results));
    }

    // Done
//    PARTICIPANT: Printable result certificate for ONE quiz.
    @GetMapping(value = "/document/participant/{userId}/quiz/{quizId}/result",
            produces = MediaType.TEXT_HTML_VALUE)
    @PreAuthorize("hasAnyAuthority('PARTICIPANT','ADMIN')")
    public ResponseEntity<String> singleResultDocument(@PathVariable Long userId, @PathVariable Long quizId) {
        List<QuizResultDTO> results = quizFeignClient.getResultsByUser(userId);

        QuizResultDTO result = results.stream()
                .filter(r -> r.getQuizId().equals(quizId))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Result not found"));

        return ResponseEntity.ok(documentService.generateSingleResultDocument(result));
    }
}
