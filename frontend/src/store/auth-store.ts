"use client";

import { create } from "zustand";
import { persist } from "zustand/middleware";
import type { DtoUsuario } from "@/types";

interface AuthState {
  user: DtoUsuario | null;
  isAuthenticated: boolean;
  login: (user: DtoUsuario) => void;
  logout: () => void;
  isAdmin: () => boolean;
}

export const useAuthStore = create<AuthState>()(
  persist(
    (set, get) => ({
      user: null,
      isAuthenticated: false,

      login: (user: DtoUsuario) =>
        set({ user, isAuthenticated: true }),

      logout: () => set({ user: null, isAuthenticated: false }),

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
      partialize: (state) => ({ user: state.user, isAuthenticated: state.isAuthenticated }),
    }
  )
);
