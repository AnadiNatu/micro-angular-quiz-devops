package com.example.quiz_service.dto.leaderboardDTO;


import lombok.*;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CategoryLeaderboardDTO {

    private String category;
    private int totalEntries;
    private List<LeaderboardEntryDTO> rankings;

}