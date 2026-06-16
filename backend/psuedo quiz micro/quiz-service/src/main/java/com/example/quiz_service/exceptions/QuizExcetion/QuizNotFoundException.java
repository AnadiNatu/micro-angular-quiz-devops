package com.example.quiz_service.exceptions.QuizExcetion;

public class QuizNotFoundException extends RuntimeException {

    private static final long seriesVersionUID = 1;

    public QuizNotFoundException(String message){ super(message);}
}
