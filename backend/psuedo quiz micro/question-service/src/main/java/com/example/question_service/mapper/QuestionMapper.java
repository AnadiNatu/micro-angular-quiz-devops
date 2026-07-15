package com.example.question_service.mapper;

import com.example.question_service.dto.applicationDTO.*;
import com.example.question_service.dto.quizDTO.QuestionResponseDTO;
import com.example.question_service.entity.Questions;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class QuestionMapper {

    // ================= ENTITY → DTO =================

    public static QuestionDto toDTO(Questions question) {
        if (question == null) return null;

        return QuestionDto.builder()
                .id(question.getId())
                .questionTitle(question.getQuestionTitle())
                .category(question.getCategory())
                .difficultyLevel(question.getDifficultyLevel())
                .rightAnswer(question.getRightAnswer())
                .option1(question.getOption1())
                .option2(question.getOption2())
                .option3(question.getOption3())
                .option4(question.getOption4())
                .creatorAuthServiceId(question.getCreatorAuthServiceId())
                .creatorUsername(question.getCreatorUsername())
                .creatorRole(question.getCreatorRole())
                .build();
    }

    public static List<QuestionDto> toDTOList(List<Questions> questions) {
        return questions.stream()
                .map(QuestionMapper::toDTO)
                .collect(Collectors.toList());
    }

    // ================= DTO → ENTITY =================

    public static Questions toEntity(CreateQuestionDto dto) {
        if (dto == null) return null;

        return Questions.builder()
                .questionTitle(dto.getQuestionTitle())
                .category(dto.getCategory())
                .difficultyLevel(dto.getDifficultyLevel())
                .rightAnswer(dto.getRightAnswer())
                .option1(dto.getOption1())
                .option2(dto.getOption2())
                .option3(dto.getOption3())
                .option4(dto.getOption4())
                .creatorAuthServiceId(dto.getCreatorAuthServiceId())
                .creatorUsername(dto.getCreatorUsername())
                .creatorRole(dto.getCreatorRole())
                .build();
    }

    // ================= WRAPPER =================

    public static QuestionWrapper toWrapper(Questions question) {
        if (question == null) return null;

        return new QuestionWrapper(
                question.getId(),
                question.getQuestionTitle(),
                question.getOption1(),
                question.getOption2(),
                question.getOption3(),
                question.getOption4()
        );
    }

    public static List<QuestionWrapper> toWrapperList(List<Questions> list) {
        return list.stream().map(q -> QuestionWrapper.builder()
                .id(q.getId())
                .questionTitle(q.getQuestionTitle())
                .option1(q.getOption1())
                .option2(q.getOption2())
                .option3(q.getOption3())
                .option4(q.getOption4())
                .build()
        ).toList();
    }

    // ================= EVALUATION =================
    public static List<ResponseEvaluationDto> evaluateResponses(
            List<Questions> questions, List<ResponseDto> responses) {

        return responses.stream().map(r -> {
            Questions q = questions.stream()
                    .filter(x -> x.getId() == (r.getQuestionId()))
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("Question not found"));

            boolean correct = r.getSelectedAnswer().equalsIgnoreCase(q.getRightAnswer());

            return ResponseEvaluationDto.builder()
                    .questionTitle(q.getQuestionTitle())
                    .selectedAnswer(r.getSelectedAnswer())
                    .correctAnswer(q.getRightAnswer())
                    .build();
        }).toList();
    }

    public static QuestionResponseDTO toQuestionResponse(Questions question) {

        if (question == null) {
            return null;
        }

        return QuestionResponseDTO.builder()
                .id(question.getId())
                .questionTitle(question.getQuestionTitle())
                .option1(question.getOption1())
                .option2(question.getOption2())
                .option3(question.getOption3())
                .option4(question.getOption4())
                .rightAnswer(question.getRightAnswer())
                .category(question.getCategory())
                .difficultyLevel(question.getDifficultyLevel())
                .build();
    }

    public static List<QuestionResponseDTO> toQuestionResponse(List<Questions> questions) {

        if (questions == null || questions.isEmpty()) {
            return Collections.emptyList();
        }

        return questions.stream()
                .map(QuestionMapper::toQuestionResponse)
                .toList();
    }
}
