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

---

## Requirements Analysis: Role-Based Access Control (Admin vs Member)

### Goal
Introduce clear, enforceable access rules within organizations so that administrative actions are limited to admins while members retain standard participation rights.

### Scope & Assumptions
- Applies to **organization membership** and **project lifecycle** actions.
- Users are already authenticated via JWT; RBAC builds on authenticated identity.
- Each organization has at least one **Admin** (cannot leave the org without transferring admin rights).
- Roles are stored in organization membership records and evaluated server-side on every protected endpoint.

### Functional Requirements
- **Roles and permissions**
  - **Admin**
    - Manage members: invite/add, update role, remove members.
    - Manage projects: create, update, delete projects within the organization.
  - **Member**
    - View organization and project data they belong to.
    - Participate in project activities (e.g., Kanban board changes) unless otherwise restricted.
- **Member management**
  - Only admins can add members or change member roles.
  - Only admins can remove members; admins cannot remove the last remaining admin.
  - Members cannot manage other members (no self-promotion or role updates).
- **Project deletion**
  - Only admins can delete projects.
  - Deletion must be blocked for members even if they created the project.
- **Role enforcement**
  - Every protected endpoint checks role + org membership before performing the action.
  - If a user is not a member of the org, return a 404 (to avoid org existence leaks) or 403, per existing API conventions.
- **Auditability (basic)**
  - Log administrative actions (member changes, project deletes) with actor ID, org ID, and timestamp.

### Non-Functional Requirements
- **Security**
  - Role checks must be server-side and non-bypassable.
  - JWT claims should not be trusted alone; verify roles from persisted membership.
- **Consistency**
  - Role checks should be centralized (service or security layer) to avoid drift.
- **Error clarity**
  - 403 for authenticated users lacking permissions; 404 where required for resource concealment.

### API/Behavior Expectations
- **Member management endpoints**
  - `POST /api/orgs/{orgId}/members` (admin only)
  - `PATCH /api/orgs/{orgId}/members/{memberId}` (admin only)
  - `DELETE /api/orgs/{orgId}/members/{memberId}` (admin only)
- **Project deletion endpoint**
  - `DELETE /api/orgs/{orgId}/projects/{projectId}` (admin only)
- **Error responses**
  - Consistent error payloads aligned with existing global exception handling.

### Out of Scope (for this feature)
- Fine-grained permissions beyond Admin vs Member (e.g., project-specific roles).
- Temporary elevated permissions or approval workflows.
- UI/UX changes for role management (backend enforcement only).
