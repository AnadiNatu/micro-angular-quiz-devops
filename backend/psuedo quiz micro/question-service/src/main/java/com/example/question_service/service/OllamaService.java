package com.example.question_service.service;

import com.example.question_service.dto.aiDTO.AiGeneratedQuestionDTO;
import com.example.question_service.dto.aiDTO.AiQuestionGenerateRequest;
import com.example.question_service.dto.ollamaDTO.OllamaMessage;
import com.example.question_service.dto.ollamaDTO.OllamaRequest;
import com.example.question_service.dto.ollamaDTO.OllamaResponse;
import com.example.question_service.feign.OllamaClient;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class OllamaService {
    private static final int MAX_QUESTION_COUNT = 20;

    private final OllamaClient ollamaClient;
    private final ObjectMapper objectMapper;

    @Value("${ollama.model:qwen3:4b}")
    private String model;

    public List<AiGeneratedQuestionDTO> generatePreview(AiQuestionGenerateRequest request){
        validateRequest(request);

        String prompt = buildPrompt(request.getTopic().trim() , request.getCategory().trim() , request.getDifficulty().trim(), request.getCount());

        String rawContent = callOllama(prompt);
        List<AiGeneratedQuestionDTO> questions = parseQuestions(rawContent);

        if (questions.size() != request.getCount()){
            throw new IllegalStateException("Ollama returned " + questions.size() + " questions, but " + request.getCount() + " were requested. Please retry.");
        }

        validateGeneratedQuestions(questions);
        return questions;
    }

    private void validateRequest(AiQuestionGenerateRequest request){
        if (request == null){
            throw new IllegalArgumentException("Request body is required.");
        }


        if (isBlank(request.getCategory())){
            throw new IllegalArgumentException("Topic is required");
        }

        if (isBlank(request.getCategory())){
            throw new IllegalArgumentException("Category is required.");
        }

        if (isBlank(request.getDifficulty())){
            throw new IllegalArgumentException("Difficulty is required.");
        }

        if (request.getCount() < 1 || request.getCount() > MAX_QUESTION_COUNT){
            throw new IllegalArgumentException("Count must be between 1 and " +MAX_QUESTION_COUNT+ ".");
        }
    }

    private String buildPrompt(String topic, String category , String difficulty , int count){
        return """
                You generate high-quality multiple-choice quiz questions.
                Generate exactly %d questions.

                Topic: %s
                Category: %s
                Difficulty: %s

                Requirements:
                1. Return exactly %d distinct questions relevant to the topic and difficulty.
                2. Each question must have exactly four plausible options.
                3. There must be exactly one correct option per question.
                4. correctAnswer must exactly match the full text of optionA, optionB, optionC, or optionD.
                5. Do not repeat questions.
                6. Do not include explanations, markdown, code fences, or text before or after the JSON.
                7. Return a valid JSON array only, using the exact property names shown below.
                8. Do not include additional properties.

                Required JSON structure:
                [
                  {
                    "question": "Question text",
                    "optionA": "First option",
                    "optionB": "Second option",
                    "optionC": "Third option",
                    "optionD": "Fourth option",
                    "correctAnswer": "Exact text of the correct option"
                  }
                ]
                """.formatted(count, topic, category, difficulty, count);
    }

    private String callOllama(String prompt) {
        OllamaRequest request = new OllamaRequest();

        request.setModel(model);
        request.setStream(false);
        request.setMessages(List.of(
                new OllamaMessage(
                        "system",
                        "Generate valid JSON only. Do not include markdown or explanations."
                ),
                new OllamaMessage("user", prompt)
        ));

        // Verify the request before sending it.
        log.info(
                "[AI] Ollama request prepared: model={}, messageCount={}, roles={}",
                request.getModel(),
                request.getMessages() == null ? 0 : request.getMessages().size(),
                request.getMessages() == null
                        ? List.of()
                        : request.getMessages().stream()
                          .map(OllamaMessage::getRole)
                          .toList()
        );

        try {
            // Temporary diagnostic: inspect the actual JSON serialization.
            log.debug("[AI] Serialized Ollama request: {}",
                    objectMapper.writeValueAsString(request));

            OllamaResponse response = ollamaClient.generate(request);

            if (response == null
                    || response.getChoices() == null
                    || response.getChoices().isEmpty()
                    || response.getChoices().get(0).getMessage() == null
                    || response.getChoices().get(0).getMessage().getContent() == null) {
                throw new IllegalStateException(
                        "Ollama returned an empty or invalid response."
                );
            }

            String content = response.getChoices()
                    .get(0)
                    .getMessage()
                    .getContent();

            log.info("[AI] Ollama responded successfully; content length={}",
                    content.length());

            return content;

        } catch (FeignException ex) {
            log.error(
                    "[AI] Ollama HTTP request failed: status={}, response={}",
                    ex.status(),
                    ex.contentUTF8()
            );

            throw new IllegalStateException(
                    "Ollama rejected or failed the request. Check the request payload and Ollama logs.",
                    ex
            );

        } catch (com.fasterxml.jackson.core.JsonProcessingException ex) {
            throw new IllegalStateException(
                    "Could not serialize the Ollama request.",
                    ex
            );
        }
    }

        private List<AiGeneratedQuestionDTO> parseQuestions(String rawContent){
        try{
            String clean = rawContent.replaceAll("(?s)<think>.*?</think>" , "")
                    .replaceAll("(?s)<think>```json\\s*</think>" , "")
                    .replaceAll("(?s)```\\s*" , "")
                    .trim();

            int firstArray = clean.indexOf('[');
            int lastArray = clean.lastIndexOf(']');
            if (firstArray < 0 || lastArray < firstArray){
                throw new IllegalArgumentException("Response did not contain a JSON array");
            }
            clean = clean.substring(firstArray , lastArray + 1);

            return objectMapper.readValue(clean, new TypeReference<List<AiGeneratedQuestionDTO>>(){} );


        } catch (Exception ex) {
            log.error("[AI] Failed to parse Ollama response: {}", ex.getMessage());
            throw new IllegalStateException("Failed to parse Ollama-generated questions. Please retry. Details: " + ex.getMessage(), ex);
        }
    }

    private void validateGeneratedQuestions(List<AiGeneratedQuestionDTO> questions) {
        for (int i = 0; i < questions.size(); i++) {
            AiGeneratedQuestionDTO q = questions.get(i);
            if (q == null ||
                    isBlank(q.getQuestion()) ||
                    isBlank(q.getOptionA()) ||
                    isBlank(q.getOptionB()) ||
                    isBlank(q.getOptionC()) ||
                    isBlank(q.getOptionD()) ||
                    isBlank(q.getCorrectAnswer())) {
                throw new IllegalStateException("Generated question " + (i + 1) + " has missing fields.");
            }

            List<String> options = List.of(q.getOptionA(), q.getOptionB(), q.getOptionC(), q.getOptionD());
            if (options.stream().distinct().count() != 4) {
                throw new IllegalStateException("Generated question " + (i + 1) + " has duplicate options.");
            }

            boolean answerMatchesOption = options.stream()
                    .anyMatch(option -> option.equals(q.getCorrectAnswer()));
            if (!answerMatchesOption) {
                throw new IllegalStateException(
                        "Generated question " + (i + 1) + " has a correctAnswer that does not match an option.");
            }
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}