package com.example.quiz_service.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;


@Entity
@Table(name = "quiz")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Quiz {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;
    private String category;
    private String difficultyLevel;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "quiz_question_ids", joinColumns = @JoinColumn(name = "quiz_id"))
    @Column(name = "question_id")
    private List<Long> questionIds;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "quiz_participant_ids", joinColumns = @JoinColumn(name = "quiz_id"))
    @Column(name = "user_id")
    private List<Long> participantIds;

    @Column(name = "created_by_user_id")
    private Long createdByUserId;

    @Column(name = "creator_username")
    private String creatorUsername;
}
