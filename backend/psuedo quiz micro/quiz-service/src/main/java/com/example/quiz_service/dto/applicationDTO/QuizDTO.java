package com.example.quiz_service.dto.applicationDTO;

import lombok.*;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class QuizDTO {
    private Long id;

    private String title;
    private String category;
    private String difficultyLevel;

    private Long createdByUserId;

    private List<Long> questionIds;
    private List<Long> participantIds;

    private String creatorUsername;
    private List<String> participantUsernames;
}
