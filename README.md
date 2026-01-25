# Remote Project Hub (RPH)

Remote Project Hub is a Spring Boot backend API for managing organizations, projects, and a lightweight Kanban board (columns + tasks). It is designed as a portfolio project to demonstrate real-world backend/full-stack fundamentals: authentication (JWT), clean layering, validation, error handling, and automated tests.

> Tech focus: Java 17 + Spring Boot 3 + Spring Security (JWT) + PostgreSQL + JPA/Hibernate + Gradle + JUnit/Mockito

---

## Status

![CI](https://github.com/itorly/remote-project-hub/actions/workflows/ci.yml/badge.svg)

---

## Features

### Authentication
- Register and login endpoints
- JWT-based authentication (stateless)
- Refresh tokens with rotation + revocation (logout)

### Organization & Projects
- Create and manage organization-scoped projects
- Role-aware access (e.g., owner/admin/member) depending on your implementation

### Kanban Board (per project)
- Default columns on project creation: **Todo / In Progress / Review / Done**
- Fetch columns + tasks for a project
- Create columns and tasks
- Move a task between columns (basis for activity logs later)

### Engineering Quality
- Validation with clear 4xx responses
- Global exception handling (consistent error responses)
- Unit tests and controller tests (MockMvc + Mockito)

---

## Tech Stack

**Backend**
- Java 17
- Spring Boot 3.x
- Spring Web (REST APIs)
- Spring Security
- JWT (token auth)
- Refresh tokens (rotation, revocation)
- Spring Data JPA (Hibernate)
- Bean Validation

**Database**
- PostgreSQL

**Build & Tooling**
- Gradle
- Docker Compose (local PostgreSQL)
- JUnit 5 + Mockito
- GitHub Actions CI (runs tests on push/PR)

---

## Project Structure (high level)

src/main/java/com/itorly/rph
auth/ # register/login, auth DTOs
auth/refresh/ # refresh token domain + rotation
security/ # JWT filter/provider, security config
organization/ # org + membership domain
project/ # project domain + controllers/services
common/ # global exception handling, shared helpers
src/test/java/com/itorly/rph
... # controller/service tests

---

## Prerequisites

- Java 17 installed
- Docker Desktop (recommended for PostgreSQL)
- (Optional) A local PostgreSQL installation if you don’t want Docker

---

## Configuration (No Secrets Committed)

This repo is configured to use environment variables for sensitive values (DB password, JWT secret).

1) Copy the template:

```bash
cp .env.example .env
```

2) Update JWT values for your environment:

```
JWT_SECRET=replace-with-a-long-secret-key-at-least-32-bytes
JWT_VALIDITY_MS=86400000
REFRESH_TOKEN_VALIDITY_MS=1209600000
```

---

## Auth Token Flow

**Endpoints**
- `POST /api/auth/login` → access token + refresh token
- `POST /api/auth/refresh` → new access token + rotated refresh token
- `POST /api/auth/logout` → revoke current refresh token

**Storage note (portfolio tradeoff)**
Refresh tokens are returned in JSON for simplicity. In production, you should store refresh tokens in an HttpOnly Secure cookie to reduce XSS risk.

## Add ActivityLog on task changes
1. On task create / move, insert an ActivityLog row.
2. Expose GET /api/projects/{id}/activity for the frontend.
