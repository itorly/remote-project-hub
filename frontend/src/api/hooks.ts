import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { apiClient } from './client';
import {
  ActivityLog,
  AuthResponse,
  BoardColumn,
  BoardResponse,
  CreateColumnInput,
  CreateTaskInput,
  Organization,
  Project,
  Task
} from '../types/api';

interface CreateProjectInput {
  name: string;
  description?: string;
}

interface CreateOrganizationInput {
  name: string;
  description?: string;
}

export const useOrganizations = () =>
  useQuery({
    queryKey: ['organizations'],
    queryFn: async () => {
      const res = await apiClient.get<Organization[]>('/api/organizations');
      return res.data;
    }
  });

export const useCreateOrganization = () => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: async (payload: CreateOrganizationInput) => {
      const res = await apiClient.post<Organization>('/api/organizations', payload);
      return res.data;
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['organizations'] });
    }
  });
};

export const useProjects = (organizationId?: string) =>
  useQuery({
    enabled: Boolean(organizationId),
    queryKey: ['projects', organizationId],
    queryFn: async () => {
      const res = await apiClient.get<Project[]>(`/api/organizations/${organizationId}/projects`);
      return res.data;
    }
  });

export const useCreateProject = (organizationId: string) => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: async (payload: CreateProjectInput) => {
      const res = await apiClient.post<Project>(`/api/organizations/${organizationId}/projects`, payload);
      return res.data;
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['projects', organizationId] });
    }
  });
};

export const useBoard = (projectId?: string) =>
  useQuery({
    enabled: Boolean(projectId),
    queryKey: ['board', projectId],
    queryFn: async () => {
      const res = await apiClient.get<BoardResponse>(`/api/projects/${projectId}/board`);
      return res.data;
    }
  });

export const useCreateColumn = (projectId: string) => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: async (payload: CreateColumnInput) => {
      const res = await apiClient.post<BoardColumn>(`/api/projects/${projectId}/columns`, payload);
      return res.data;
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['board', projectId] });
    }
  });
};

export const useCreateTask = (projectId: string) => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: async (payload: CreateTaskInput) => {
      const res = await apiClient.post<Task>(`/api/projects/${projectId}/tasks`, payload);
      return res.data;
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['board', projectId] });
    }
  });
};

export const useMoveTask = (projectId: string) => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: async ({ taskId, targetColumnId }: { taskId: number; targetColumnId: number }) => {
      const res = await apiClient.patch<Task>(`/api/projects/${projectId}/tasks/${taskId}/move`, {
        targetColumnId
      });
      return res.data;
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['board', projectId] });
      queryClient.invalidateQueries({ queryKey: ['activity', projectId] });
    }
  });
};

export const useActivity = (projectId?: string) =>
  useQuery({
    enabled: Boolean(projectId),
    queryKey: ['activity', projectId],
    queryFn: async () => {
      const res = await apiClient.get<ActivityLog[]>(`/api/projects/${projectId}/activity`);
      return res.data;
    }
  });

export const useAuthMutations = () => {
  const login = useMutation({
    mutationFn: async (payload: { email: string; password: string }) => {
      const res = await apiClient.post<AuthResponse>('/api/auth/login', payload);
      return res.data;
    }
  });

  const register = useMutation({
    mutationFn: async (payload: { email: string; password: string; displayName: string; timezone?: string }) => {
      const res = await apiClient.post<AuthResponse>('/api/auth/register', payload);
      return res.data;
    }
  });

  return { login, register };
};
