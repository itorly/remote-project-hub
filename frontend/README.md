# Remote Project Hub – Frontend

React + TypeScript client for the Remote Project Hub backend. It focuses on authentication, project/org browsing, and an interactive Kanban board with drag-and-drop powered by `@dnd-kit`.

## Stack
- React 18 + TypeScript + Vite
- React Router for routing
- TanStack Query for server state
- React Hook Form for forms
- @dnd-kit for drag-and-drop
- Axios for API calls

## Running locally
1. Install dependencies
   ```bash
   npm install
   ```
2. Start the dev server
   ```bash
   npm run dev
   ```
3. Point `VITE_API_BASE_URL` to your backend (defaults to `http://localhost:8080`).

Build + preview:
```bash
npm run build
npm run preview
```

Tests (component-level smoke):
```bash
npm test
```

## Auth token storage
The auth toggle lets you choose between:
- **Memory** (default safer option): token is kept in memory only; closing or refreshing the tab clears it.
- **Local storage**: token survives refreshes and new tabs but should be avoided on shared machines.

## Key flows
- **/login, /register**: Auth with JWT stored per the chosen persistence mode.
- **/organizations**: List + create organizations.
- **/organizations/:id/projects**: List + create projects for an org.
- **/projects/:projectId/board**: Kanban board, create columns/tasks, drag tasks to move columns, view activity feed.

## Env variables
- `VITE_API_BASE_URL` – Backend base URL (e.g., `http://localhost:8080`).
