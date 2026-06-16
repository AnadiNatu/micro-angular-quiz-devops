package com.example.quiz_service.dto.authDTO;


import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserStatSyncDTO {

    private Long authServiceId;
    private String statType;

}
