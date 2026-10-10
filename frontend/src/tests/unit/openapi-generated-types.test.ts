/**
 * #1260 / #1197 P0.5 — high-traffic hooks consume OpenAPI-generated aliases.
 */
import { describe, expect, it } from "vitest";
import type {
  SubmittedDocument,
  DeedManagement,
  Presupuesto,
  BudgetSummary,
} from "@/types/api";
import { gestionesKeys } from "@/hooks/useManagements";
import { presupuestosKeys } from "@/hooks/useBudgets";
import { documentosPresentadosKeys } from "@/hooks/useSubmittedDocuments";

describe("openapi generated types (#1260)", () => {
  it("exports schema aliases used by gestiones, presupuestos and documentos hooks", () => {
    const gestion: DeedManagement = { idManagement: 1, procedureCount: 2 };
    const budget: Presupuesto = { idBudget: 9, number: 1 };
    const resumen: BudgetSummary = {
      idBudget: 9,
      numberBudget: 1,
      total: 100,
      pendingBalance: 0,
    };
    const doc: SubmittedDocument = { idSubmittedDocument: 3, delivered: false };
    expect(gestion.idManagement).toBe(1);
    expect(budget.idBudget).toBe(9);
    expect(resumen.total).toBe(100);
    expect(doc.delivered).toBe(false);
  });

  it("keeps hook query-key factories for dashboard / list screens", () => {
    expect(gestionesKeys.page({ page: 0, size: 1 })).toEqual(["gestiones", "page", { page: 0, size: 1 }]);
    expect(presupuestosKeys.page({ page: 0, size: 1 })).toEqual([
      "presupuestos",
      "page",
      { page: 0, size: 1 },
    ]);
    expect(documentosPresentadosKeys.all).toEqual(["documentosPresentados"]);
  });
});
