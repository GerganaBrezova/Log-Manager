package com.example.logmanager.config;

import com.example.logmanager.observer.ConsoleAlertObserver;
import com.example.logmanager.storage.LogFileManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AppConfig {

    @Bean
    public LogFileManager logFileManager() {
        LogFileManager manager = LogFileManager.getInstance();
        manager.addObserver(new ConsoleAlertObserver());
        return manager;
    }
}