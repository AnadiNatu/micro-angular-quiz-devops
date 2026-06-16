package com.example.quiz_service.mapper;

import com.example.quiz_service.dto.applicationDTO.CreateQuizDto;
import com.example.quiz_service.dto.applicationDTO.QuizDTO;
import com.example.quiz_service.dto.applicationDTO.UserResponseDTO;
import com.example.quiz_service.entity.Quiz;

import java.util.List;
import java.util.stream.Collectors;

public class QuizMapper {

    // ================= ENTITY → DTO =================

    public static QuizDTO toDTO(
            Quiz quiz,
            UserResponseDTO creator,
            List<UserResponseDTO> participants
    ) {

        return QuizDTO.builder()
                .id(quiz.getId())
                .title(quiz.getTitle())
                .category(quiz.getCategory())
                .difficultyLevel(quiz.getDifficultyLevel())
                .createdByUserId(quiz.getCreatedByUserId())
                .questionIds(quiz.getQuestionIds())
                .participantIds(quiz.getParticipantIds())

                // 🔥 Enriched fields
                .creatorUsername(creator != null ? creator.getUsername() : null)
                .participantUsernames(
                        participants != null
                                ? participants.stream()
                                .map(UserResponseDTO::getUsername)
                                .collect(Collectors.toList())
                                : null
                )
                .build();
    }

    // ================= CREATE =================

    public static Quiz toEntity(CreateQuizDto dto, List<Long> questionIds) {

        return Quiz.builder()
                .title(dto.getTitle())
                .category(dto.getCategory())
                .difficultyLevel(dto.getDifficultyLevel())
                .createdByUserId(dto.getCreatedByUserId())
                .questionIds(questionIds)
                .build();
    }
}
