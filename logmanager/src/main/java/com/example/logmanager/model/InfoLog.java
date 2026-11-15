package com.example.logmanager.model;

import java.time.LocalDateTime;

public class InfoLog extends LogEntry {

    public InfoLog(LocalDateTime timestamp, String message) {
        super(timestamp, message);
    }

    @Override
    public LogLevel getLevel() {
        return LogLevel.INFO;
    }
}