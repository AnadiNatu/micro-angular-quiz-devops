package com.example.question_service.dto.ollamaDTO;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OllamaRequest {
    private String model;

    @JsonProperty("messages")
    private List<OllamaMessage> messages;

    private boolean stream = false;
    private boolean think = false;
    private Object format = "json";
    private Map<String, Object> options;
}