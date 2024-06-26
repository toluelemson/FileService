package com.example.fileservice.exception;

import lombok.Getter;

@Getter
public class ResourceNotFoundException extends RuntimeException {
    private final String errorCode;

    public ResourceNotFoundException(String message) {
        super(message);
        this.errorCode = "RESOURCE_NOT_FOUND";
    }

}
