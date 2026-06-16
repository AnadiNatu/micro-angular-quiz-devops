package com.example.quiz_service.exceptions.QuizExcetion;

public class QuizWithIdNotFound extends RuntimeException {

    private static final long seriesVersionUID = 3;

    public QuizWithIdNotFound(String message){super(message);}
}
