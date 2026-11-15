package com.example.logmanager.model;

import java.time.LocalDateTime;

public class WarningLog extends LogEntry {

    public WarningLog(LocalDateTime timestamp, String message) {
        super(timestamp, message);
    }

    @Override
    public LogLevel getLevel() {
        return LogLevel.WARNING;
    }
}