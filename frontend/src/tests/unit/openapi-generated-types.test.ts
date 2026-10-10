/**
 * #1260 / #1197 P0.5 — high-traffic hooks consume OpenAPI-generated aliases.
 */
import { describe, expect, it } from "vitest";
import type {
  DocumentoPresentado,
  GestionDeEscritura,
  Presupuesto,
  PresupuestoResumen,
} from "@/types/api";
import { gestionesKeys } from "@/hooks/useGestiones";
import { presupuestosKeys } from "@/hooks/usePresupuestos";
import { documentosPresentadosKeys } from "@/hooks/useDocumentosPresentados";

describe("openapi generated types (#1260)", () => {
  it("exports schema aliases used by gestiones, presupuestos and documentos hooks", () => {
    const gestion: GestionDeEscritura = { idManagement: 1, procedureCount: 2 };
    const budget: Presupuesto = { idBudget: 9, number: 1 };
    const resumen: PresupuestoResumen = {
      idBudget: 9,
      numberBudget: 1,
      total: 100,
      pendingBalance: 0,
    };
    const doc: DocumentoPresentado = { idSubmittedDocument: 3, delivered: false };
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
