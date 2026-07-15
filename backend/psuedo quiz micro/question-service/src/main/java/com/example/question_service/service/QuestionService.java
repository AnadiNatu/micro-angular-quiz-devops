package com.example.question_service.service;

import com.example.question_service.dto.applicationDTO.*;
import com.example.question_service.dto.authDTO.UserStatSyncDTO;
import com.example.question_service.dto.quizDTO.QuestionResponseDTO;
import com.example.question_service.entity.Questions;
import com.example.question_service.feign.AuthServiceClient;
import com.example.question_service.mapper.QuestionMapper;
import com.example.question_service.repository.QuestionRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;


import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class QuestionService {

    private final QuestionRepository questionRepository;
    private final AuthServiceClient authServiceClient;
    private final QuestionMapper questionMapper;
    // ================= CREATE =================

    public QuestionDto addQuestion(CreateQuestionDto dto) {

        // Validate creator id is present and is the auth-service id, not a local FK
        if (dto.getCreatorAuthServiceId() == null) {
            throw new IllegalArgumentException(
                    "creatorAuthServiceId is required and must be the user's ID in auth-service");
        }

        Questions question = QuestionMapper.toEntity(dto);
        Questions saved = questionRepository.save(question);

//        if (saved.getCreatorAuthServiceId() != null){
            try{
                authServiceClient.syncStat(UserStatSyncDTO.builder()
                                .authServiceId(saved.getCreatorAuthServiceId())
                                .statType("QUESTION_CREATED")
                                .build());
                log.info("[QUESTION-SERVICE] Synced QUESTION_CREATED for authServiceID : {}" , saved.getCreatorAuthServiceId());

            }catch (Exception ex){
                log.warn("[QUESTION-SERVICE] Failed to sync QUESTION_CREATED stat : {}" , ex.getMessage());
            }

        log.info("[QUESTION-SERVICE] Question saved: id={}, category={}", saved.getId(), saved.getCategory());
        return QuestionMapper.toDTO(saved);
    }

    // ================= GET ALL =================

    public List<QuestionDto> getAllQuestions() {
        List<QuestionDto> list = QuestionMapper.toDTOList(questionRepository.findAll());
        log.debug("[QUESTION] Fetched all questions, count={}", list.size());
        return list;
    }

    // ================= FILTER =================

    public List<QuestionResponseDTO> getQuestionsByCategory(String category) {

        List<Questions> questions =
                questionRepository.findByCategoryIgnoreCase(category);

        List<QuestionResponseDTO> response =
                QuestionMapper.toQuestionResponse(questions);

        log.debug(
                "[QUESTION] Fetched {} questions for category={}",
                response.size(),
                category
        );

        return response;
    }

    // ================= QUIZ SUPPORT =================

    public List<Long> getQuestionIdsForQuiz(String category, Integer numQuestions) {
        List<Long> ids =  questionRepository.findRandomQuestionsByCategory(category, numQuestions);
        log.info("[QUESTION] Getting Question id for the Quiz: category={}, requested={}, found={}", category, numQuestions, ids.size());
        return ids;
    }

    public List<QuestionResponseDTO> getQuestionsFromIds(List<Long> questionIds) {

        List<Questions> questions = questionRepository.findAllById(questionIds);

        log.info("[QUESTION] Fetch questions by ids: count={}", questionIds.size());

        return QuestionMapper.toQuestionResponse(questions);
    }

    // ================= EVALUATION =================

    public Integer calculateScore(List<ResponseDto> responses) {

        int score = 0;

        for (ResponseDto response : responses) {

            Questions question = questionRepository
                    .findById(response.getQuestionId())
                    .orElseThrow(() -> new RuntimeException("Question not found"));

            if (response.getSelectedAnswer().equalsIgnoreCase(question.getRightAnswer())) {
                score++;
            }
        }

        return score;
    }

    public List<ResponseEvaluationDto> evaluateResponses(
            List<ResponseDto> responses) {

        List<ResponseEvaluationDto> evaluation = new ArrayList<>();

        for(ResponseDto response : responses){

            Questions question =
                    questionRepository.findById(response.getQuestionId())
                            .orElseThrow(() ->
                                    new RuntimeException(
                                            "Question not found : "
                                                    + response.getQuestionId()));

            evaluation.add(

                    ResponseEvaluationDto.builder()

                            .questionTitle(question.getQuestionTitle())

                            .selectedAnswer(response.getSelectedAnswer())

                            .correctAnswer(question.getRightAnswer())

                            .build()

            );

        }

        return evaluation;

    }

//    Feign Functionality
public List<QuestionResponseDTO> getQuestionsByIds(List<Long> ids) {

    List<Questions> questions = questionRepository.findAllById(ids);

    return questions.stream()
            .map(q -> QuestionResponseDTO.builder()
                    .id(q.getId())
                    .questionTitle(q.getQuestionTitle())
                    .option1(q.getOption1())
                    .option2(q.getOption2())
                    .option3(q.getOption3())
                    .option4(q.getOption4())
                    .rightAnswer(q.getRightAnswer())
                    .build())
            .toList();
}

//    public List<QuestionResponseDTO> getQuestionsByCategoryFeign(String category) {
//
//        return questionRepository.findByCategoryIgnoreCase(category)
//                .stream()
//                .map(q -> QuestionResponseDTO.builder()
//                        .id(q.getId())
//                        .questionTitle(q.getQuestionTitle())
//                        .option1(q.getOption1())
//                        .option2(q.getOption2())
//                        .option3(q.getOption3())
//                        .option4(q.getOption4())
//                        .rightAnswer(q.getRightAnswer())
//                        .build())
//                .toList();
//    }

    public QuestionDto deleteQuestion(Long id) {

        Questions question = questionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Question not found"));

        QuestionDto dto = QuestionMapper.toDTO(question);

        questionRepository.delete(question);

        return dto;
    }

    public List<String> getAllCategories() {return questionRepository.findAllDistinctCategories();}

    // QUESTION BY CREATOR
    public List<QuestionDto> getQuestionsByCreator(Long creatorAuthServiceId){

        return questionRepository
                .findByCreatorAuthServiceIdOrderByIdDesc(creatorAuthServiceId)
                .stream()
                .map(QuestionMapper::toDTO)
                .toList();

    }
}
