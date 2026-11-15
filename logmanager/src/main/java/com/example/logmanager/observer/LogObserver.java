package com.example.logmanager.observer;

import com.example.logmanager.model.LogEntry;

public interface LogObserver {
    void onLogAdded(LogEntry entry);
}