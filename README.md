# Log Manager

A web application for managing log files, built with **Java 17**, **Spring Boot** and **Thymeleaf**.
Course project for *Software Patterns* demonstrating the **Factory** and **Singleton** patterns, plus **Observer** as an additional pattern.

## Table of contents

- [Assignment](#assignment)
- [Features](#features)
- [Tech stack](#tech-stack)
- [Getting started](#getting-started)
- [Log file format](#log-file-format)
- [Project structure](#project-structure)
- [Class reference](#class-reference)
- [Design patterns](#design-patterns)
- [No concurrent writes](#no-concurrent-writes)
- [User interface](#user-interface)

## Assignment

The task is to build a program for managing log files. It must support different log types (info, warning, error, etc.), reading from a file and viewing entries by type. Files are stored centrally, and concurrent write access is not allowed.

| Requirement | Implementation |
|---|---|
| Different log types | `LogLevel` enum and a separate class per type: `InfoLog`, `WarningLog`, `ErrorLog`, `DebugLog` |
| Reading from a file | `LogFileManager.readAll()` reads the central file. An external file can also be imported through the UI |
| Viewing by type | `LogService.find(level)` and a dropdown filter on the page |
| Central storage | A single file, accessed only through `LogFileManager` |
| No concurrent writes | `ReentrantReadWriteLock`: writing is exclusive |
| Factory | `LogEntryFactory` |
| Singleton | `LogFileManager` |
| Additional pattern | Observer: `LogObserver` and `ConsoleAlertObserver` |
| Graphical interface | Thymeleaf page with a responsive layout and dark mode |

## Features

- add an entry of type INFO, WARNING, ERROR or DEBUG;
- statistics: number of entries per type;
- view entries with a filter by type (newest first);
- import entries from an external file (invalid lines are skipped);
- console alert on the server when an ERROR entry is written;
- safe writes under simultaneous requests.

> The interface text is in Bulgarian.

## Tech stack

- Java 17
- Spring Boot 3.3 (`spring-boot-starter-web`, `spring-boot-starter-thymeleaf`)
- Maven
- HTML and CSS

No database is used. All data is stored in a file.

## Getting started

Requires Java 17 and Maven (or the included `mvnw`).

```bash
git clone <repository-url>
cd logmanager
./mvnw spring-boot:run
```

The application is available at `http://localhost:8080`.

Entries are stored in `logs/central.log`. The folder and the file are created automatically on first start.

## Log file format

One entry is one line: timestamp, type and message, separated by a vertical bar.

```
2026-11-07T18:13:24 | INFO | System started
2026-11-07T18:14:02 | WARNING | Low disk space
2026-11-07T18:15:47 | ERROR | Database connection lost
```

Lines in any other format are skipped when reading and importing.

## Project structure

```
com.example.logmanager
├── Application
├── config        AppConfig
├── controller    LogController
├── service       LogService
├── factory       LogEntryFactory
├── storage       LogFileManager
├── observer      LogObserver, ConsoleAlertObserver
└── model         LogLevel, LogEntry, InfoLog, WarningLog, ErrorLog, DebugLog

src/main/resources
├── templates/index.html
├── static/css/style.css
└── application.properties
```

Data flow: browser → `LogController` → `LogService` → `LogEntryFactory` and `LogFileManager` → `logs/central.log`.

## Class reference

### Package `model`

**`LogLevel`** is an enum with the types `INFO`, `WARNING`, `ERROR` and `DEBUG`. It is used by the factory, the forms and the type filter.

**`LogEntry`** is the abstract base class for a single log entry.

| Member | Description |
|---|---|
| `timestamp`, `message` | Immutable fields. Line breaks in the message are replaced with a space, so one entry is always one line in the file |
| `getLevel()` | Abstract method. Each subclass returns its own type |
| `getCssClass()` | Returns the type in lowercase. Used for coloring in the table |
| `toLine()` | Returns the file line in the format timestamp, type, message |
| `getTimestamp()`, `getMessage()` | Field access for Thymeleaf |

**`InfoLog`, `WarningLog`, `ErrorLog`, `DebugLog`** extend `LogEntry`. Each has a constructor (timestamp, message) and a `getLevel()` method that returns its type. A new log type requires one new class and one line in the factory.

### Package `factory`

**`LogEntryFactory`** is the only place where log entries are created. The constructor is private and all methods are static.

| Member | Description |
|---|---|
| `CREATORS` | An `EnumMap` that maps each type to the constructor of its class |
| `create(level, message)` | Creates an entry with the current time |
| `create(level, timestamp, message)` | Creates an entry with a given time. Throws `IllegalArgumentException` for an unsupported type |
| `fromLine(line)` | Converts a file line into an object. Returns `Optional.empty()` for an invalid line, which is then skipped |

### Package `storage`

**`LogFileManager`** is the single point of access to the central file.

| Member | Description |
|---|---|
| `instance` | The only instance (`static volatile`) |
| `file` | Path to `logs/central.log` |
| `lock` | `ReentrantReadWriteLock`: exclusive writes, shared reads |
| `observers` | List of observers (`CopyOnWriteArrayList`) |
| Private constructor | Creates the `logs` folder and the file if they do not exist |
| `getInstance()` | Returns the single instance. Uses double-checked locking |
| `addObserver(observer)` | Registers an observer |
| `append(entry)` | Acquires the write lock, appends the line to the end of the file and releases the lock in `finally`. Then notifies the observers |
| `readAll()` | Acquires the read lock, reads all lines and converts them to objects through the factory |

### Package `observer`

**`LogObserver`** is an interface with one method, `onLogAdded(entry)`, which is called after every successful write.

**`ConsoleAlertObserver`** implements `LogObserver`. When an ERROR entry is written, it prints an alert to the server console.

### Package `service`

**`LogService`** contains the logic between the controller and the storage.

| Method | Description |
|---|---|
| `add(level, message)` | Creates an entry through the factory and writes it through `LogFileManager` |
| `find(level)` | Reads the entries and keeps only those of the selected type (all of them when `null`). Newest first |
| `stats()` | Returns the number of entries per type |
| `importFile(upload)` | Reads an uploaded file line by line, converts the lines through `fromLine` and adds the valid ones. Returns the number of imported lines |

### Package `controller`

**`LogController`** handles HTTP requests and passes data to the template. It receives `LogService` through its constructor (dependency injection).

| Method | Description |
|---|---|
| `index(level)` | `GET /`. Passes the entries, statistics, types and the selected filter to the template |
| `add(level, message, ra)` | `POST /logs`. Checks that the message is not blank and calls the service. Redirects to `/` |
| `importFile(file, ra)` | `POST /logs/import`. Checks that a file was selected and calls the service. Shows the number of imported lines |

Every POST action ends with a redirect (`redirect:/`), so the form is not resubmitted when the page is refreshed. Success and error messages are stored as flash attributes.

### Package `config` and class `Application`

**`AppConfig`** registers `LogFileManager` as a Spring bean. The instance is obtained through `getInstance()` and `ConsoleAlertObserver` is added to it.

**`Application`** is the entry point. It starts Spring Boot and the embedded Tomcat server.

## Design patterns

### Factory (`LogEntryFactory`)

The rest of the code does not know which concrete classes exist. It requests an entry by type and the factory returns the right object. Creation lives in one place, so adding a new type only changes the factory.

### Singleton (`LogFileManager`)

The constructor is private and access goes through `getInstance()`. Only one object exists, and it owns the file and the lock. This provides central storage and exclusive writes. Spring receives the same instance through `AppConfig`.

### Observer (additional pattern)

`LogFileManager` is the subject and `LogObserver` is the observer. After a write, the manager notifies all registered observers without knowing what they do. New behavior (for example an email on error) is added with a new observer, without changing `LogFileManager`. Observers are notified after the lock is released, so they cannot block writing.

## No concurrent writes

The web server handles requests on different threads. Without protection, two simultaneous writes could interleave their lines in the file. `LogFileManager` uses a `ReentrantReadWriteLock`:

| Situation | Result |
|---|---|
| Two threads write at the same time | The second waits for the first. Every line stays intact |
| One thread writes, another reads | The read waits until the write finishes |
| Two threads read at the same time | Allowed. They do not block each other |

## User interface

The `index.html` page has a responsive layout and an automatic dark theme. It contains:

- **statistics:** number of entries per type;
- **new entry:** a form with a type and a message;
- **file import:** upload a file with valid lines;
- **view by type:** a dropdown and a table with the time, type and message, newest first.
