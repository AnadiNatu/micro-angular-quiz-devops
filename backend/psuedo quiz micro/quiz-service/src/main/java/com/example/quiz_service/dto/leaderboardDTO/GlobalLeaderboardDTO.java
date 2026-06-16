package com.example.quiz_service.dto.leaderboardDTO;


import lombok.*;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GlobalLeaderboardDTO {

    private int totalEntries;
    private List<GlobalRankEntryDTO> rankings;

}