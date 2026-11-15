package com.example.logmanager.model;

import java.time.LocalDateTime;

public abstract class LogEntry {

    private final LocalDateTime timestamp;
    private final String message;

    protected LogEntry(LocalDateTime timestamp, String message) {
        this.timestamp = timestamp;
        this.message = message == null ? "" : message.replaceAll("[\\r\\n]+", " ").trim();
    }

    public abstract LogLevel getLevel();

    public String getCssClass() {
        return getLevel().name().toLowerCase();
    }

    public String toLine() {
        return timestamp + " | " + getLevel() + " | " + message;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public String getMessage() {
        return message;
    }
}