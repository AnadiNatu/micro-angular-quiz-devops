package com.example.quiz_service.kafka;

import com.example.quiz_service.config.RabbitMQConfig;
import com.example.quiz_service.dto.leaderboardDTO.QuizStatSyncMessage;
import com.example.quiz_service.dto.notificationDTO.QuizSubmittedNotificationMessage;
import com.example.quiz_service.dto.notificationDTO.QuizTakenNotificationMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class QuizNotificationPublisher {


    private final RabbitTemplate rabbitTemplate;

    public void publishQuizTaken(QuizTakenNotificationMessage message) {
        try {
            rabbitTemplate.convertAndSend(
                    RabbitMQConfig.NOTIFICATION_EXCHANGE,
                    RabbitMQConfig.QUIZ_TAKEN_ROUTING_KEY,
                    message);
            log.info("[RABBITMQ] Published quiz-taken — quizId={} participant={}",
                    message.getQuizId(), message.getParticipantUsername());
        } catch (Exception e) {
            // Publish failure must never roll back or fail the already-committed quiz operation.
            log.warn("[RABBITMQ] Failed to publish quiz-taken message: {}", e.getMessage());
        }
    }

    public void publishQuizSubmitted(QuizSubmittedNotificationMessage message) {
        try {
            rabbitTemplate.convertAndSend(
                    RabbitMQConfig.NOTIFICATION_EXCHANGE,
                    RabbitMQConfig.QUIZ_SUBMITTED_ROUTING_KEY,
                    message);
            log.info("[RABBITMQ] Published quiz-submitted — quizId={} participant={}",
                    message.getQuizId(), message.getParticipantUsername());
        } catch (Exception e) {
            log.warn("[RABBITMQ] Failed to publish quiz-submitted message: {}", e.getMessage());
        }
    }

    public void publishStatSync(QuizStatSyncMessage message) {
        try {
            rabbitTemplate.convertAndSend(
                    RabbitMQConfig.NOTIFICATION_EXCHANGE,
                    RabbitMQConfig.QUIZ_STAT_SYNC_ROUTING_KEY,
                    message);
            log.info("[RABBITMQ] Published stat-sync — authServiceId={} statType={}",
                    message.getAuthServiceId(), message.getStatType());
        } catch (Exception e) {
            log.warn("[RABBITMQ] Failed to publish stat-sync message: {}", e.getMessage());
        }
    }
}
