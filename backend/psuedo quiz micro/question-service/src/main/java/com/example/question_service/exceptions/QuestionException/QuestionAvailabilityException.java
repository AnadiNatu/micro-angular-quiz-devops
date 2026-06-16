package com.example.question_service.exceptions.QuestionException;

public class QuestionAvailabilityException extends RuntimeException {

    private static final long seriesVersionUID = 7;

    public QuestionAvailabilityException(String message){super(message);}
}
