package com.example.question_service.feign;

import com.example.question_service.dto.ollamaDTO.OllamaNativeResponse;
import com.example.question_service.dto.ollamaDTO.OllamaRequest;
import com.example.question_service.dto.ollamaDTO.OllamaResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(
        name = "ollama-client",
        url = "${ollama.base-url:http://localhost:11434}"
)
public interface OllamaClient {
    @PostMapping(value = "/api/chat", consumes = MediaType.APPLICATION_JSON_VALUE)
    OllamaNativeResponse generate(@RequestBody OllamaRequest request);
}