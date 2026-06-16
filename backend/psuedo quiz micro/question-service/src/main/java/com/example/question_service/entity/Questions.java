package com.example.question_service.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "questions")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Questions {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String questionTitle;

    private String category;

    private String difficultyLevel;

    @Column(nullable = false)
    private String rightAnswer;

    private String option1;
    private String option2;
    private String option3;
    private String option4;

    @Column(name = "creator_auth_service_id")
    private Long creatorAuthServiceId;

    @Column(name = "creator_username")
    private String creatorUsername;

    @Column(name = "creator_role")
    private String creatorRole;
}