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

  it("exposes Próximos vencimientos (CU42) at its route with i18n labels", () => {
    const item = DASHBOARD_NAV_ITEMS.find((n) => n.labelKey === "proximosVencimientos");
    expect(item?.href).toBe("/dashboard/proximos-vencimientos");
    expect(esMessages.navigation.proximosVencimientos).toBeTruthy();
    expect(enMessages.navigation.proximosVencimientos).toBeTruthy();
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

// ---------------------------------------------------------------------------
// #1367 — task-based sidebar sections shared with the dashboard module grid
// ---------------------------------------------------------------------------
import { readdirSync, statSync } from "node:fs";
import {
  DASHBOARD_NAV_GROUPS,
  DASHBOARD_NAV_HOME,
  DASHBOARD_MODULES,
} from "@/lib/dashboard-nav";
import { ADMIN_ROUTE_PREFIXES } from "@/lib/admin-access";

describe("DASHBOARD_NAV_GROUPS (#1367)", () => {
  it("has the six owner-approved sections in order with their members", () => {
    const shape = DASHBOARD_NAV_GROUPS.map((g) => [g.labelKey, g.items.map((i) => i.labelKey)]);
    expect(shape).toEqual([
      ["clientes", ["personas", "inmuebles"]],
      ["tramites", ["gestiones", "presupuestos", "pagos"]],
      [
        "documentacion",
        ["documentos", "documentosNecesarios", "documentosEntidadesExternas", "reingresoDocumentacion", "proximosVencimientos"],
      ],
      [
        "escriturasProtocolo",
        ["escrituras", "testimonios", "movimientosTestimonio", "copias", "minutasInscripcion", "protocolo"],
      ],
      ["gestion", ["reportes", "auditoria", "suplencias", "items"]],
      ["administracion", ["administracion"]],
    ]);
    expect(DASHBOARD_NAV_HOME.href).toBe("/dashboard");
  });

  it("translates every section label in es and en", () => {
    const es = (esMessages as Record<string, unknown>).navGroups as Record<string, string> | undefined;
    const en = (enMessages as Record<string, unknown>).navGroups as Record<string, string> | undefined;
    for (const g of DASHBOARD_NAV_GROUPS) {
      expect(es?.[g.labelKey], `es navGroups.${g.labelKey}`).toBeTruthy();
      expect(en?.[g.labelKey], `en navGroups.${g.labelKey}`).toBeTruthy();
    }
  });

  it("lists every top-level dashboard route exactly once", () => {
    const dir = resolve(__dirname, "../../app/dashboard");
    const routes = readdirSync(dir)
      .filter((d) => statSync(resolve(dir, d)).isDirectory() && existsSync(resolve(dir, d, "page.tsx")))
      .map((d) => `/dashboard/${d}`)
      .sort();
    const hrefs = DASHBOARD_NAV_GROUPS.flatMap((g) => g.items.map((i) => i.href));
    expect([...hrefs].sort()).toEqual(routes);
    expect(new Set(hrefs).size).toBe(hrefs.length);
    expect(DASHBOARD_NAV_ITEMS.map((i) => i.href)).toEqual(["/dashboard", ...hrefs]);
  });

  it("marks exactly the admin-guarded routes as adminOnly", () => {
    const adminHrefs = DASHBOARD_NAV_ITEMS.filter((i) => i.adminOnly).map((i) => i.href).sort();
    expect(adminHrefs).toEqual([...ADMIN_ROUTE_PREFIXES].sort());
  });
});

describe("DASHBOARD_MODULES (#1367)", () => {
  it("is derived from the featured nav items, in sidebar order", () => {
    const featured = DASHBOARD_NAV_GROUPS.flatMap((g) => g.items).filter((i) => i.featured);
    expect(DASHBOARD_MODULES).toEqual(featured);
    expect(DASHBOARD_MODULES.map((m) => m.href)).toEqual([
      "/dashboard/personas",
      "/dashboard/inmuebles",
      "/dashboard/gestiones",
      "/dashboard/presupuestos",
      "/dashboard/pagos",
      "/dashboard/documentos",
      "/dashboard/escrituras",
      "/dashboard/copias",
      "/dashboard/protocolo",
      "/dashboard/reportes",
      "/dashboard/auditoria",
      "/dashboard/suplencias",
      "/dashboard/items",
      "/dashboard/administracion",
    ]);
  });

  it("has a dashboard label and description for every module in es and en", () => {
    for (const m of DASHBOARD_MODULES) {
      for (const msgs of [esMessages, enMessages]) {
        const entry = (msgs.dashboard as Record<string, unknown>)[m.labelKey] as Record<string, string> | undefined;
        expect(entry?.label, `dashboard.${m.labelKey}.label`).toBeTruthy();
        expect(entry?.description, `dashboard.${m.labelKey}.description`).toBeTruthy();
      }
    }
  });

  it("the dashboard page no longer keeps its own module array", () => {
    const src = readFileSync(resolve(__dirname, "../../app/dashboard/page.tsx"), "utf8");
    expect(src).toContain("DASHBOARD_MODULES");
    expect(src).not.toMatch(/const modules\s*:/);
  });
});
