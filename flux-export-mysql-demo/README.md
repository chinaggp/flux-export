# FluxExport MySQL Demo

This module demonstrates how to integrate FluxExport with a MySQL database using MyBatis-Plus.

## Project Structure

```
src/main/java/com/flux/export/mysql/
├── MysqlDemoApplication.java     # Boot Application
├── config/                      # MyBatis-Plus & Business Config
├── converter/                   # Entity/DTO Converters
├── demo/                        # Business Service (OrderService)
├── mapper/                      # MyBatis-Plus Mappers
├── pojo/                        # Entity, Params, VO
├── service/                     # Task Persistence Service
└── spi/                         # SPI Implementations
```

## Setup

1. Create a MySQL database named `flux-export`.
2. Run the SQL scripts in `src/main/resources/db/` (schema.sql and data.sql).
3. Update `src/main/resources/application.yml` with your database credentials.

## Running the Demo

```bash
.\mvnw.bat clean package -DskipTests -pl flux-export-mysql-demo -am
java -jar flux-export-mysql-demo/target/flux-export-mysql-demo-1.0.0-SNAPSHOT.jar
```

Access: http://localhost:8080
