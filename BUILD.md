# FluxExport Build Guide

## Project Structure

```
flux-export/
├── flux-export-core/      # SDK Core Module
├── flux-export-sample/    # Basic Sample Application
└── flux-export-mysql-demo/ # MySQL & MyBatis-Plus Demo
```

## Prerequisites

- **JDK 17+**
- **Maven 3.6+**

## Building the Project

### Option 1: Using provided mvnw.bat (Recommended)

```bash
# Compile
.\mvnw.bat clean compile

# Package
.\mvnw.bat clean package

# Package skipping tests
.\mvnw.bat clean package -DskipTests
```

### Option 2: Using System Maven

Ensure `JAVA_HOME` points to JDK 17.

## Running the Sample Application

```bash
.\mvnw.bat clean package -DskipTests
cd flux-export-sample
java -jar target\flux-export-sample-1.0.0-SNAPSHOT.jar
```

Access: http://localhost:8080

## API Endpoints

### 1. Create Export Task
`POST /public/export/create`

### 2. Query Status
`GET /public/export/status?taskId={taskId}`

### 3. List Tasks
`GET /public/export/list?limit=10`
