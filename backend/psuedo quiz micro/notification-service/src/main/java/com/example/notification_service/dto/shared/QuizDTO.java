package com.example.notification_service.dto.shared;

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
    private String creatorUsername;
    private List<Long> questionIds;
    private List<Long> participantIds;
    private List<String> participantUsernames;

}
