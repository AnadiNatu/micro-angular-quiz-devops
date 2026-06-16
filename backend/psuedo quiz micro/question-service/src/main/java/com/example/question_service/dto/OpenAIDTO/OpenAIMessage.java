package com.example.question_service.dto.OpenAIDTO;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OpenAIMessage {

    private String role;
    private String content;

}