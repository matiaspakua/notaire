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
  token: string | null;
  isAuthenticated: boolean;
  login: (user: DtoUsuario, token: string) => void;
  logout: () => void;
  isAdmin: () => boolean;
}

export const useAuthStore = create<AuthState>()(
  persist(
    (set, get) => ({
      user: null,
      token: null,
      isAuthenticated: false,

      login: (user: DtoUsuario, token: string) => {
        // Edge middleware reads these non-credential cookies (issue #1052).
        setAuthCookies(user.tipo ?? "");
        set({ user, token, isAuthenticated: true });
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
      partialize: (state) => ({
        user: state.user,
        token: state.token,
        isAuthenticated: state.isAuthenticated,
      }),
    }
  )
);
