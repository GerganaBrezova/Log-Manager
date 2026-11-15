package com.example.logmanager.storage;

import com.example.logmanager.factory.LogEntryFactory;
import com.example.logmanager.model.LogEntry;
import com.example.logmanager.observer.LogObserver;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public final class LogFileManager {

    private static volatile LogFileManager instance;

    private final Path file;
    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
    private final List<LogObserver> observers = new CopyOnWriteArrayList<>();

    private LogFileManager(Path file) {
        this.file = file;
        try {
            Files.createDirectories(file.getParent());
            if (Files.notExists(file)) {
                Files.createFile(file);
            }
        } catch (IOException e) {
            throw new UncheckedIOException("Неуспешно създаване на log файла", e);
        }
    }

    /** Double-checked locking. */
    public static LogFileManager getInstance() {
        if (instance == null) {
            synchronized (LogFileManager.class) {
                if (instance == null) {
                    instance = new LogFileManager(Paths.get("logs", "central.log"));
                }
            }
        }
        return instance;
    }

    public void addObserver(LogObserver observer) {
        observers.add(observer);
    }

    /** Само една нишка може да пише в даден момент. */
    public void append(LogEntry entry) {
        lock.writeLock().lock();
        try {
            Files.writeString(file, entry.toLine() + System.lineSeparator(),
                    StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        } catch (IOException e) {
            throw new UncheckedIOException("Грешка при запис в log файла", e);
        } finally {
            lock.writeLock().unlock();
        }
        observers.forEach(o -> o.onLogAdded(entry));
    }

    /** Четенето е паралелно, но не се случва по време на запис. */
    public List<LogEntry> readAll() {
        lock.readLock().lock();
        try {
            return Files.readAllLines(file, StandardCharsets.UTF_8).stream()
                    .map(LogEntryFactory::fromLine)
                    .flatMap(Optional::stream)
                    .toList();
        } catch (IOException e) {
            throw new UncheckedIOException("Грешка при четене на log файла", e);
        } finally {
            lock.readLock().unlock();
        }
    }
}