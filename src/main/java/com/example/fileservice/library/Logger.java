package com.example.fileservice.library;

import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class Logger {

    public void info(LogItem logItem) {
        write("info", logItem);
    }

    public void warning(LogItem logItem) {
        write("warning", logItem);
    }

    public void error(LogItem logItem) {
        write("error", logItem);
    }

    public void crit(LogItem logItem) {
        write("crit", logItem);
    }

    private void write(String logLevel, LogItem logItem) {
        System.out.println(logLevel + ": " + logItem);
    }
}
