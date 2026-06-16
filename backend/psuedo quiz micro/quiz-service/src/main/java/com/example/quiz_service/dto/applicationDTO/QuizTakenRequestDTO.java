package com.example.quiz_service.dto.applicationDTO;

import lombok.*;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class QuizTakenRequestDTO {
    private Long quizId;
    private Long userId;

    private List<ResponseDTO> responses;
}
