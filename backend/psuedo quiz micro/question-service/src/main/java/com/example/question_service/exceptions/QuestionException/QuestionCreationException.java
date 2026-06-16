package com.example.question_service.exceptions.QuestionException;

public class QuestionCreationException extends RuntimeException{

    private static final long seriesVersionUID = 12;

    public QuestionCreationException(String message){super(message);}
}
