package com.example.quiz_service.dto.leaderboardDTO;

import lombok.*;

import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuizStatSyncMessage implements Serializable {
    private Long authServiceId;
    private String statType;
}