package ru.t1.java.demo.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalErrorHandler {

    @ExceptionHandler
    public ResponseEntity<Object> handlerEntityNotFoundException(EntityNotFoundException e) {
        return buildResponseEntity(new MyErrorResponse(HttpStatus.NOT_FOUND, e.getMessage()));
    }

    private ResponseEntity<Object> buildResponseEntity(MyErrorResponse errorResponse) {
        return new ResponseEntity<>(errorResponse, errorResponse.getStatus());
    }
}
