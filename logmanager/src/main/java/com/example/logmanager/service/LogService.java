package com.example.logmanager.service;

import com.example.logmanager.factory.LogEntryFactory;
import com.example.logmanager.model.LogEntry;
import com.example.logmanager.model.LogLevel;
import com.example.logmanager.storage.LogFileManager;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class LogService {

    private final LogFileManager manager;

    public LogService(LogFileManager manager) {
        this.manager = manager;
    }

    public void add(LogLevel level, String message) {
        manager.append(LogEntryFactory.create(level, message));
    }

    /** level == null означава "всички". Най-новите са първи. */
    public List<LogEntry> find(LogLevel level) {
        List<LogEntry> result = manager.readAll().stream()
                .filter(e -> level == null || e.getLevel() == level)
                .collect(Collectors.toCollection(ArrayList::new));
        Collections.reverse(result);
        return result;
    }

    public Map<LogLevel, Long> stats() {
        Map<LogLevel, Long> stats = new EnumMap<>(LogLevel.class);
        for (LogLevel l : LogLevel.values()) {
            stats.put(l, 0L);
        }
        manager.readAll().forEach(e -> stats.merge(e.getLevel(), 1L, Long::sum));
        return stats;
    }

    /** Импортира валидните редове от качен файл в централния. */
    public int importFile(MultipartFile upload) throws IOException {
        String content = new String(upload.getBytes(), StandardCharsets.UTF_8);
        int count = 0;
        for (String line : content.split("\\R")) {
            Optional<LogEntry> entry = LogEntryFactory.fromLine(line);
            if (entry.isPresent()) {
                manager.append(entry.get());
                count++;
            }
        }
        return count;
    }
}