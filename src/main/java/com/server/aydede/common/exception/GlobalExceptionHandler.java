package com.server.aydede.common.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import java.time.LocalDateTime;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleResourceNotFoundException(ResourceNotFoundException ex,
            WebRequest request) {
        ErrorResponse errorResponse = new ErrorResponse(ex.getMessage(), "RESOURCE_NOT_FOUND",
                request.getDescription(false), LocalDateTime.now());
        return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleException(Exception ex, WebRequest request) {
        ErrorResponse errorResponse = new ErrorResponse(ex.getMessage(), "INTERNAL_SERVER_ERROR",
                request.getDescription(false), LocalDateTime.now());
        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> LivekitTokenGenerationException(MethodArgumentNotValidException ex,
            WebRequest request) {
        ErrorResponse errorResponse = new ErrorResponse(ex.getMessage(), "INVALID_REQUEST",
                request.getDescription(false), LocalDateTime.now());
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }
}
