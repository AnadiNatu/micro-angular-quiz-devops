package com.example.notification_service.kafka;

import com.example.notification_service.config.RabbitMQConfig;
import com.example.notification_service.dto.internal.QuizSubmittedNotificationDTO;
import com.example.notification_service.dto.internal.QuizTakenNotificationDTO;
import com.example.notification_service.dto.kafka.QuizSubmittedNotificationMessage;
import com.example.notification_service.dto.kafka.QuizTakenNotificationMessage;
import com.example.notification_service.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class QuizNotificationListener {

    private final EmailService emailService;

    @RabbitListener(queues = RabbitMQConfig.QUIZ_TAKEN_QUEUE)
    public void handleQuizTaken(QuizTakenNotificationMessage message) {
        log.info("[RABBITMQ] Consumed quiz-taken — quiz={} participant={}",
                message.getQuizTitle(), message.getParticipantUsername());
        try {
            QuizTakenNotificationDTO dto = QuizTakenNotificationDTO.builder()
                    .quizId(message.getQuizId())
                    .quizTitle(message.getQuizTitle())
                    .category(message.getCategory())
                    .difficultyLevel(message.getDifficultyLevel())
                    .participantId(message.getParticipantId())
                    .participantUsername(message.getParticipantUsername())
                    .participantEmail(message.getParticipantEmail())
                    .curatorId(message.getCuratorId())
                    .curatorUsername(message.getCuratorUsername())
                    .curatorEmail(message.getCuratorEmail())
                    .build();

            emailService.sendCuratorQuizTakenEmail(dto);
            emailService.sendParticipantQuizStartedEmail(dto);
        } catch (Exception e) {
            // Swallow so a bad email send doesn't cause endless redelivery.
            log.error("[RABBITMQ] Failed processing quiz-taken message: {}", e.getMessage());
        }
    }

    @RabbitListener(queues = RabbitMQConfig.QUIZ_SUBMITTED_QUEUE)
    public void handleQuizSubmitted(QuizSubmittedNotificationMessage message) {
        log.info("[RABBITMQ] Consumed quiz-submitted — quiz={} participant={} score={}%",
                message.getQuizTitle(), message.getParticipantUsername(), message.getPercentage());
        try {
            QuizSubmittedNotificationDTO dto = QuizSubmittedNotificationDTO.builder()
                    .quizId(message.getQuizId())
                    .quizTitle(message.getQuizTitle())
                    .category(message.getCategory())
                    .difficultyLevel(message.getDifficultyLevel())
                    .participantId(message.getParticipantId())
                    .participantUsername(message.getParticipantUsername())
                    .participantEmail(message.getParticipantEmail())
                    .curatorId(message.getCuratorId())
                    .curatorUsername(message.getCuratorUsername())
                    .curatorEmail(message.getCuratorEmail())
                    .totalQuestions(message.getTotalQuestions())
                    .correctAnswers(message.getCorrectAnswers())
                    .incorrectAnswers(message.getIncorrectAnswers())
                    .percentage(message.getPercentage())
                    .build();

            emailService.sendParticipantResultEmail(dto);
            emailService.sendCuratorSubmissionEmail(dto);
        } catch (Exception e) {
            log.error("[RABBITMQ] Failed processing quiz-submitted message: {}", e.getMessage());
        }
    }
}