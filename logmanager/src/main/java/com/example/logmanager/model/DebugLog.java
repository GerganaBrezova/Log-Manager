package com.example.logmanager.model;

import java.time.LocalDateTime;

public class DebugLog extends LogEntry {

    public DebugLog(LocalDateTime timestamp, String message) {
        super(timestamp, message);
    }

    @Override
    public LogLevel getLevel() {
        return LogLevel.DEBUG;
    }
}