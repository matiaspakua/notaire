"use client";

import { create } from "zustand";
import { persist } from "zustand/middleware";
import type { DtoUsuario } from "@/types";

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

      login: (user: DtoUsuario, token: string) =>
        set({ user, token, isAuthenticated: true }),

      logout: () => {
        // Clear the auth status cookie consumed by the Next.js middleware so the
        // user is not redirected back to /dashboard from /login after logout.
        // Must mirror the attributes used on login (path, SameSite) for browsers
        // to recognise it as the same cookie.
        if (typeof document !== "undefined") {
          document.cookie =
            "notaire-auth-status=; path=/; SameSite=Lax; Max-Age=0";
        }
        set({ user: null, token: null, isAuthenticated: false });
      },

      isAdmin: () => {
        const tipo = get().user?.tipo?.toUpperCase();
        return (
          tipo === "ADMIN" ||
          tipo === "ADMINISTRADOR" ||
          tipo === "ESCRIBANO"
        );
      },
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
