# Airline Booking JavaFX App

This is a separate Maven JavaFX starter application for the graded airline booking interface. The existing Python/SQLite data-warehouse project remains unchanged in the parent directory.

## Requirements

- JDK 17 or later
- Apache Maven 3.9 or later
- Internet access on the first build so Maven can download JavaFX dependencies

Verify the tools from PowerShell:

```powershell
java -version
mvn -version
```

## Run

From this directory:

```powershell
mvn clean javafx:run
```

## Build

```powershell
mvn clean package
```

The JavaFX application entry point is `com.ccs3307.airline.App`. Add domain classes, persistence, and the graded booking workflows under `src/main/java` as the implementation grows.
