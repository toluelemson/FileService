package com.example.fileservice.rest;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class ErrorMessage {

    private String message;
    private String code;


    public ErrorMessage(String message) {
        this.message = message;
    }


}
