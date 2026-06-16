package com.example.question_service.exceptions.QuestionException;

public class QuestionDataInvalidException extends RuntimeException {

    private static final long seriesVersionUID = 4;

    public QuestionDataInvalidException(String message){super(message);}
}
