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
  useAuthStore.setState({ user: null, isAuthenticated: false });
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

  it("clears the notaire-auth-status cookie on logout (issue #392)", () => {
    // Simulate the cookie set by the login flow.
    document.cookie = "notaire-auth-status=1; path=/; SameSite=Lax";
    expect(document.cookie).toContain("notaire-auth-status=1");

    useAuthStore.setState({ user: adminUser, isAuthenticated: true });
    useAuthStore.getState().logout();

    // After logout, the middleware-facing cookie must be gone so that
    // navigating to /login is not bounced back to /dashboard.
    expect(document.cookie).not.toContain("notaire-auth-status=1");
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
