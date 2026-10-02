/**
 * Unit tests for admin route guard helpers (issue #1052, CU78).
 * Spec: openspec/changes/fix-1052-admin-route-guard/specs/admin-route-guard
 */
import { describe, it, expect, beforeEach, afterEach } from "vitest";
import {
  isAdminTipo,
  isAdminRoute,
  shouldDenyAdminRoute,
  AUTH_ROLE_COOKIE,
  AUTH_STATUS_COOKIE,
  setAuthCookies,
  clearAuthCookies,
  FORBIDDEN_QUERY,
} from "@/lib/admin-access";

describe("isAdminTipo", () => {
  it("returns true for ADMIN, ADMINISTRADOR, ESCRIBANO (case-insensitive)", () => {
    expect(isAdminTipo("ADMIN")).toBe(true);
    expect(isAdminTipo("administrador")).toBe(true);
    expect(isAdminTipo("Escribano")).toBe(true);
  });

  it("returns false for EMPLEADO and empty", () => {
    expect(isAdminTipo("EMPLEADO")).toBe(false);
    expect(isAdminTipo(undefined)).toBe(false);
    expect(isAdminTipo("")).toBe(false);
  });
});

describe("isAdminRoute", () => {
  it("matches /dashboard/administracion and sub-paths", () => {
    expect(isAdminRoute("/dashboard/administracion")).toBe(true);
    expect(isAdminRoute("/dashboard/administracion/usuarios")).toBe(true);
    expect(isAdminRoute("/dashboard/administracion/roles")).toBe(true);
  });

  it("does not match other dashboard paths", () => {
    expect(isAdminRoute("/dashboard")).toBe(false);
    expect(isAdminRoute("/dashboard/gestiones")).toBe(false);
    expect(isAdminRoute("/dashboard/auditoria")).toBe(false);
    expect(isAdminRoute("/login")).toBe(false);
  });
});

describe("shouldDenyAdminRoute (edge decision)", () => {
  it("denies non-admin role on administración path (issue #1052)", () => {
    expect(
      shouldDenyAdminRoute("/dashboard/administracion/usuarios", "1", "EMPLEADO"),
    ).toBe(true);
  });

  it("allows admin-capable role on administración path", () => {
    expect(
      shouldDenyAdminRoute("/dashboard/administracion/usuarios", "1", "ADMIN"),
    ).toBe(false);
    expect(
      shouldDenyAdminRoute("/dashboard/administracion", "authenticated", "ESCRIBANO"),
    ).toBe(false);
  });

  it("denies admin path when role cookie is missing", () => {
    expect(shouldDenyAdminRoute("/dashboard/administracion/roles", "1", undefined)).toBe(
      true,
    );
  });

  it("does not deny non-admin paths", () => {
    expect(shouldDenyAdminRoute("/dashboard/gestiones", "1", "EMPLEADO")).toBe(false);
  });
});

describe("auth cookies", () => {
  beforeEach(() => {
    clearAuthCookies();
  });

  afterEach(() => {
    clearAuthCookies();
  });

  it(`sets ${AUTH_STATUS_COOKIE} and ${AUTH_ROLE_COOKIE} on login helper`, () => {
    setAuthCookies("EMPLEADO");
    expect(document.cookie).toContain(`${AUTH_STATUS_COOKIE}=`);
    expect(document.cookie).toContain(`${AUTH_ROLE_COOKIE}=EMPLEADO`);
  });

  it("clears both cookies on logout helper", () => {
    setAuthCookies("ADMIN");
    clearAuthCookies();
    expect(document.cookie).not.toContain(`${AUTH_STATUS_COOKIE}=1`);
    expect(document.cookie).not.toContain(`${AUTH_ROLE_COOKIE}=ADMIN`);
  });
});

describe("FORBIDDEN_QUERY", () => {
  it("uses forbidden=1 as the denial signal", () => {
    expect(FORBIDDEN_QUERY).toBe("forbidden=1");
  });
});
