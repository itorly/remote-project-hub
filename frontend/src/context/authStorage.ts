import { AuthResponse, AuthUser } from '../types/api';

export type AuthPersistence = 'memory' | 'local';

const TOKEN_KEY = 'rph_token';
const USER_KEY = 'rph_user';

let memoryToken: string | null = null;
let memoryUser: AuthUser | null = null;

const isBrowser = () => typeof window !== 'undefined';

export const persistAuth = (
  response: AuthResponse,
  persistence: AuthPersistence
) => {
  const user: AuthUser = {
    id: response.userId,
    email: response.email,
    displayName: response.displayName
  };

  memoryToken = response.token;
  memoryUser = user;

  if (persistence === 'local' && isBrowser()) {
    localStorage.setItem(TOKEN_KEY, response.token);
    localStorage.setItem(USER_KEY, JSON.stringify(user));
  } else if (isBrowser()) {
    localStorage.removeItem(TOKEN_KEY);
    localStorage.removeItem(USER_KEY);
  }
};

export const loadStoredAuth = (): { token: string; user: AuthUser; persistence: AuthPersistence } | null => {
  if (!isBrowser()) return null;
  const token = localStorage.getItem(TOKEN_KEY);
  const userRaw = localStorage.getItem(USER_KEY);
  if (!token || !userRaw) return null;

  try {
    const user = JSON.parse(userRaw) as AuthUser;
    memoryToken = token;
    memoryUser = user;
    return { token, user, persistence: 'local' };
  } catch (err) {
    console.error('Failed to parse stored user', err);
    return null;
  }
};

export const clearAuth = () => {
  memoryToken = null;
  memoryUser = null;
  if (isBrowser()) {
    localStorage.removeItem(TOKEN_KEY);
    localStorage.removeItem(USER_KEY);
  }
};

export const getAuthToken = () => {
  if (memoryToken) return memoryToken;
  if (!isBrowser()) return null;
  return localStorage.getItem(TOKEN_KEY);
};

export const getAuthUser = () => memoryUser;
