/**
 * Unit tests for dashboard primary navigation (#1058).
 * Gate 2: written to fail until Suplencias/Reportes nav + canonical redirects exist.
 */
import { describe, it, expect } from "vitest";
import { existsSync, readFileSync } from "node:fs";
import { resolve } from "node:path";
import {
  CANONICAL_ADMIN_REDIRECTS,
  DASHBOARD_NAV_ITEMS,
} from "@/lib/dashboard-nav";
import esMessages from "../../../messages/es.json";
import enMessages from "../../../messages/en.json";
import nextConfig from "../../../next.config";

describe("DASHBOARD_NAV_ITEMS (#1058)", () => {
  it("exposes Suplencias at the canonical route", () => {
    const item = DASHBOARD_NAV_ITEMS.find((n) => n.labelKey === "suplencias");
    expect(item).toBeDefined();
    expect(item?.href).toBe("/dashboard/suplencias");
    expect(item?.adminOnly).toBeFalsy();
  });

  it("exposes Reportes at the canonical route", () => {
    const item = DASHBOARD_NAV_ITEMS.find((n) => n.labelKey === "reportes");
    expect(item).toBeDefined();
    expect(item?.href).toBe("/dashboard/reportes");
    expect(item?.adminOnly).toBeFalsy();
  });

  it("keeps Administración admin-gated", () => {
    const admin = DASHBOARD_NAV_ITEMS.find((n) => n.labelKey === "administracion");
    expect(admin?.adminOnly).toBe(true);
    expect(admin?.href).toBe("/dashboard/administracion");
  });

  it("points Items and Auditoría at canonical dashboard routes", () => {
    expect(DASHBOARD_NAV_ITEMS.find((n) => n.labelKey === "items")?.href).toBe(
      "/dashboard/items",
    );
    expect(
      DASHBOARD_NAV_ITEMS.find((n) => n.labelKey === "auditoria")?.href,
    ).toBe("/dashboard/auditoria");
  });
});

describe("navigation i18n keys (#1058)", () => {
  it("es.json has navigation.suplencias and navigation.reportes", () => {
    expect(esMessages.navigation.suplencias).toBeTruthy();
    expect(esMessages.navigation.reportes).toBeTruthy();
  });

  it("en.json has navigation.suplencias and navigation.reportes", () => {
    expect(enMessages.navigation.suplencias).toBeTruthy();
    expect(enMessages.navigation.reportes).toBeTruthy();
  });
});

describe("canonical admin redirects (#1058)", () => {
  it("declares redirects from legacy admin duplicates to canonical routes", async () => {
    expect(typeof nextConfig.redirects).toBe("function");
    const rules = await nextConfig.redirects!();
    for (const expected of CANONICAL_ADMIN_REDIRECTS) {
      const match = rules.find(
        (r) =>
          r.source === expected.source && r.destination === expected.destination,
      );
      expect(match).toBeDefined();
    }
  });

  it("does not keep a full duplicate Items page under administración", () => {
    const adminItems = resolve(
      __dirname,
      "../../app/dashboard/administracion/items/page.tsx",
    );
    if (!existsSync(adminItems)) {
      return;
    }
    const source = readFileSync(adminItems, "utf8");
    // Redirect-only stubs are fine; a full CRUD page is not.
    expect(source).toMatch(/redirect\s*\(/);
    expect(source).not.toMatch(/useDescuentosYRecargos/);
    expect(source).not.toMatch(/btn-nuevo-item/);
  });

  it("does not keep a full duplicate Auditoría page under administración", () => {
    const adminAuditoria = resolve(
      __dirname,
      "../../app/dashboard/administracion/auditoria/page.tsx",
    );
    if (!existsSync(adminAuditoria)) {
      return;
    }
    const source = readFileSync(adminAuditoria, "utf8");
    expect(source).toMatch(/redirect\s*\(/);
    expect(source).not.toMatch(/useAuditoria/);
  });
});
