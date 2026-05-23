/**
 * Unit tests for i18n infrastructure (Phase 1 — Foundation).
 */
import { describe, it, expect } from "vitest";
import { isSupportedLocale, DEFAULT_LOCALE } from "@/i18n/request";
import esMessages from "../../../messages/es.json";
import enMessages from "../../../messages/en.json";

// ──────────────────────────────────────────────
// Locale validation
// ──────────────────────────────────────────────

describe("isSupportedLocale", () => {
  it("returns true for 'es'", () => {
    expect(isSupportedLocale("es")).toBe(true);
  });

  it("returns true for 'en'", () => {
    expect(isSupportedLocale("en")).toBe(true);
  });

  it("returns false for unsupported locales", () => {
    expect(isSupportedLocale("fr")).toBe(false);
    expect(isSupportedLocale("pt")).toBe(false);
    expect(isSupportedLocale("")).toBe(false);
    expect(isSupportedLocale("ES")).toBe(false);
  });

  it("default locale is 'es'", () => {
    expect(DEFAULT_LOCALE).toBe("es");
  });
});

// ──────────────────────────────────────────────
// Message file integrity
// ──────────────────────────────────────────────

function getKeys(obj: Record<string, unknown>, prefix = ""): string[] {
  return Object.entries(obj).flatMap(([key, value]) => {
    const fullKey = prefix ? `${prefix}.${key}` : key;
    return typeof value === "object" && value !== null && !Array.isArray(value)
      ? getKeys(value as Record<string, unknown>, fullKey)
      : [fullKey];
  });
}

describe("message file integrity", () => {
  const esKeys = getKeys(esMessages).sort();
  const enKeys = getKeys(enMessages).sort();

  it("both locale files have the same number of keys", () => {
    expect(enKeys.length).toBe(esKeys.length);
  });

  it("both locale files have identical key structure", () => {
    expect(enKeys).toEqual(esKeys);
  });

  it("es.json has all required navigation keys", () => {
    const navKeys = [
      "navigation.home",
      "navigation.gestiones",
      "navigation.presupuestos",
      "navigation.personas",
      "navigation.escrituras",
      "navigation.pagos",
      "navigation.protocolo",
      "navigation.inmuebles",
      "navigation.copias",
      "navigation.items",
      "navigation.documentos",
      "navigation.auditoria",
      "navigation.administracion",
      "navigation.logout",
      "navigation.brand",
      "navigation.brandSubtitle",
    ];
    navKeys.forEach((key) => {
      expect(esKeys).toContain(key);
    });
  });

  it("en.json has all required navigation keys", () => {
    const navKeys = ["navigation.home", "navigation.logout", "navigation.brand"];
    navKeys.forEach((key) => {
      expect(enKeys).toContain(key);
    });
  });

  it("es.json navigation values are non-empty strings", () => {
    const nav = esMessages.navigation;
    Object.values(nav).forEach((value) => {
      expect(typeof value).toBe("string");
      expect(value.length).toBeGreaterThan(0);
    });
  });

  it("en.json navigation values are non-empty strings", () => {
    const nav = enMessages.navigation;
    Object.values(nav).forEach((value) => {
      expect(typeof value).toBe("string");
      expect(value.length).toBeGreaterThan(0);
    });
  });

  it("es.json and en.json have different navigation.home values", () => {
    expect(esMessages.navigation.home).toBe("Inicio");
    expect(enMessages.navigation.home).toBe("Home");
  });

  it("es.json common keys are present", () => {
    const requiredCommon = ["common.create", "common.edit", "common.delete", "common.cancel", "common.save"];
    requiredCommon.forEach((key) => {
      expect(esKeys).toContain(key);
    });
  });

  it("es.json has all module namespaces", () => {
    const modules = ["personas", "gestiones", "escrituras", "pagos", "protocolo",
      "inmuebles", "copias", "items", "documentos", "presupuestos", "auditoria", "administracion"];
    modules.forEach((mod) => {
      const modKeys = esKeys.filter((k) => k.startsWith(`${mod}.`));
      expect(modKeys.length).toBeGreaterThan(0);
    });
  });
});
