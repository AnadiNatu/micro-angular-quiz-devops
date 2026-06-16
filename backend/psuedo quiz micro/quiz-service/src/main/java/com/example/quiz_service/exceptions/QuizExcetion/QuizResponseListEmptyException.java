package com.example.quiz_service.exceptions.QuizExcetion;

public class QuizResponseListEmptyException extends RuntimeException {

    private static final long seriesVersionUID = 8;

    public QuizResponseListEmptyException(String message){super(message);}

}
