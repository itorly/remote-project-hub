import { PropsWithChildren, createContext, useEffect, useMemo, useState } from 'react';
import { AuthPersistence, clearAuth, getAuthToken, loadStoredAuth, persistAuth } from './authStorage';
import { AuthResponse, AuthUser } from '../types/api';

interface AuthContextValue {
  token: string | null;
  user: AuthUser | null;
  persistence: AuthPersistence;
  setPersistence: (mode: AuthPersistence) => void;
  applyAuth: (response: AuthResponse, mode?: AuthPersistence) => void;
  logout: () => void;
}

export const AuthContext = createContext<AuthContextValue | undefined>(undefined);

export const AuthProvider = ({ children }: PropsWithChildren) => {
  const [token, setToken] = useState<string | null>(null);
  const [user, setUser] = useState<AuthUser | null>(null);
  const [persistence, setPersistenceState] = useState<AuthPersistence>('local');

  useEffect(() => {
    const stored = loadStoredAuth();
    if (stored) {
      setToken(stored.token);
      setUser(stored.user);
      setPersistence(stored.persistence);
    }
  }, []);

  const applyAuth = (response: AuthResponse, mode?: AuthPersistence) => {
    const persistenceMode = mode ?? persistence;
    persistAuth(response, persistenceMode);
    setToken(response.token);
    setUser({ id: response.userId, email: response.email, displayName: response.displayName });
    setPersistenceState(persistenceMode);
  };

  const updatePersistence = (mode: AuthPersistence) => {
    setPersistenceState(mode);
    if (token && user) {
      persistAuth(
        {
          token,
          userId: user.id,
          email: user.email,
          displayName: user.displayName
        },
        mode
      );
    }
  };

  const logout = () => {
    clearAuth();
    setToken(null);
    setUser(null);
  };

  const value = useMemo(
    () => ({ token, user, persistence, setPersistence: updatePersistence, applyAuth, logout }),
    [token, user, persistence]
  );

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
};

export const authHeader = () => {
  const tokenValue = getAuthToken();
  return tokenValue ? { Authorization: `Bearer ${tokenValue}` } : {};
};
