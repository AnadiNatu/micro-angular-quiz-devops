package com.example.question_service.feign;

import com.example.question_service.dto.OpenAIDTO.OpenAIRequest;
import com.example.question_service.dto.OpenAIDTO.OpenAIResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(name="openai-client" , url = "https://api.openai.com")
public interface OpenAIClient {

    @PostMapping(value = "/v1/chat/completions" , consumes = MediaType.APPLICATION_JSON_VALUE)
    OpenAIResponse generate(@RequestHeader("Authorization") String authorization , @RequestBody OpenAIRequest request);

}