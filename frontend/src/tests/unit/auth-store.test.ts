/**
 * Unit tests for auth-store (Zustand)
 * CU: Login / Authentication flow
 */
import { describe, it, expect, beforeEach } from "vitest";
import { useAuthStore } from "@/store/auth-store";
import type { DtoUsuario } from "@/types";

// Reset store state between tests
beforeEach(() => {
  useAuthStore.setState({ user: null, isAuthenticated: false });
});

const adminUser: DtoUsuario = {
  idUsuario: 1,
  nombre: "admin",
  tipo: "ADMIN",
  valido: true,
};

const empleadoUser: DtoUsuario = {
  idUsuario: 2,
  nombre: "empleado1",
  tipo: "EMPLEADO",
  valido: true,
};

describe("useAuthStore — login()", () => {
  it("sets user and isAuthenticated on login", () => {
    const { login } = useAuthStore.getState();
    login(adminUser);

    const state = useAuthStore.getState();
    expect(state.isAuthenticated).toBe(true);
    expect(state.user).toEqual(adminUser);
  });
});

describe("useAuthStore — logout()", () => {
  it("clears user and isAuthenticated on logout", () => {
    useAuthStore.setState({ user: adminUser, isAuthenticated: true });
    useAuthStore.getState().logout();

    const state = useAuthStore.getState();
    expect(state.isAuthenticated).toBe(false);
    expect(state.user).toBeNull();
  });
});

describe("useAuthStore — isAdmin()", () => {
  it("returns true for tipo ADMIN", () => {
    useAuthStore.setState({ user: adminUser, isAuthenticated: true });
    expect(useAuthStore.getState().isAdmin()).toBe(true);
  });

  it("returns true for tipo ESCRIBANO", () => {
    useAuthStore.setState({ user: { ...adminUser, tipo: "ESCRIBANO" }, isAuthenticated: true });
    expect(useAuthStore.getState().isAdmin()).toBe(true);
  });

  it("returns true for tipo ADMINISTRADOR", () => {
    useAuthStore.setState({ user: { ...adminUser, tipo: "ADMINISTRADOR" }, isAuthenticated: true });
    expect(useAuthStore.getState().isAdmin()).toBe(true);
  });

  it("returns false for tipo EMPLEADO", () => {
    useAuthStore.setState({ user: empleadoUser, isAuthenticated: true });
    expect(useAuthStore.getState().isAdmin()).toBe(false);
  });

  it("returns false when not authenticated", () => {
    expect(useAuthStore.getState().isAdmin()).toBe(false);
  });
});
