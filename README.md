# remote-project-hub
Goal: A Trello/Asana-like mini app for remote teams to manage tasks, notes, and time zones.

## Add ActivityLog on task changes
1. On task create / move, insert an ActivityLog row.
2. Expose GET /api/projects/{id}/activity for the frontend.