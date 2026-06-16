package com.example.question_service.dto.OpenAIDTO;

import lombok.*;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OpenAIRequest {

    private String model;
    private List<OpenAIMessage> messages;

}