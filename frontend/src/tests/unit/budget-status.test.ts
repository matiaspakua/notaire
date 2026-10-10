/**
 * Budget status vocabulary (#1346, RF-02/RF-06; Owner default: the stored codes).
 * The UI listed BORRADOR/APROBADO/RECHAZADO/FACTURADO as Spanish literals, so
 * "Pendiente" budgets (most of the dev DB) could not be filtered, their edit
 * select was blank, and raw codes showed untranslated.
 */
import { describe, it, expect } from "vitest";
import { readFileSync } from "node:fs";
import { join } from "node:path";
import es from "../../../messages/es.json";
import en from "../../../messages/en.json";
import { BUDGET_STATUSES, DEFAULT_BUDGET_STATUS, normalizeBudgetStatus, budgetStatusLabelKey } from "@/lib/budget-status";

describe("budget status vocabulary (#1346)", () => {
  it("is the five stored codes, in workflow order, with BORRADOR as the default", () => {
    expect(BUDGET_STATUSES).toEqual(["BORRADOR", "PENDIENTE", "APROBADO", "RECHAZADO", "FACTURADO"]);
    expect(DEFAULT_BUDGET_STATUS).toBe("BORRADOR");
  });

  it("reads any letter case and spacing as the code, and anything else as unknown", () => {
    expect(normalizeBudgetStatus("Pendiente")).toBe("PENDIENTE");
    expect(normalizeBudgetStatus(" aprobado ")).toBe("APROBADO");
    expect(normalizeBudgetStatus("ACTIVO")).toBeNull();
    expect(normalizeBudgetStatus(undefined)).toBeNull();
  });

  it("has a translated label for every code in es and en", () => {
    for (const code of BUDGET_STATUSES) {
      const key = budgetStatusLabelKey(code);
      expect(key).toBe(`status.${code}`);
      expect((es.presupuestos as Record<string, unknown>).status).toHaveProperty(code);
      expect((en.presupuestos as Record<string, unknown>).status).toHaveProperty(code);
    }
    expect((es.presupuestos.status as Record<string, string>).PENDIENTE).toBe("Pendiente");
    expect((en.presupuestos.status as Record<string, string>).PENDIENTE).toBe("Pending");
  });

  it("the budgets page builds its status options from the list, with no Spanish literals", () => {
    const src = readFileSync(join(__dirname, "..", "..", "app", "dashboard", "presupuestos", "page.tsx"), "utf8");
    expect(src).not.toMatch(/>(Borrador|Aprobado|Rechazado|Facturado|Todos)</);
    expect(src).toMatch(/BUDGET_STATUSES\.map/);
  });
});
