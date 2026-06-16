package com.example.question_service.dto.OpenAIDTO;

import lombok.Data;

import java.util.List;

@Data
public class OpenAIResponse {
    private List<OpenAIChoice> choices;
}