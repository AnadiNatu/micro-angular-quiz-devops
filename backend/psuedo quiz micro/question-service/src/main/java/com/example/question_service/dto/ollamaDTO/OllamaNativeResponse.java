package com.example.question_service.dto.ollamaDTO;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class OllamaNativeResponse {
    private OllamaMessage message;

    @JsonProperty("done_reason")
    private String doneReason;
}