package com.example.fileservice.library;

import org.springframework.stereotype.Component;

@Component
public class Logger {

    public void crit(LogItem logItem) {
        write(logItem);
    }

    private void write(LogItem logItem) {
        System.out.println("crit" + ": " + logItem);
    }
}
