package com.example.logmanager.observer;

import com.example.logmanager.model.LogEntry;
import com.example.logmanager.model.LogLevel;

public class ConsoleAlertObserver implements LogObserver {

    @Override
    public void onLogAdded(LogEntry entry) {
        if (entry.getLevel() == LogLevel.ERROR) {
            System.out.println("[ALERT] Нова грешка: " + entry.getMessage());
        }
    }
}