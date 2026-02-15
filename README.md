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

---

## Requirements Analysis: Task Search & Filters (Tag, Assignee, Status, Due Date)

### Goal
Enable users to quickly narrow large task lists by applying structured filters and keyword search so they can identify actionable work (e.g., "overdue tasks assigned to me with bug tag") without manually scanning columns.

### Scope & Assumptions
- Applies to **task retrieval** within a project board context (not cross-project global search in v1).
- Existing task fields are reused:
  - `tags` (comma-separated MVP format)
  - `assignee`
  - `status`
  - `dueDate`
- Filtering is server-side to guarantee consistency, enforce access control, and avoid sending unnecessarily large payloads.
- Users must already have access to the project/org to query tasks.

### Problem Statement
- Current board/task retrieval returns broad datasets and shifts filtering burden to clients.
- As project size grows, client-side filtering becomes expensive and inconsistent across views.
- Teams need canonical filtering behavior for collaboration and future sharing of views.

### Functional Requirements
- **Combined search + filters**
  - Users can provide any combination of:
    - `q` (free-text search)
    - `tag`
    - `assigneeId`
    - `status`
    - `dueDate` constraints
  - Multiple parameters are composed with logical **AND**.
- **Text search behavior (`q`)**
  - Matches task title (required) and description (recommended).
  - Case-insensitive matching.
  - Partial match supported (substring semantics for MVP).
- **Tag filter behavior**
  - `tag` matches tasks containing that tag value in normalized form (trimmed; case-insensitive).
  - If multiple tags are supported in the request (optional enhancement), define mode explicitly:
    - `ANY` (default): task has at least one requested tag.
    - `ALL` (optional): task contains every requested tag.
- **Assignee filter behavior**
  - `assigneeId=<uuid>` returns tasks assigned to that user.
  - `assignee=unassigned` (or equivalent flag) returns tasks without assignee.
  - Requests for users outside the org/project context return empty results or validation error per API convention.
- **Status filter behavior**
  - Supports one or more canonical task statuses (e.g., `TODO`, `IN_PROGRESS`, `DONE`).
  - Invalid status values return 400 validation errors with clear allowed values.
- **Due date filter behavior**
  - Support standard range constraints:
    - `dueBefore`
    - `dueAfter`
    - `dueOn` (calendar-day match in agreed timezone strategy)
    - `overdue=true` (implies due date < "now" and task not in terminal status, if applicable)
  - Define timezone rule explicitly (recommended: store/query in UTC; convert day boundaries on server).
- **Sorting & pagination**
  - Filtered endpoints should support deterministic sorting (default: `updatedAt desc`, tie-break by `id`).
  - Pagination is required for large projects; include `page`, `size`, and total metadata.
- **Access control**
  - Only users authorized for the project can query/filter tasks.
  - Querying tasks in unauthorized projects must return 403/404 according to existing concealment rules.

### Non-Functional Requirements
- **Performance**
  - Target median query latency under normal load: < 300 ms for typical filtered requests.
  - Index strategy should cover common predicates (`project_id`, `status`, `assignee_id`, `due_date`, text search fields as feasible).
- **Scalability**
  - Queries should remain efficient with thousands of tasks per project.
  - Avoid N+1 fetch patterns for assignee/project relations.
- **Consistency**
  - Identical query params should always produce deterministic order and stable pagination.
- **Security**
  - Enforce server-side authorization before executing expensive search operations when possible.
- **Observability**
  - Log filter usage and query timing at aggregate level (no sensitive content), enabling performance tuning.

### API/Behavior Expectations
- **Endpoint shape (example)**
  - `GET /api/projects/{projectId}/tasks?q=bug&tag=backend&assigneeId={id}&status=IN_PROGRESS&dueBefore=2026-12-31T23:59:59Z&page=0&size=20`
- **Query parameter contract**
  - Document optional vs required params, accepted enums, date-time formats (ISO-8601), and max page size.
- **Response contract**
  - Return a paged payload with task DTOs and pagination metadata.
  - Include normalized filter echo (optional) to aid client debugging.
- **Validation/error handling**
  - 400 on malformed UUID/date/enums.
  - 400 when incompatible parameters are sent (e.g., `dueOn` with contradictory range if disallowed).
  - Errors follow existing global error response schema.

### Data & Domain Considerations
- **Tags modeling (MVP vs future)**
  - MVP uses comma-separated `tags`; define normalization rules (trim, lowercase, deduplicate) to reduce inconsistent matches.
  - Future-ready option: migrate to join table (`task_tags`) for robust querying and indexing.
- **Due date semantics**
  - Clarify whether tasks without `dueDate` are excluded from due-date filters (recommended: yes unless explicitly requested).
  - Clarify whether completed tasks are excluded from `overdue=true`.
- **Status source of truth**
  - Validate against backend enum, not client-provided arbitrary strings.

### Edge Cases
- Empty search/filter input should behave as standard project task listing.
- Filters yielding zero tasks still return 200 with empty `content` and valid pagination metadata.
- Very large `q` values or excessive filter lists should be rejected or capped to prevent abuse.
- Mixed-case tags/status query values should be normalized when safe.

### Acceptance Criteria
- A user can combine at least 3 filters (e.g., `tag + assignee + status`) and receive correct intersected results.
- Due date filters return correct results around UTC day boundaries.
- Unauthorized users cannot enumerate or infer tasks through filter queries.
- API docs clearly describe filter parameters, examples, and error cases.
- Automated tests cover repository/query logic, service authorization, and controller validation paths.

### Out of Scope (for this feature)
- Saved filter presets or personal views.
- Full-text ranking/relevance scoring.
- Cross-project/global workspace search.
- Advanced natural language query parsing.
