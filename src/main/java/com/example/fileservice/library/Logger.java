package com.example.fileservice.library;

import org.springframework.stereotype.Component;

@Component
public class Logger {

    public void crit(LogItem logItem) {
        write("crit", logItem);
    }

    private void write(String logLevel, LogItem logItem) {
        System.out.println(logLevel + ": " + logItem);
    }
}
