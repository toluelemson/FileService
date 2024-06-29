package com.example.fileservice.library;

import lombok.Getter;
import lombok.Setter;

import java.io.IOException;
import java.time.LocalDateTime;

@Getter
public class LogItem extends IOException {
    private final String message;
    private final LocalDateTime dateTime;

    @Setter
    private String correlationId;
    @Setter
    private String type;

    public LogItem(String message) {
        this.message = message;
        this.dateTime = LocalDateTime.now();
    }

    @Override
    public String toString() {
        return "[" + dateTime + "] [" + correlationId + "] " + message;
    }
}
