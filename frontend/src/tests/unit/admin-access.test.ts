/**
 * Unit tests for admin route guard helpers (issue #1052, CU78).
 * Spec: docs/openspec/changes/fix-1052-admin-route-guard/specs/admin-route-guard
 */
import { describe, it, expect, beforeEach, afterEach } from "vitest";
import {
  isAdminType,
  isAdminRoute,
  shouldDenyAdminRoute,
  AUTH_ROLE_COOKIE,
  AUTH_STATUS_COOKIE,
  setAuthCookies,
  clearAuthCookies,
  FORBIDDEN_QUERY,
} from "@/lib/admin-access";

describe("isAdminType", () => {
  it("returns true for ADMIN, ADMINISTRADOR, ESCRIBANO (case-insensitive)", () => {
    expect(isAdminType("ADMIN")).toBe(true);
    expect(isAdminType("administrador")).toBe(true);
    expect(isAdminType("Escribano")).toBe(true);
  });

  it("returns false for EMPLEADO and empty", () => {
    expect(isAdminType("EMPLEADO")).toBe(false);
    expect(isAdminType(undefined)).toBe(false);
    expect(isAdminType("")).toBe(false);
  });
});

describe("isAdminRoute", () => {
  it("matches /dashboard/administration and sub-paths", () => {
    expect(isAdminRoute("/dashboard/administration")).toBe(true);
    expect(isAdminRoute("/dashboard/administration/users")).toBe(true);
    expect(isAdminRoute("/dashboard/administration/roles")).toBe(true);
    expect(isAdminRoute("/dashboard/audit")).toBe(true);
  });

  it("does not match other dashboard paths", () => {
    expect(isAdminRoute("/dashboard")).toBe(false);
    expect(isAdminRoute("/dashboard/managements")).toBe(false);
    expect(isAdminRoute("/login")).toBe(false);
  });
});

describe("shouldDenyAdminRoute (edge decision)", () => {
  it("denies a non-admin role on an administration path (issue #1052)", () => {
    expect(
      shouldDenyAdminRoute("/dashboard/administration/users", "1", "EMPLEADO"),
    ).toBe(true);
  });

  it("allows an admin-capable role on an administration path", () => {
    expect(
      shouldDenyAdminRoute("/dashboard/administration/users", "1", "ADMIN"),
    ).toBe(false);
    expect(
      shouldDenyAdminRoute("/dashboard/administration", "authenticated", "ESCRIBANO"),
    ).toBe(false);
  });

  it("denies admin path when role cookie is missing", () => {
    expect(shouldDenyAdminRoute("/dashboard/administration/roles", "1", undefined)).toBe(
      true,
    );
  });

  it("does not deny non-admin paths", () => {
    expect(shouldDenyAdminRoute("/dashboard/managements", "1", "EMPLEADO")).toBe(false);
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
