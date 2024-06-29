package com.example.fileservice.rest;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
public class ResponseEntity<T> {

    @Setter
    private T data;

    @Setter
    private List<ErrorMessage> errors;

    private final int status;

    public ResponseEntity(int status) {
        this.status = status;
    }

    public ResponseEntity(T data, int status) {
        this(data, null, status);
    }

    public ResponseEntity(T data, List<ErrorMessage> errors, int status) {
        this.data = data;
        this.errors = errors;
        this.status = status;
    }
}
