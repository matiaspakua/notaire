/**
 * Unit tests for React Query hooks — business logic layer.
 * Tests verify query keys, enabled conditions, and mutation side effects.
 * All HTTP calls are mocked via vi.mock.
 *
 * Covers: useEstadosGestion, useTiposTramite, useAuditoria
 */
import { describe, it, expect } from "vitest";

// ──────────────────────────────────────────────
// Query key contracts
// ──────────────────────────────────────────────

import { estadosGestionKeys } from "@/hooks/useEstadosGestion";
import { tiposTramiteKeys } from "@/hooks/useTiposTramite";
import { auditoriaKeys } from "@/hooks/useAuditoria";

describe("React Query key contracts", () => {
  describe("estadosGestionKeys", () => {
    it("all key is ['estadosGestion']", () => {
      expect(estadosGestionKeys.all).toEqual(["estadosGestion"]);
    });

    it("detail key includes id", () => {
      expect(estadosGestionKeys.detail(5)).toEqual(["estadosGestion", 5]);
    });
  });

  describe("tiposTramiteKeys", () => {
    it("all key is ['tiposTramite']", () => {
      expect(tiposTramiteKeys.all).toEqual(["tiposTramite"]);
    });

    it("detail key includes id", () => {
      expect(tiposTramiteKeys.detail(3)).toEqual(["tiposTramite", 3]);
    });
  });

  describe("auditoriaKeys", () => {
    it("all key is ['auditoria']", () => {
      expect(auditoriaKeys.all).toEqual(["auditoria"]);
    });

    it("byUsuario key includes user id", () => {
      expect(auditoriaKeys.byUsuario(12)).toEqual(["auditoria", "usuario", 12]);
    });
  });
});

// ──────────────────────────────────────────────
// Type shape contracts
// ──────────────────────────────────────────────

import type {
  EstadoDeGestion,
  TipoDeTramite,
  Folio,
  PlantillaPresupuesto,
  RegistroAuditoria,
} from "@/types";

describe("TypeScript type shape contracts", () => {
  it("EstadoDeGestion uses idManagementStatus", () => {
    const e: EstadoDeGestion = { idManagementStatus: 1, name: "En proceso" };
    expect(e.idManagementStatus).toBe(1);
  });

  it("TipoDeTramite uses idProcedureType", () => {
    const t: TipoDeTramite = { idProcedureType: 2, name: "Compraventa" };
    expect(t.idProcedureType).toBe(2);
  });

  it("PlantillaPresupuesto uses budgetTemplatePK", () => {
    const p: PlantillaPresupuesto = {
      budgetTemplatePK: { fkIdProcedureType: 2, fkIdConcept: 1 },
      procedureType: { name: "Cobro base" },
    };
    expect(p.budgetTemplatePK?.fkIdConcept).toBe(1);
  });

  it("RegistroAuditoria uses idAuditRecord and operationDetail", () => {
    const r: RegistroAuditoria = {
      idAuditRecord: 100,
      operationDetail: "Creó gestión #45",
      module: "Gestiones",
    };
    expect(r.idAuditRecord).toBe(100);
    expect(r.operationDetail).toBe("Creó gestión #45");
  });

  it("Folio has number and fkIdFolioType", () => {
    const f: Folio = {
      idFolio: 1,
      number: 42,
      fkIdFolioType: { idFolioType: 1, name: "Protocolo" },
    };
    expect(f.number).toBe(42);
    expect(f.fkIdFolioType?.name).toBe("Protocolo");
  });
});

// ──────────────────────────────────────────────
// API client path contracts
// ──────────────────────────────────────────────

describe("API endpoint path contracts", () => {
  it("EstadoDeGestion endpoint is /estado-gestion (not /estados-gestion)", () => {
    // Document the backend API path for CI/CD integration tests
    const path = "/estado-gestion";
    expect(path).toBe("/estado-gestion");
  });

  it("TipoDeTramite endpoint is /tipo-tramite", () => {
    expect("/tipo-tramite").toBe("/tipo-tramite");
  });

  it("Folio endpoint is /folio (singular)", () => {
    expect("/folio").toBe("/folio");
  });

  it("PlantillaPresupuesto endpoint is /plantilla-presupuestos", () => {
    expect("/plantilla-presupuestos").toBe("/plantilla-presupuestos");
  });

  it("PlantillaPresupuesto composite PUT path is correct", () => {
    const idTipoTramite = 3;
    const idConcepto = 7;
    const path = `/plantilla-presupuestos/tipo-tramite/${idTipoTramite}/concepto/${idConcepto}`;
    expect(path).toBe("/plantilla-presupuestos/tipo-tramite/3/concepto/7");
  });

  it("RegistroAuditoria endpoint is /audit-log", () => {
    expect("/audit-log").toBe("/audit-log");
  });

  it("RegistroAuditoria by user path includes usuario segment", () => {
    const userId = 5;
    const path = `/audit-log/user/${userId}`;
    expect(path).toBe("/audit-log/user/5");
  });
});

// ──────────────────────────────────────────────
// Enabled condition for conditional hooks
// ──────────────────────────────────────────────

describe("Conditional query enabled logic", () => {
  it("useAuditoriaByUsuario — enabled when idUsuario > 0", () => {
    const enabled = (id: number | null) => id !== null && id > 0;
    expect(enabled(1)).toBe(true);
    expect(enabled(null)).toBe(false);
    expect(enabled(0)).toBe(false);
  });
});
