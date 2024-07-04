package com.example.fileservice.controller.exception;

import com.example.fileservice.library.LogItem;
import com.example.fileservice.library.Logger;
import com.example.fileservice.rest.ErrorMessage;
import org.springframework.http.HttpStatus;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import jakarta.validation.ConstraintViolationException;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private final Logger logger;

    public GlobalExceptionHandler(Logger logger) {
        this.logger = logger;
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorMessage handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error ->
                errors.put(error.getField(), error.getDefaultMessage()));
        return new ErrorMessage(HttpStatus.BAD_REQUEST, errors.toString());
    }

    @ExceptionHandler(ConstraintViolationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorMessage handleConstraintViolationException(ConstraintViolationException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getConstraintViolations().forEach(error ->
                errors.put(error.getPropertyPath().toString(), error.getMessage()));
        return new ErrorMessage(HttpStatus.BAD_REQUEST, errors.toString());
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    @ResponseStatus(HttpStatus.METHOD_NOT_ALLOWED)
    public ErrorMessage handleMethodNotSupportedException(HttpRequestMethodNotSupportedException ex) {
        return new ErrorMessage(HttpStatus.METHOD_NOT_ALLOWED, ex.getMessage());
    }

    @ExceptionHandler(IOException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorMessage handleIOException(IOException ex) {
        return new ErrorMessage(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    @ExceptionHandler(NotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorMessage handleResourceNotFoundException(NotFoundException ex) {
        return new ErrorMessage(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler({IllegalArgumentException.class, InternalException.class})
    public ErrorMessage handleBadRequestExceptions(Exception ex) {
        HttpStatus status = ex instanceof IllegalArgumentException ? HttpStatus.BAD_REQUEST : HttpStatus.SERVICE_UNAVAILABLE;

        if (status == HttpStatus.SERVICE_UNAVAILABLE) {
            logger.crit(new LogItem("Critical exception occurred: " + ex.getMessage()));
        } else {
            logger.crit(new LogItem("Bad request exception occurred: " + ex.getMessage()));
        }

        return new ErrorMessage(status, ex.getMessage());
    }
}
