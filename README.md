<div align="center">

# ✦ VELMORA COMPOSER

**Olfactory Lab — Desktop Application for Perfume Composition**

*A professional desktop system for modeling, analyzing, and crafting fragrance compositions*

---

![Java](https://img.shields.io/badge/Java-21+-B89B8B?style=flat-square&logo=openjdk&logoColor=white)
![JavaFX](https://img.shields.io/badge/JavaFX-21-A4A0C5?style=flat-square&logo=java&logoColor=white)
![Maven](https://img.shields.io/badge/Maven-3.9+-78A0A0?style=flat-square&logo=apachemaven&logoColor=white)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-Supabase-E2A998?style=flat-square&logo=postgresql&logoColor=white)

</div>

---

## Overview

Velmora Composer is an educational desktop application built with JavaFX that simulates the workflow of a professional perfumer. It provides an interactive vault of raw fragrance materials, a formula workbench for building compositions, session history tracking, and deep analytical tools for olfactory profiling.

---

## Requirements

| Tool | Version |
|------|---------|
| Java | 21+ |
| Maven | 3.9+ |
| PostgreSQL / Supabase | any recent |

---

## Configuration

Sensitive credentials are managed via environment variables — never hardcoded.

### `application.properties`

```properties
spring.datasource.url=${DB_URL}
spring.datasource.username=${DB_USERNAME}
spring.datasource.password=${DB_PASSWORD}

spring.mail.username=${MAIL_USERNAME}
spring.mail.password=${MAIL_PASSWORD}
```

### Environment Variables

Set these inside **IntelliJ IDEA** → Run/Debug Configurations → Environment Variables:

```env
DB_URL=
DB_USERNAME=
DB_PASSWORD=

MAIL_USERNAME=
MAIL_PASSWORD=
```

Or create a local `.env` file in the project root *(not committed to version control)*.

---

## Build & Run

```bash
# Install dependencies and build
mvn clean install

# Launch the application
mvn javafx:run
```

**Main class:** `velmora.composer.Main`

---

## Project Focus

This project was developed as an educational software system with emphasis on:

- **Perfume composition modeling** — phase-aware ingredient layering (top / heart / base)
- **Desktop application development** — native JavaFX UI with scene management
- **JavaFX interface design** — custom components, animations, and responsive layouts
- **PostgreSQL interaction** — relational schema for ingredients, compositions, and sessions
- **JDBC programming** — raw SQL access without ORM abstractions
- **Custom connection pool implementation** — manual pooling for controlled resource management
- **Analytical perfume systems** — intensity scoring, scent timelines, and olfactory profiling

---

## Project Structure

```
velmora-composer/
├── src/
│   └── main/
│       ├── java/velmora/composer/
│       │   ├── Main.java
│       │   ├── ui/
│       │   │   ├── controller/
│       │   │   │   └── VaultController.java
│       │   │   └── view/
│       │   ├── model/
│       │   ├── repository/
│       │   └── service/
│       └── resources/
│           ├── fxml/
│           ├── css/
│           └── application.properties
├── pom.xml
└── README.md
```

---