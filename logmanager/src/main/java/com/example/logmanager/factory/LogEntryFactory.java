package com.example.logmanager.factory;

import com.example.logmanager.model.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.EnumMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiFunction;

public final class LogEntryFactory {

    private static final Map<LogLevel, BiFunction<LocalDateTime, String, LogEntry>> CREATORS =
            new EnumMap<>(LogLevel.class);

    static {
        CREATORS.put(LogLevel.INFO, InfoLog::new);
        CREATORS.put(LogLevel.WARNING, WarningLog::new);
        CREATORS.put(LogLevel.ERROR, ErrorLog::new);
        CREATORS.put(LogLevel.DEBUG, DebugLog::new);
    }

    private LogEntryFactory() {}

    public static LogEntry create(LogLevel level, String message) {
        return create(level, LocalDateTime.now(), message);
    }

    public static LogEntry create(LogLevel level, LocalDateTime timestamp, String message) {
        BiFunction<LocalDateTime, String, LogEntry> creator = CREATORS.get(level);
        if (creator == null) {
            throw new IllegalArgumentException("Неподдържан тип log: " + level);
        }
        return creator.apply(timestamp, message);
    }

    /** Създава запис от ред във файла; невалидните редове се пропускат. */
    public static Optional<LogEntry> fromLine(String line) {
        if (line == null) return Optional.empty();
        String[] parts = line.split(" \\| ", 3);
        if (parts.length < 3) return Optional.empty();
        try {
            LogLevel level = LogLevel.valueOf(parts[1].trim().toUpperCase());
            LocalDateTime ts = LocalDateTime.parse(parts[0].trim());
            return Optional.of(create(level, ts, parts[2]));
        } catch (IllegalArgumentException | DateTimeParseException e) {
            return Optional.empty();
        }
    }
}