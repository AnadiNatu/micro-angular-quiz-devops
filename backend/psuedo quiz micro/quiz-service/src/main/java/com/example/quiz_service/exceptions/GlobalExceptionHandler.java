package com.example.quiz_service.exceptions;

import com.example.quiz_service.exceptions.QuizExcetion.*;
import com.example.quiz_service.exceptions.UserException.UserNotAuthenticatedException;
import com.example.quiz_service.exceptions.UserException.UserRoleIncorrectException;
import com.example.quiz_service.exceptions.UserException.UserWithNameNotFound;
import com.example.quiz_service.exceptions.UserException.UserWithNameOrIdNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.Date;

@ControllerAdvice
public class GlobalExceptionHandler {

//    @ExceptionHandler(QuestionDataInvalidException.class)
//    public ResponseEntity<ErrorObject> handleInvalidQuestionDataException(QuestionDataInvalidException ex){
//        ErrorObject errorObject = new ErrorObject();
//
//        errorObject.setStatusCode(HttpStatus.NOT_FOUND.value());
//        errorObject.setMessage(ex.getMessage());
//        errorObject.setTimeStamp(new Date());
//
//        return new ResponseEntity<ErrorObject>(errorObject, HttpStatus.NOT_FOUND);
//    }
//
//
//    @ExceptionHandler(QuestionCreationException.class)
//    public ResponseEntity<ErrorObject> handleInvalidQuestionCreationException(QuestionCreationException ex){
//        ErrorObject errorObject = new ErrorObject();
//
//        errorObject.setStatusCode(HttpStatus.NOT_FOUND.value());
//        errorObject.setMessage(ex.getMessage());
//        errorObject.setTimeStamp(new Date());
//
//        return new ResponseEntity<ErrorObject>(errorObject, HttpStatus.NOT_FOUND);
//    }
//
//    @ExceptionHandler(QuestionNotCreatedException.class)
//    public ResponseEntity<ErrorObject> handleQuestionNotCreatedException(QuestionNotCreatedException ex){
//        ErrorObject errorObject = new ErrorObject();
//
//        errorObject.setStatusCode(HttpStatus.NOT_FOUND.value());
//        errorObject.setMessage(ex.getMessage());
//        errorObject.setTimeStamp(new Date());
//
//        return new ResponseEntity<ErrorObject>(errorObject, HttpStatus.NOT_FOUND);
//    }
//
//    @ExceptionHandler(QuestionAvailabilityException.class)
//    public ResponseEntity<ErrorObject> handleQuestionAvailabilityException(QuestionAvailabilityException ex){
//        ErrorObject errorObject = new ErrorObject();
//
//        errorObject.setStatusCode(HttpStatus.NOT_FOUND.value());
//        errorObject.setMessage(ex.getMessage());
//        errorObject.setTimeStamp(new Date());
//
//        return new ResponseEntity<ErrorObject>(errorObject, HttpStatus.NOT_FOUND);
//    }


    @ExceptionHandler(QuizNotFoundException.class)
    public ResponseEntity<ErrorObject> handleQuizNotFoundException(QuizNotFoundException ex){
        // RuntimeException

        ErrorObject errorObject = new ErrorObject();

        errorObject.setStatusCode(HttpStatus.NOT_FOUND.value());
        errorObject.setMessage(ex.getMessage());
        errorObject.setTimeStamp(new Date());

        return new ResponseEntity<ErrorObject>(errorObject, HttpStatus.NOT_FOUND);

    }



    @ExceptionHandler(QuizWithIdNotFound.class)
    public ResponseEntity<ErrorObject> handleQuizWithIdNotFound(QuizWithIdNotFound ex){

        ErrorObject errorObject = new ErrorObject();

        errorObject.setStatusCode(HttpStatus.NOT_FOUND.value());
        errorObject.setMessage(ex.getMessage());
        errorObject.setTimeStamp(new Date());

        return new ResponseEntity<ErrorObject>(errorObject, HttpStatus.NOT_FOUND);

    }

    @ExceptionHandler(QuizProcessingErrorException.class)
    public ResponseEntity<ErrorObject> handleQuizProcessingErrorException(QuizProcessingErrorException ex){

        ErrorObject errorObject = new ErrorObject();

        errorObject.setStatusCode(HttpStatus.NOT_FOUND.value());
        errorObject.setMessage(ex.getMessage());
        errorObject.setTimeStamp(new Date());

        return new ResponseEntity<ErrorObject>(errorObject, HttpStatus.NOT_FOUND);
    }


    @ExceptionHandler(QuizResponseListEmptyException.class)
    public ResponseEntity<ErrorObject> handleEmptyQuizListException(QuizResponseListEmptyException ex){
        ErrorObject errorObject = new ErrorObject();

        errorObject.setStatusCode(HttpStatus.NOT_FOUND.value());
        errorObject.setMessage(ex.getMessage());
        errorObject.setTimeStamp(new Date());

        return new ResponseEntity<ErrorObject>(errorObject, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(QuizWithNameOrIdNotFoundException.class)
    public ResponseEntity<ErrorObject> handleQuizWithNameOrIdNotFoundException(QuizWithNameOrIdNotFoundException ex){
        ErrorObject errorObject = new ErrorObject();

        errorObject.setStatusCode(HttpStatus.NOT_FOUND.value());
        errorObject.setMessage(ex.getMessage());
        errorObject.setTimeStamp(new Date());

        return new ResponseEntity<ErrorObject>(errorObject, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(UserWithNameOrIdNotFoundException.class)
    public ResponseEntity<ErrorObject> handleQuizWithNameOrIdNotFoundException(UserWithNameOrIdNotFoundException ex){
        ErrorObject errorObject = new ErrorObject();

        errorObject.setStatusCode(HttpStatus.NOT_FOUND.value());
        errorObject.setMessage(ex.getMessage());
        errorObject.setTimeStamp(new Date());

        return new ResponseEntity<ErrorObject>(errorObject, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(UserWithNameNotFound.class)
    public ResponseEntity<ErrorObject> handleUserWithNameNotFound(UserWithNameNotFound ex)
    {

        ErrorObject errorObject = new ErrorObject();

        errorObject.setStatusCode(HttpStatus.NOT_FOUND.value());
        errorObject.setMessage(ex.getMessage());
        errorObject.setTimeStamp(new Date());

        return new ResponseEntity<ErrorObject>(errorObject, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(UserRoleIncorrectException.class)
    public ResponseEntity<ErrorObject> handleUserRoleIncorrectException(UserRoleIncorrectException ex){
        ErrorObject errorObject = new ErrorObject();

        errorObject.setStatusCode(HttpStatus.FORBIDDEN.value());
        errorObject.setMessage(ex.getMessage());
        errorObject.setTimeStamp(new Date());

        return new ResponseEntity<ErrorObject>(errorObject, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(UserNotAuthenticatedException.class)
    public ResponseEntity<ErrorObject> handleUserNotAuthenticatedException(UserNotAuthenticatedException ex){
        ErrorObject errorObject = new ErrorObject();

        errorObject.setStatusCode(HttpStatus.NOT_FOUND.value());
        errorObject.setMessage(ex.getMessage());
        errorObject.setTimeStamp(new Date());

        return new ResponseEntity<ErrorObject>(errorObject, HttpStatus.NOT_FOUND);
    }

}
