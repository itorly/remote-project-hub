export interface AuthResponse {
  token: string;
  userId: number;
  email: string;
  displayName: string;
}

export interface AuthUser {
  id: number;
  email: string;
  displayName: string;
}

export interface Organization {
  id: number;
  name: string;
  description?: string;
  role: string;
}

export type ProjectStatus = 'PLANNING' | 'ACTIVE' | 'ON_HOLD' | 'ARCHIVED';

export interface Project {
  id: number;
  name: string;
  description?: string;
  status: ProjectStatus;
  organizationId: number;
}

export interface Task {
  id: number;
  columnId: number;
  title: string;
  description?: string;
  status: string;
  assigneeId?: number | null;
  assigneeDisplayName?: string | null;
  dueDate?: string | null;
  tags?: string | null;
}

export interface BoardColumn {
  id: number;
  name: string;
  position: number;
  tasks: Task[];
}

export interface BoardResponse {
  projectId: number;
  projectName: string;
  columns: BoardColumn[];
}

export interface CreateColumnInput {
  name: string;
}

export interface CreateTaskInput {
  columnId: number;
  title: string;
  description?: string;
  tags?: string;
  assigneeId?: number;
  dueDate?: string;
}

export interface ActivityLog {
  id: number;
  projectId: number;
  taskId: number;
  taskTitle: string;
  actionType: string;
  oldValue?: string | null;
  newValue?: string | null;
  actorId?: number | null;
  actorDisplayName?: string | null;
  createdAt: string;
}
