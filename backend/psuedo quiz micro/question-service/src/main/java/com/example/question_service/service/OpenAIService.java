package com.example.question_service.service;

import com.example.question_service.dto.OpenAIDTO.OpenAIMessage;
import com.example.question_service.dto.OpenAIDTO.OpenAIRequest;
import com.example.question_service.dto.OpenAIDTO.OpenAIResponse;
import com.example.question_service.dto.aiDTO.AiGeneratedQuestionDTO;
import com.example.question_service.dto.aiDTO.AiQuestionGenerateRequest;
import com.example.question_service.feign.OpenAIClient;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class OpenAIService {

    private final OpenAIClient openAIClient;
    private final ObjectMapper objectMapper;

//    @Value("${openai.api.key}")
//    private String apiKey;

    public List<AiGeneratedQuestionDTO> generatePreview(AiQuestionGenerateRequest req){
        String prompt = buildPrompt(req.getTopic() , req.getCategory() , req.getDifficulty() , req.getCount());

        String rawJson = callOpenAI(prompt);

        return parseQuestion(rawJson);
    }

    private String buildPrompt(String topic , String category , String difficulty , int count){

        return """
                Generate %d multiple choice quiz questions.
 
                Topic:      %s
                Category:   %s
                Difficulty: %s
 
                Rules:
                1. Each question must have exactly four options (optionA, optionB, optionC, optionD).
                2. Only ONE option is the correct answer.
                3. No duplicate questions.
                4. correctAnswer must be the EXACT text of the correct option (not "A" or "B", the full text).
                5. Return a valid JSON array ONLY — no markdown fences, no explanations.
 
                Required JSON format:
                [
                  {
                    "question":     "",
                    "optionA":      "",
                    "optionB":      "",
                    "optionC":      "",
                    "optionD":      "",
                    "correctAnswer": ""
                  }
                ]
                """.formatted(count , topic , category , difficulty);
    }

    private String callOpenAI(String prompt){
        OpenAIRequest request = new OpenAIRequest();
        request.setModel("gpt-4.1");
        request.setMessages(List.of(new OpenAIMessage("user" , prompt)));

        log.info("[AI] Calling Open API ....");

        OpenAIResponse response = openAIClient.generate("Bearer " + "${openAI API key}" , request);

        String content = response.getChoices().get(0).getMessage().getContent();

        log.info("[AI] OpenAI responded successfully , content length={} " , content.length());

        return content;
    }

   private List<AiGeneratedQuestionDTO> parseQuestion(String rawJson){
        try{
            String clean = rawJson
                    .replaceAll("(?s)```json\\s*" , "")
                    .replaceAll("(?s)```\\s*" , "")
                    .trim();
            return objectMapper.readValue(
                    clean,
                    new TypeReference<List<AiGeneratedQuestionDTO>>() {});
        }catch (Exception ex){
            log.error("[AI] Failed to parse OpenAI response : {}" , ex.getMessage());

            throw new RuntimeException("Failed to parse AI-generated questions . Raw : " + rawJson , ex);
        }
   }
}