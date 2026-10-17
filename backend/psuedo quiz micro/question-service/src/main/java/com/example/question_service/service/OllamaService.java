package com.example.question_service.service;

import com.example.question_service.dto.aiDTO.AiGeneratedQuestionDTO;
import com.example.question_service.dto.aiDTO.AiQuestionGenerateRequest;
import com.example.question_service.dto.ollamaDTO.OllamaMessage;
import com.example.question_service.dto.ollamaDTO.OllamaNativeResponse;
import com.example.question_service.dto.ollamaDTO.OllamaRequest;
import com.example.question_service.dto.ollamaDTO.OllamaResponse;
import com.example.question_service.feign.OllamaClient;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class OllamaService {

    private static final int MAX_QUESTION_COUNT = 20;
    private static final int MAX_OUTPUT_TOKENS = 4096;
    private static final int MAX_LOG_PREVIEW = 1500;

    private final OllamaClient ollamaClient;
    private final ObjectMapper objectMapper;

    @Value("${ollama.model:qwen3:4b}")
    private String model;


    public List<AiGeneratedQuestionDTO> generatePreview(
            AiQuestionGenerateRequest request) {

        validateRequest(request);

        String topic = request.getTopic().trim();
        String category = request.getCategory().trim();
        String difficulty = request.getDifficulty().trim();

        List<AiGeneratedQuestionDTO> questions =
                new java.util.ArrayList<>();

        for (int i = 1; i <= request.getCount(); i++) {

            String prompt = buildSingleQuestionPrompt(
                    topic,
                    category,
                    difficulty,
                    i
            );

            String rawContent = callOllama(prompt);

            List<AiGeneratedQuestionDTO> generated =
                    parseQuestions(rawContent);

            if (generated.size() != 1) {
                throw new IllegalStateException(
                        "Ollama must return exactly one question "
                                + "per generation request. Attempt "
                                + i + " returned " + generated.size() + "."
                );
            }

            validateGeneratedQuestions(generated);

            questions.add(generated.get(0));
        }

        return questions;
    }


    private String buildSingleQuestionPrompt(
            String topic,
            String category,
            String difficulty,
            int questionNumber) {

        return """
            Generate exactly ONE multiple-choice quiz question.

            Topic: %s
            Category: %s
            Difficulty: %s

            Requirements:
            1. Return exactly one JSON object.
            2. Include all six required properties.
            3. The property names are case-sensitive and must match exactly:
               question, optionA, optionB, optionC, optionD, correctAnswer.
            4. correctAnswer is mandatory and must never be null or empty.
            5. correctAnswer must exactly equal the full text of one of the four options.
            6. Do not use aliases such as answer, correct_answer, or correctOption.
            7. Do not omit any property.
            8. Return JSON only, without Markdown or explanations.

            Question sequence number: %d

            Required JSON structure:
            {
              "question": "Question text",
              "optionA": "First option",
              "optionB": "Second option",
              "optionC": "Third option",
              "optionD": "Fourth option",
              "correctAnswer": "Exact text of the correct option"
            }
            """.formatted(
                topic,
                category,
                difficulty,
                questionNumber
        );
    }


    private void validateRequest(AiQuestionGenerateRequest request) {

        if (request == null) {
            throw new IllegalArgumentException(
                    "Request body is required."
            );
        }

        if (isBlank(request.getTopic())) {
            throw new IllegalArgumentException(
                    "Topic is required."
            );
        }

        if (isBlank(request.getCategory())) {
            throw new IllegalArgumentException(
                    "Category is required."
            );
        }

        if (isBlank(request.getDifficulty())) {
            throw new IllegalArgumentException(
                    "Difficulty is required."
            );
        }

        if (request.getCount() < 1
                || request.getCount() > MAX_QUESTION_COUNT) {
            throw new IllegalArgumentException(
                    "Count must be between 1 and "
                            + MAX_QUESTION_COUNT + "."
            );
        }
    }

    private String buildPrompt(
            String topic,
            String category,
            String difficulty,
            int count) {

        return """
                You generate high-quality multiple-choice quiz questions.

                Generate exactly %d questions.

                Topic: %s
                Category: %s
                Difficulty: %s

                Requirements:
                1. Return exactly %d distinct questions.
                2. Each question must have four plausible options.
                3. There must be exactly one correct option.
                4. correctAnswer must exactly match the full text of
                   optionA, optionB, optionC, or optionD.
                5. Do not repeat questions.
                6. Do not include explanations or Markdown.
                7. Return a JSON array only.
                8. Use exactly these property names:
                   question, optionA, optionB, optionC, optionD,
                   correctAnswer.
                9. Do not include additional properties.

                Required structure:
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
                """.formatted(
                count, topic, category, difficulty, count
        );
    }

    private String callOllama(String prompt) {

        OllamaRequest request = new OllamaRequest();

        request.setModel(model);
        request.setStream(false);
        request.setThink(false);
        request.setFormat(questionSchema());
        request.setOptions(Map.of(
                "temperature", 0.2,
                "num_predict", MAX_OUTPUT_TOKENS
        ));

        request.setMessages(List.of(
                new OllamaMessage(
                        "system",
                        "Return only a valid JSON array containing "
                                + "the requested multiple-choice questions. "
                                + "Do not include thinking text or explanations."
                ),
                new OllamaMessage("user", prompt)
        ));

        long start = System.currentTimeMillis();

        try {
            log.info("[AI] Sending request to Ollama: model={}, count of messages={}", model, request.getMessages().size());

            OllamaNativeResponse response = ollamaClient.generate(request);

            long elapsed = System.currentTimeMillis() - start;

            if (response == null || response.getMessage() == null || isBlank(response.getMessage().getContent())) {
                throw new IllegalStateException("Ollama returned an empty response.");
            }

            String content = response.getMessage().getContent();
            log.warn("[AI-DEBUG] Raw Ollama content: {}", preview(content));

            log.info("[AI] Ollama completed in {} ms; doneReason={}, contentLength={}", elapsed, response.getDoneReason(), content.length());
            if ("length".equalsIgnoreCase(response.getDoneReason())) {
                log.warn("[AI] Response reached the generation limit. Preview: {}", preview(content));
                throw new IllegalStateException("Ollama reached its output token limit. " + "Increase num_predict or request fewer questions.");
            }

            return content;

        } catch (FeignException ex) {

            log.error(
                    "[AI] Ollama HTTP error: status={}, response={}",
                    ex.status(),
                    ex.contentUTF8(),
                    ex
            );

            throw new IllegalStateException(
                    "Ollama request failed: " + ex.getMessage(),
                    ex
            );
        }
    }

    private List<AiGeneratedQuestionDTO> parseQuestions(String rawContent) {

        if (isBlank(rawContent)) {
            throw new IllegalStateException(
                    "Ollama returned empty question content."
            );
        }

        String clean = rawContent.trim();

        clean = clean.replaceAll(
                "(?is)<think\\b[^>]*>.*?</think\\s*>",
                ""
        ).trim();

        clean = clean.replaceAll(
                "(?im)^\\s*```(?:json)?\\s*$",
                ""
        ).trim();

        try {
            JsonNode root = objectMapper.readTree(clean);

            if (root == null) {
                throw new IllegalArgumentException(
                        "Ollama returned no JSON content."
                );
            }

            // Preferred response: an array containing all requested questions.
            if (root.isArray()) {
                return objectMapper.convertValue(
                        root,
                        new TypeReference<List<AiGeneratedQuestionDTO>>() {}
                );
            }

            // Recovery: Ollama sometimes returns a single question object.
            if (root.isObject() && root.has("question")) {
                AiGeneratedQuestionDTO question =
                        objectMapper.treeToValue(
                                root,
                                AiGeneratedQuestionDTO.class
                        );

                log.warn(
                        "[AI] Ollama returned a single question object instead of an array."
                );

                return List.of(question);
            }

            throw new IllegalArgumentException(
                    "Expected a JSON array or a question object."
            );

        } catch (Exception ex) {

            log.error(
                    "[AI] Failed to parse Ollama response. Reason={}, responsePreview={}",
                    ex.getMessage(),
                    preview(clean)
            );

            throw new IllegalStateException(
                    "Failed to parse Ollama-generated questions: "
                            + ex.getMessage(),
                    ex
            );
        }
    }


    private Map<String, Object> questionSchema() {

        Map<String, Object> properties = Map.of(
                "question", Map.of("type", "string"),
                "optionA", Map.of("type", "string"),
                "optionB", Map.of("type", "string"),
                "optionC", Map.of("type", "string"),
                "optionD", Map.of("type", "string"),
                "correctAnswer", Map.of("type", "string")
        );

        return Map.of(
                "type", "object",
                "properties", properties,
                "required", List.of(
                        "question",
                        "optionA",
                        "optionB",
                        "optionC",
                        "optionD",
                        "correctAnswer"
                ),
                "additionalProperties", false
        );
    }


    private String extractJsonArray(String content) {

        int start = content.indexOf('[');

        if (start < 0) {
            throw new IllegalArgumentException(
                    "Response did not contain a JSON array. "
                            + "Content starts with: " + preview(content)
            );
        }

        boolean inString = false;
        boolean escaped = false;
        int depth = 0;

        for (int i = start; i < content.length(); i++) {

            char ch = content.charAt(i);

            if (inString) {
                if (escaped) {
                    escaped = false;
                } else if (ch == '\\') {
                    escaped = true;
                } else if (ch == '"') {
                    inString = false;
                }

                continue;
            }

            if (ch == '"') {
                inString = true;
            } else if (ch == '[') {
                depth++;
            } else if (ch == ']') {
                depth--;

                if (depth == 0) {
                    return content.substring(start, i + 1);
                }
            }
        }

        throw new IllegalArgumentException(
                "The JSON array was incomplete or truncated. "
                        + "Response preview: " + preview(content)
        );
    }


    private void validateGeneratedQuestions(
            List<AiGeneratedQuestionDTO> questions) {

        for (int i = 0; i < questions.size(); i++) {

            AiGeneratedQuestionDTO q = questions.get(i);

            if (q == null) {
                throw new IllegalStateException(
                        "Generated question " + (i + 1) + " is null."
                );
            }

            List<String> missingFields = new java.util.ArrayList<>();

            if (isBlank(q.getQuestion())) {
                missingFields.add("question");
            }
            if (isBlank(q.getOptionA())) {
                missingFields.add("optionA");
            }
            if (isBlank(q.getOptionB())) {
                missingFields.add("optionB");
            }
            if (isBlank(q.getOptionC())) {
                missingFields.add("optionC");
            }
            if (isBlank(q.getOptionD())) {
                missingFields.add("optionD");
            }
            if (isBlank(q.getCorrectAnswer())) {
                missingFields.add("correctAnswer");
            }

            if (!missingFields.isEmpty()) {
                log.error(
                        "[AI] Generated question {} is missing fields: {}",
                        i + 1,
                        missingFields
                );

                throw new IllegalStateException(
                        "Generated question " + (i + 1)
                                + " has missing fields: " + missingFields
                );
            }

            List<String> options = List.of(
                    q.getOptionA(),
                    q.getOptionB(),
                    q.getOptionC(),
                    q.getOptionD()
            );

            if (options.stream().distinct().count() != 4) {
                throw new IllegalStateException(
                        "Generated question " + (i + 1)
                                + " has duplicate options."
                );
            }

            boolean answerMatchesOption = options.stream()
                    .anyMatch(option ->
                            option.equals(q.getCorrectAnswer()));

            if (!answerMatchesOption) {
                throw new IllegalStateException(
                        "Generated question " + (i + 1)
                                + " has a correctAnswer that "
                                + "does not match an option."
                );
            }
        }
    }


    private String preview(String value) {

        if (value == null) {
            return "<null>";
        }

        String normalized = value.replaceAll("\\s+", " ");

        if (normalized.length() <= MAX_LOG_PREVIEW) {
            return normalized;
        }

        return normalized.substring(0, MAX_LOG_PREVIEW)
                + "... [truncated for logging]";
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}