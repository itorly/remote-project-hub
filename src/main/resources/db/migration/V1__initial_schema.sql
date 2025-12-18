-- Initial database schema for Remote Project Hub
-- Aligns with JPA entities and uses UTC-friendly timestamp columns

CREATE TABLE users (
    id              BIGSERIAL PRIMARY KEY,
    created_at      TIMESTAMPTZ NOT NULL,
    updated_at      TIMESTAMPTZ NOT NULL,
    email           VARCHAR(255) NOT NULL UNIQUE,
    password_hash   VARCHAR(255) NOT NULL,
    display_name    VARCHAR(100) NOT NULL,
    timezone        VARCHAR(50)  NOT NULL DEFAULT 'UTC'
);

CREATE TABLE organizations (
    id              BIGSERIAL PRIMARY KEY,
    created_at      TIMESTAMPTZ NOT NULL,
    updated_at      TIMESTAMPTZ NOT NULL,
    name            VARCHAR(255) NOT NULL,
    description     VARCHAR(2000),
    owner_id        BIGINT NOT NULL REFERENCES users(id)
);

CREATE TABLE organization_members (
    id               BIGSERIAL PRIMARY KEY,
    created_at       TIMESTAMPTZ NOT NULL,
    updated_at       TIMESTAMPTZ NOT NULL,
    organization_id  BIGINT NOT NULL REFERENCES organizations(id),
    user_id          BIGINT NOT NULL REFERENCES users(id),
    role             VARCHAR(20) NOT NULL DEFAULT 'MEMBER',
    joined_at        TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT uk_org_member UNIQUE (organization_id, user_id)
);

CREATE TABLE projects (
    id               BIGSERIAL PRIMARY KEY,
    created_at       TIMESTAMPTZ NOT NULL,
    updated_at       TIMESTAMPTZ NOT NULL,
    name             VARCHAR(255) NOT NULL,
    description      VARCHAR(2000),
    status           VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    organization_id  BIGINT NOT NULL REFERENCES organizations(id)
);

CREATE TABLE board_columns (
    id           BIGSERIAL PRIMARY KEY,
    created_at   TIMESTAMPTZ NOT NULL,
    updated_at   TIMESTAMPTZ NOT NULL,
    project_id   BIGINT NOT NULL REFERENCES projects(id),
    name         VARCHAR(100) NOT NULL,
    position     INTEGER NOT NULL
);

CREATE TABLE tasks (
    id            BIGSERIAL PRIMARY KEY,
    created_at    TIMESTAMPTZ NOT NULL,
    updated_at    TIMESTAMPTZ NOT NULL,
    project_id    BIGINT NOT NULL REFERENCES projects(id),
    column_id     BIGINT NOT NULL REFERENCES board_columns(id),
    title         VARCHAR(255) NOT NULL,
    description   VARCHAR(4000),
    assignee_id   BIGINT REFERENCES users(id),
    status        VARCHAR(20) NOT NULL DEFAULT 'TODO',
    due_date      TIMESTAMPTZ,
    tags          VARCHAR(500)
);

CREATE TABLE activity_logs (
    id            BIGSERIAL PRIMARY KEY,
    created_at    TIMESTAMPTZ NOT NULL,
    updated_at    TIMESTAMPTZ NOT NULL,
    project_id    BIGINT NOT NULL REFERENCES projects(id),
    task_id       BIGINT REFERENCES tasks(id),
    actor_id      BIGINT REFERENCES users(id),
    action_type   VARCHAR(50) NOT NULL,
    old_value     VARCHAR(4000),
    new_value     VARCHAR(4000)
);

-- Helpful indexes for join performance
CREATE INDEX idx_organizations_owner ON organizations (owner_id);
CREATE INDEX idx_org_members_user ON organization_members (user_id);
CREATE INDEX idx_projects_org ON projects (organization_id);
CREATE INDEX idx_board_columns_project ON board_columns (project_id);
CREATE INDEX idx_tasks_project ON tasks (project_id);
CREATE INDEX idx_tasks_column ON tasks (column_id);
CREATE INDEX idx_tasks_assignee ON tasks (assignee_id);
CREATE INDEX idx_activity_logs_project ON activity_logs (project_id);
CREATE INDEX idx_activity_logs_task ON activity_logs (task_id);
CREATE INDEX idx_activity_logs_actor ON activity_logs (actor_id);
