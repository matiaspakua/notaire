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
      "navigation.suplencias",
      "navigation.reportes",
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

  // ──────────────────────────────────────────────
  // #1059 — remaining page coverage + login leftovers
  // ──────────────────────────────────────────────

  const requiredGapKeys = [
    // common status labels reused by roles/workflows
    "common.active",
    "common.inactive",
    // login leftovers
    "login.connectionError",
    "login.lockoutError",
    "login.validationRequired",
    "login.welcome",
    "login.forgotPassword",
    "login.footerSecure",
    // auditoria leftover
    "auditoria.allModules",
    // items (canonical dashboard page — expanded catalog)
    "items.title",
    "items.description",
    "items.newItem",
    "items.editItem",
    "items.created",
    "items.updated",
    "items.deleted",
    "items.errorSave",
    "items.errorDelete",
    "items.noData",
    "items.reasonRequired",
    "items.fields.nombre",
    "items.fields.valor",
    "items.fields.presupuesto",
    "items.fields.tipo",
    "items.fields.motivo",
    "items.types.NORMAL",
    "items.types.DESCUENTO",
    "items.types.RECARGO",
    "items.report.title",
    "items.report.empty",
    "items.report.consult",
    // suplencias
    "suplencias.title",
    "suplencias.description",
    "suplencias.newSuplencia",
    "suplencias.editSuplencia",
    "suplencias.created",
    "suplencias.updated",
    "suplencias.deleted",
    "suplencias.errorSave",
    "suplencias.errorDelete",
    "suplencias.noData",
    "suplencias.fields.escribano",
    "suplencias.fields.suplente",
    "suplencias.fields.desde",
    "suplencias.fields.hasta",
    // reportes
    "reportes.title",
    "reportes.description",
    "reportes.generated",
    "reportes.errorGenerate",
    "reportes.presupuesto.title",
    "reportes.presupuestoInmuebles.title",
    "reportes.historial.title",
    "reportes.ddjjMensual.title",
    "reportes.ddjjRentas.title",
    "reportes.libroIndice.title",
    "reportes.deudaDocumentos.title",
    // administracion.roles
    "administracion.roles.title",
    "administracion.roles.description",
    "administracion.roles.newRol",
    "administracion.roles.editRol",
    "administracion.roles.created",
    "administracion.roles.updated",
    "administracion.roles.deleted",
    "administracion.roles.errorSave",
    "administracion.roles.errorDelete",
    "administracion.roles.noData",
    "administracion.roles.nameRequired",
    "administracion.roles.noPermissions",
    "administracion.roles.fields.modulos",
    "administracion.roles.fields.activo",
    "administracion.roles.fields.activoHint",
    "administracion.roles.modules.administracion",
    "administracion.roles.modules.usuarios",
    "administracion.roles.modules.tramites",
    "administracion.roles.modules.presupuestos",
    "administracion.roles.modules.escrituras",
    "administracion.roles.modules.personas",
    "administracion.roles.modules.auditoria",
    "administracion.roles.modules.workflows",
    // administracion.workflows (list)
    "administracion.workflows.title",
    "administracion.workflows.description",
    "administracion.workflows.newWorkflow",
    "administracion.workflows.editWorkflow",
    "administracion.workflows.created",
    "administracion.workflows.updated",
    "administracion.workflows.deleted",
    "administracion.workflows.errorSave",
    "administracion.workflows.errorDeleteHasNodes",
    "administracion.workflows.noData",
    "administracion.workflows.nameRequired",
    "administracion.workflows.searchPlaceholder",
    "administracion.workflows.editGraph",
    "administracion.workflows.editData",
    "administracion.workflows.deleteAction",
    "administracion.workflows.enabledForAssignment",
    "administracion.workflows.fields.namePlaceholder",
    "administracion.workflows.fields.descriptionPlaceholder",
    // administracion.workflows.editor
    "administracion.workflows.editor.title",
    "administracion.workflows.editor.back",
    "administracion.workflows.editor.editMode",
    "administracion.workflows.editor.viewMode",
    "administracion.workflows.editor.addNode",
    "administracion.workflows.editor.validate",
    "administracion.workflows.editor.valid",
    "administracion.workflows.editor.invalid",
    "administracion.workflows.editor.errorValidate",
    "administracion.workflows.editor.errorCreateTransition",
    "administracion.workflows.editor.errorSavePosition",
    "administracion.workflows.editor.selectStatus",
    "administracion.workflows.editor.nodeAdded",
    "administracion.workflows.editor.errorAddNode",
    "administracion.workflows.editor.nodeDeleted",
    "administracion.workflows.editor.errorDeleteNodeHasTransitions",
    "administracion.workflows.editor.transitionDeleted",
    "administracion.workflows.editor.errorDeleteTransition",
    "administracion.workflows.editor.consistencyErrors",
    "administracion.workflows.editor.hint",
    "administracion.workflows.editor.addNodeTitle",
    "administracion.workflows.editor.fields.estado",
    "administracion.workflows.editor.fields.estadoPlaceholder",
    "administracion.workflows.editor.fields.tipo",
    "administracion.workflows.editor.types.INITIAL",
    "administracion.workflows.editor.types.INTERMEDIATE",
    "administracion.workflows.editor.types.FINAL",
    "administracion.workflows.editor.nodeFallback",
  ];

  it("es.json has all required #1059 gap-page and login leftover keys", () => {
    requiredGapKeys.forEach((key) => {
      expect(esKeys).toContain(key);
    });
  });

  it("en.json has all required #1059 gap-page and login leftover keys", () => {
    requiredGapKeys.forEach((key) => {
      expect(enKeys).toContain(key);
    });
  });

  it("required #1059 keys have non-empty values in both catalogs", () => {
    function valueAt(obj: Record<string, unknown>, path: string): unknown {
      return path.split(".").reduce<unknown>((acc, part) => {
        if (acc && typeof acc === "object" && !Array.isArray(acc)) {
          return (acc as Record<string, unknown>)[part];
        }
        return undefined;
      }, obj);
    }

    requiredGapKeys.forEach((key) => {
      const esVal = valueAt(esMessages as Record<string, unknown>, key);
      const enVal = valueAt(enMessages as Record<string, unknown>, key);
      expect(typeof esVal).toBe("string");
      expect(typeof enVal).toBe("string");
      expect((esVal as string).length).toBeGreaterThan(0);
      expect((enVal as string).length).toBeGreaterThan(0);
    });
  });
});
