# Job Application Tracker – Spring Boot REST API

A secure REST API to track job applications. Each user registers, logs in, and manages
**only their own** applications (company, role, status, date, notes).

**Tech:** Java 17, Spring Boot 3.3, Spring Web, Spring Data JPA / Hibernate, Spring Security (JWT),
Bean Validation, PostgreSQL (H2 for zero-setup runs), JUnit 5 + MockMvc, Maven, Docker.

## Features
- Register / login with BCrypt-hashed passwords and stateless **JWT** authentication
- CRUD for job applications with **pagination, sorting and filtering by status**
- Per-user data isolation (a user gets 404 for another user's records)
- Request validation (`@Valid`) and a global exception handler with clean JSON errors
- Integration tests with MockMvc covering auth, validation, ownership, update and delete
- Docker + docker-compose (app + PostgreSQL)

## Run locally (no database needed – uses in-memory H2)
```bash
mvn test            # run the tests
mvn spring-boot:run # start on http://localhost:8080
```

## Run with PostgreSQL (Docker)
```bash
docker compose up --build
```

## API
| Method | Endpoint | Auth | Description |
|---|---|---|---|
| POST | `/api/auth/register` | – | Create account, returns JWT |
| POST | `/api/auth/login` | – | Login, returns JWT |
| POST | `/api/applications` | Bearer | Create application |
| GET | `/api/applications?status=INTERVIEW&page=0&size=10` | Bearer | List (paged, filterable) |
| GET | `/api/applications/{id}` | Bearer | Get one |
| PUT | `/api/applications/{id}` | Bearer | Update |
| DELETE | `/api/applications/{id}` | Bearer | Delete |

Statuses: `APPLIED`, `INTERVIEW`, `OFFER`, `REJECTED`.

### Example
```bash
TOKEN=$(curl -s -X POST localhost:8080/api/auth/register \
  -H 'Content-Type: application/json' \
  -d '{"email":"me@example.com","password":"password123"}' | sed 's/.*"token":"\([^"]*\)".*/\1/')

curl -X POST localhost:8080/api/applications \
  -H "Authorization: Bearer $TOKEN" -H 'Content-Type: application/json' \
  -d '{"company":"Siemens","role":"Java Developer","status":"APPLIED","appliedOn":"2026-11-01","notes":"via LinkedIn"}'

curl -H "Authorization: Bearer $TOKEN" "localhost:8080/api/applications?status=APPLIED"
```

## Project structure
```
controller/  REST endpoints (thin)
service/     business logic, per-user scoping
repository/  Spring Data JPA repositories
model/       JPA entities and enum
dto/         request/response records
security/    JWT service, auth filter, Spring Security config
exception/   custom exceptions + @RestControllerAdvice
```

## Configuration
| Variable | Default | Purpose |
|---|---|---|
| `DB_URL` | in-memory H2 | JDBC URL (PostgreSQL in Docker) |
| `DB_USER` / `DB_PASSWORD` | `sa` / empty | Database credentials |
| `JWT_SECRET` | dev value | **Change in any real deployment** (32+ chars) |
