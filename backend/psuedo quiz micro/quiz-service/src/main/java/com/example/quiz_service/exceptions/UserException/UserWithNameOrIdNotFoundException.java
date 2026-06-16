package com.example.quiz_service.exceptions.UserException;

public class UserWithNameOrIdNotFoundException extends RuntimeException{

    private static final long seriesFinalVersionUID = 10;

    public UserWithNameOrIdNotFoundException(String message ){super(message);}
}
