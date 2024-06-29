package com.example.fileservice.controller.exception;

import lombok.Getter;

@Getter
public class InternalException extends RuntimeException {

    private final String errorCode;
    private final String message;


    public InternalException(String message, Throwable cause) {
        super(message, cause);
        this.message = message;
        this.errorCode = "SERVICE_UNAVAILABLE";
    }
}
