package com.example.fileservice.controller.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.NOT_FOUND)
public class NotFoundException extends BadRequestException {

    public NotFoundException(String message) {
        super(message);
    }
}
