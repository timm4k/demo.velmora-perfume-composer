# Velmora Perfume Composer

Desktop application for composing and analyzing perfume formulas. Built with JavaFX + Spring Boot.

## Requirements

- Java 21+
- Maven 3.9+
- PostgreSQL (Supabase)

## Configuration

Set database credentials in `application.properties`:

```properties
spring.datasource.url=jdbc:postgresql://<host>:<port>/<db>
spring.datasource.username=<user>
spring.datasource.password=<password>
```

Email (optional):
```properties
spring.mail.host=smtp.gmail.com
spring.mail.port=587
spring.mail.username=<email>
spring.mail.password=<app-password>
```

## Build & Run

```bash
mvn clean install
mvn javafx:run
```

## Architecture

- **UI**: JavaFX (FXML + CSS), runs async Tasks for DB calls
- **Facade**: AuthFacade — single entry point for UI
- **Service**: UserService, CompositionService, SynergyService, EmailService
- **Repository**: Spring Data JPA (HikariCP) + JDBC NoteDao (SimpleConnectionPool)
- **Database**: PostgreSQL (Supabase), Flyway migrations

## Connection Pool Note

The project demonstrates two pool implementations:
- **HikariCP**: used by Spring Data JPA repositories
- **SimpleConnectionPool**: custom JDBC pool used by NoteDao

## Tech Stack

- Java 21, JavaFX 21, Spring Boot 3.2
- Spring Data JPA, Spring Security (BCrypt)
- Flyway, PostgreSQL
- Jakarta Validation
- Maven, Lombok
