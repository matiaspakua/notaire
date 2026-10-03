/**
 * Unit tests for auth-store (Zustand)
 * CU: Login / Authentication flow — issue #1051 HttpOnly JWT
 */
import { describe, it, expect, beforeEach, vi } from "vitest";
import { partializeAuthState, useAuthStore } from "@/store/auth-store";
import type { DtoUsuario } from "@/types";

// Mock localStorage for Zustand persist middleware
const localStorageMock = {
  getItem: vi.fn(() => null),
  setItem: vi.fn(),
  removeItem: vi.fn(),
  clear: vi.fn(),
  key: vi.fn(() => null),
  length: 0,
};

Object.defineProperty(window, "localStorage", {
  value: localStorageMock,
  writable: true,
});

// Mock the persist middleware to avoid storage issues
vi.mock("zustand/middleware", () => ({
  persist: vi.fn((config) => config),
}));

// Reset store state between tests
beforeEach(() => {
  useAuthStore.setState({ user: null, token: null, isAuthenticated: false });
  vi.clearAllMocks();
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
  it("sets user and isAuthenticated on login without keeping a script-readable token", () => {
    const { login } = useAuthStore.getState();
    login(adminUser);

    const state = useAuthStore.getState();
    expect(state.isAuthenticated).toBe(true);
    expect(state.user).toEqual(adminUser);
    expect(state.token).toBeNull();
  });
});

describe("partializeAuthState (issue #1051)", () => {
  it("does not persist the JWT token", () => {
    const persisted = partializeAuthState({
      user: adminUser,
      token: "should-not-persist",
      isAuthenticated: true,
      login: () => undefined,
      logout: () => undefined,
      isAdmin: () => true,
    });

    expect(persisted).toEqual({
      user: adminUser,
      isAuthenticated: true,
    });
    expect(persisted).not.toHaveProperty("token");
  });
});

describe("useAuthStore — logout()", () => {
  it("clears user, token and isAuthenticated on logout", () => {
    useAuthStore.setState({ user: adminUser, token: null, isAuthenticated: true });
    useAuthStore.getState().logout();

    const state = useAuthStore.getState();
    expect(state.isAuthenticated).toBe(false);
    expect(state.user).toBeNull();
    expect(state.token).toBeNull();
  });

  it("clears UX auth cookies on logout (issues #392 / #1052)", () => {
    const { login, logout } = useAuthStore.getState();
    login(adminUser);
    expect(document.cookie).toContain("notaire-auth-status=");
    expect(document.cookie).toContain("notaire-auth-role=ADMIN");

    logout();

    // After logout, UX auth cookies must be gone so /login is not bounced
    // back to /dashboard and admin paths are not edge-allowed.
    expect(document.cookie).not.toContain("notaire-auth-status=1");
    expect(document.cookie).not.toContain("notaire-auth-role=ADMIN");
  });

  it("sets role cookie on login for edge admin guard (issue #1052)", () => {
    useAuthStore.getState().login(empleadoUser);
    expect(document.cookie).toContain("notaire-auth-role=EMPLEADO");
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
