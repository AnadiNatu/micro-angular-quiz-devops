package com.example.quiz_service.exceptions.UserException;

public class UserNotAuthenticatedException extends RuntimeException {

    private static final long seriesVersionUID = 6;

    public UserNotAuthenticatedException(String message){ super(message);}

}
