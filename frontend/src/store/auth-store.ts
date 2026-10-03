"use client";

import { create } from "zustand";
import { persist } from "zustand/middleware";
import type { DtoUsuario } from "@/types";
import {
  clearAuthCookies,
  isAdminTipo,
  setAuthCookies,
} from "@/lib/admin-access";

interface AuthState {
  user: DtoUsuario | null;
  /** Kept null for browser sessions — JWT lives in HttpOnly cookie (#1051). */
  token: string | null;
  isAuthenticated: boolean;
  login: (user: DtoUsuario, token?: string | null) => void;
  logout: () => void;
  isAdmin: () => boolean;
}

/** Persist shape: never store JWT in localStorage (issue #1051). */
export function partializeAuthState(state: AuthState) {
  return {
    user: state.user,
    isAuthenticated: state.isAuthenticated,
  };
}

export const useAuthStore = create<AuthState>()(
  persist(
    (set, get) => ({
      user: null,
      token: null,
      isAuthenticated: false,

      login: (user: DtoUsuario, _token?: string | null) => {
        // Edge middleware reads these non-credential cookies (issue #1052).
        // JWT credential is HttpOnly cookie from the login Set-Cookie (#1051).
        setAuthCookies(user.tipo ?? "");
        set({ user, token: null, isAuthenticated: true });
      },

      logout: () => {
        // Clear middleware cookies so /login is not bounced back to /dashboard.
        clearAuthCookies();
        set({ user: null, token: null, isAuthenticated: false });
      },

      isAdmin: () => isAdminTipo(get().user?.tipo),
    }),
    {
      name: "notaire-auth",
      partialize: partializeAuthState,
    }
  )
);
