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

## Add ActivityLog on task changes
1. On task create / move, insert an ActivityLog row.
2. Expose GET /api/projects/{id}/activity for the frontend.

---

## Requirements Analysis: Real-Time Board Updates (WebSocket)

### Goal
Enable real-time collaboration so that when any user changes a project’s Kanban board (columns/tasks), all other connected users see the update immediately without polling.

### Scope & Assumptions
- Applies to the **project Kanban board** (columns and tasks) only.
- Users are already authenticated via JWT for REST APIs; WebSocket connections must enforce the same access control.
- The backend is the source of truth; clients render updates from server-sent events.

### Functional Requirements
- **Live updates for board mutations**
  - When a column or task is created, updated, moved, or deleted, the server broadcasts an event to all clients subscribed to that project’s board.
- **Project-scoped subscriptions**
  - Clients can subscribe to a specific project’s board channel (e.g., by project ID) and only receive events for that project.
- **Access control**
  - Only authenticated users with access to the project may connect and subscribe to its board events.
  - Unauthorized subscriptions are rejected.
- **Event payloads**
  - Events must include enough data to update the UI without a full refetch (e.g., entity IDs, changed fields, and new ordering/column IDs).
  - Include a consistent event type (e.g., `TASK_MOVED`, `TASK_CREATED`, `COLUMN_UPDATED`) and a server timestamp.
- **Client reconciliation**
  - Clients should be able to apply events idempotently and ignore duplicates.
  - On initial connect, clients should still fetch the full board state via REST, then apply subsequent WebSocket events.

### Non-Functional Requirements
- **Latency**
  - Updates should be broadcast within a low-latency window (target < 300ms server-side).
- **Reliability**
  - If a client disconnects, it can reconnect and resync via REST.
- **Security**
  - WebSocket connections must validate JWT and enforce project-level authorization.
- **Scalability**
  - The design should support multiple concurrent clients per project.
  - If horizontal scaling is introduced, the event fan-out mechanism should remain correct (e.g., via a message broker or in-memory with single instance).
- **Observability**
  - Log connection lifecycle events (connect, subscribe, disconnect) and error cases.

### API/Protocol Expectations
- **WebSocket endpoint**
  - Provide a single endpoint for connecting (e.g., `/ws`), then subscribe to project channels (e.g., `/topic/projects/{projectId}`).
- **Event types**
  - Define a minimal event schema with `type`, `projectId`, `payload`, and `timestamp`.
- **Error handling**
  - On invalid auth or subscription, return a clear error and close the connection.

### Out of Scope (for this feature)
- Activity logs and notifications beyond board updates.
- Presence/typing indicators.
- Offline conflict resolution beyond re-fetching state.
