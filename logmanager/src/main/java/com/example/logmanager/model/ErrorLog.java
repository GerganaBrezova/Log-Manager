package com.example.logmanager.model;

import java.time.LocalDateTime;

public class ErrorLog extends LogEntry {

    public ErrorLog(LocalDateTime timestamp, String message) {
        super(timestamp, message);
    }

    @Override
    public LogLevel getLevel() {
        return LogLevel.ERROR;
    }
}