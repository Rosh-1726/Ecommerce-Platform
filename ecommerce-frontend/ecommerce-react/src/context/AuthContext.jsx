import { createContext, useContext, useState, useCallback } from "react";
import * as authApi from "../api/auth";
import { apiErrorMessage } from "../api/client";

const AuthContext = createContext(null);

function loadUser() {
  const raw = localStorage.getItem("user");
  return raw ? JSON.parse(raw) : null;
}

export function AuthProvider({ children }) {
  const [user, setUser] = useState(loadUser());

  const login = useCallback(async (email, password) => {
    try {
      const { data } = await authApi.login({ email, password });
      localStorage.setItem("accessToken", data.accessToken);
      localStorage.setItem("refreshToken", data.refreshToken);
      const nextUser = {
        email: data.email,
        firstName: data.firstName,
        lastName: data.lastName,
        role: data.role,
        profileImageUrl: data.profileImageUrl,
      };
      localStorage.setItem("user", JSON.stringify(nextUser));
      setUser(nextUser);
      return { ok: true };
    } catch (error) {
      return { ok: false, message: apiErrorMessage(error, "Couldn't sign in with those details.") };
    }
  }, []);

  const register = useCallback(async (fields) => {
    try {
      const { data } = await authApi.register(fields);
      return { ok: true, message: data.message };
    } catch (error) {
      return { ok: false, message: apiErrorMessage(error, "Registration failed.") };
    }
  }, []);

  const logout = useCallback(() => {
    localStorage.removeItem("accessToken");
    localStorage.removeItem("refreshToken");
    localStorage.removeItem("user");
    setUser(null);
  }, []);

  const refreshUserRole = useCallback((role) => {
    setUser((prev) => {
      if (!prev) return prev;
      const next = { ...prev, role };
      localStorage.setItem("user", JSON.stringify(next));
      return next;
    });
  }, []);

  return (
    <AuthContext.Provider value={{ user, login, register, logout, refreshUserRole }}>
      {children}
    </AuthContext.Provider>
  );
}

export function useAuth() {
  const ctx = useContext(AuthContext);
  if (!ctx) throw new Error("useAuth must be used inside AuthProvider");
  return ctx;
}
