/**
 * Unit tests for auth-store (Zustand)
 * CU: Login / Authentication flow
 */
import { describe, it, expect, beforeEach, vi } from "vitest";
import { useAuthStore } from "@/store/auth-store";
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

Object.defineProperty(window, 'localStorage', {
  value: localStorageMock,
  writable: true,
});

// Mock the persist middleware to avoid storage issues
vi.mock('zustand/middleware', () => ({
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
  it("sets user, token and isAuthenticated on login", () => {
    const { login } = useAuthStore.getState();
    login(adminUser, "fake-jwt-token");

    const state = useAuthStore.getState();
    expect(state.isAuthenticated).toBe(true);
    expect(state.user).toEqual(adminUser);
    expect(state.token).toBe("fake-jwt-token");
  });
});

describe("useAuthStore — logout()", () => {
  it("clears user, token and isAuthenticated on logout", () => {
    useAuthStore.setState({ user: adminUser, token: "fake-jwt-token", isAuthenticated: true });
    useAuthStore.getState().logout();

    const state = useAuthStore.getState();
    expect(state.isAuthenticated).toBe(false);
    expect(state.user).toBeNull();
    expect(state.token).toBeNull();
  });

  it("clears middleware auth cookies on logout (issues #392 / #1052)", () => {
    const { login, logout } = useAuthStore.getState();
    login(adminUser, "fake-jwt-token");
    expect(document.cookie).toContain("notaire-auth-status=");
    expect(document.cookie).toContain("notaire-auth-role=ADMIN");

    logout();

    // After logout, middleware cookies must be gone so /login is not bounced
    // back to /dashboard and admin paths are not edge-allowed.
    expect(document.cookie).not.toContain("notaire-auth-status=1");
    expect(document.cookie).not.toContain("notaire-auth-role=ADMIN");
  });

  it("sets role cookie on login for edge admin guard (issue #1052)", () => {
    useAuthStore.getState().login(empleadoUser, "tok");
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
