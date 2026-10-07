/**
 * Unit tests for session-expiry handling (issue #1053, CU84).
 * Spec: docs/openspec/changes/fix-1053-session-401-handling/specs/session-expiry-handling
 */
import { describe, it, expect, vi, beforeEach, afterEach } from "vitest";
import {
  handleAuthenticatedSessionExpiry,
  resetSessionExpiryGuardForTests,
} from "@/lib/session-expiry";
import { useAuthStore } from "@/store/auth-store";

const assignMock = vi.fn();

beforeEach(() => {
  resetSessionExpiryGuardForTests();
  vi.clearAllMocks();
  window.localStorage.clear();
  useAuthStore.setState({
    user: { nombre: "admin", tipo: "ADMIN", valido: true },
    token: "valid-jwt",
    isAuthenticated: true,
  });
  Object.defineProperty(window, "location", {
    configurable: true,
    value: { ...window.location, assign: assignMock, href: "http://localhost/dashboard" },
  });
});

afterEach(() => {
  resetSessionExpiryGuardForTests();
});

describe("handleAuthenticatedSessionExpiry (issue #1053)", () => {
  it("clears auth and navigates to /login?expired=1 on authenticated 401", () => {
    const handled = handleAuthenticatedSessionExpiry(401, "/gestiones");

    expect(handled).toBe(true);
    expect(useAuthStore.getState().isAuthenticated).toBe(false);
    expect(useAuthStore.getState().token).toBeNull();
    expect(assignMock).toHaveBeenCalledWith("/login?expired=1");
  });

  it("does not logout or redirect on non-401 statuses", () => {
    for (const status of [400, 403, 404, 409, 500]) {
      resetSessionExpiryGuardForTests();
      assignMock.mockClear();
      useAuthStore.setState({
        user: { nombre: "admin", tipo: "ADMIN", valido: true },
        token: "valid-jwt",
        isAuthenticated: true,
      });

      const handled = handleAuthenticatedSessionExpiry(status, "/gestiones");

      expect(handled).toBe(false);
      expect(useAuthStore.getState().isAuthenticated).toBe(true);
      expect(assignMock).not.toHaveBeenCalled();
    }
  });

  it("does not expiry-redirect login-endpoint 401 (bad credentials)", () => {
    useAuthStore.setState({ user: null, token: null, isAuthenticated: false });

    const handled = handleAuthenticatedSessionExpiry(401, "/usuarios/login");

    expect(handled).toBe(false);
    expect(assignMock).not.toHaveBeenCalled();
  });

  it("does not expiry-redirect when there is no local session", () => {
    useAuthStore.setState({ user: null, token: null, isAuthenticated: false });

    const handled = handleAuthenticatedSessionExpiry(401, "/gestiones");

    expect(handled).toBe(false);
    expect(assignMock).not.toHaveBeenCalled();
  });

  it("treats isAuthenticated without a token as an active session (issue #1051)", () => {
    useAuthStore.setState({
      user: { nombre: "admin", tipo: "ADMIN", valido: true },
      token: null,
      isAuthenticated: true,
    });

    const handled = handleAuthenticatedSessionExpiry(401, "/gestiones");

    expect(handled).toBe(true);
    expect(assignMock).toHaveBeenCalledWith("/login?expired=1");
  });

  it("is idempotent under concurrent 401s (re-entrancy guard)", () => {
    handleAuthenticatedSessionExpiry(401, "/gestiones");
    handleAuthenticatedSessionExpiry(401, "/personas");

    expect(assignMock).toHaveBeenCalledTimes(1);
    expect(assignMock).toHaveBeenCalledWith("/login?expired=1");
  });
});
