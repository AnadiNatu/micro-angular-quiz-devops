package com.example.quiz_service.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "quiz_results",
        uniqueConstraints = @UniqueConstraint(columnNames = {"quiz_id", "participant_id"}))
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QuizResult {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "quiz_id", nullable = false)
    private Long quizId;

    @Column(name = "quiz_title", nullable = false)
    private String quizTitle;

    private String category;
    private String difficultyLevel;

    @Column(name = "participant_id", nullable = false)
    private Long participantId;

    @Column(name = "participant_username", nullable = false)
    private String participantUsername;

    @Column(name = "participant_email")
    private String participantEmail;

    @Column(name = "curator_id")
    private Long curatorId;

    @Column(name = "curator_username")
    private String curatorUsername;

    @Column(name = "total_questions")
    private int totalQuestions;

    @Column(name = "correct_answers")
    private int correctAnswers;

    @Column(name = "incorrect_answers")
    private int incorrectAnswers;

    private double percentage;

    @Column(name = "taken_at")
    private LocalDateTime takenAt;

    @PrePersist
    protected void onCreate() {
        takenAt = LocalDateTime.now();
    }
}
