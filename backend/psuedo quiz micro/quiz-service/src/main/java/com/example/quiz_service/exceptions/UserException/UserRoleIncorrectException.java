package com.example.quiz_service.exceptions.UserException;

public class UserRoleIncorrectException extends RuntimeException{
    private static final long seriesVersionUID = 5;

    public UserRoleIncorrectException(String message){super(message);}
}


