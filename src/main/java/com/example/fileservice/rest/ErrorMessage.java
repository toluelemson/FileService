package com.example.fileservice.rest;

import lombok.Getter;
import lombok.Setter;
import org.springframework.http.HttpStatus;

@Setter
@Getter
public class ErrorMessage {

    private String httpStatus;
    private String errors;


    public ErrorMessage(HttpStatus httpStatus, String errors) {

        this.httpStatus = httpStatus.toString();
        this.errors = errors;
    }
}
