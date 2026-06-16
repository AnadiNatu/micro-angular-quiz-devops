package com.example.quiz_service.exceptions.QuizExcetion;

public class QuizWithNameOrIdNotFoundException extends RuntimeException {

    private static final long seriesVersionUID = 9;

    public QuizWithNameOrIdNotFoundException(String message){super(message);}
}
