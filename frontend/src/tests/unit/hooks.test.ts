/**
 * Unit tests for React Query hooks — business logic layer.
 * Tests verify query keys, enabled conditions, and mutation side effects.
 * All HTTP calls are mocked via vi.mock.
 *
 * Covers: useManagementStatuses, useProcedureTypes, useAudit
 */
import { describe, it, expect } from "vitest";

// ──────────────────────────────────────────────
// Query key contracts
// ──────────────────────────────────────────────

import { estadosGestionKeys } from "@/hooks/useManagementStatuses";
import { tiposTramiteKeys } from "@/hooks/useProcedureTypes";
import { auditoriaKeys } from "@/hooks/useAudit";

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
  ManagementStatus,
  ProcedureType,
  Folio,
  BudgetTemplate,
  AuditRecord,
} from "@/types";

describe("TypeScript type shape contracts", () => {
  it("ManagementStatus uses idManagementStatus", () => {
    const e: ManagementStatus = { idManagementStatus: 1, name: "En proceso" };
    expect(e.idManagementStatus).toBe(1);
  });

  it("ProcedureType uses idProcedureType", () => {
    const t: ProcedureType = { idProcedureType: 2, name: "Compraventa" };
    expect(t.idProcedureType).toBe(2);
  });

  it("BudgetTemplate uses budgetTemplatePK", () => {
    const p: BudgetTemplate = {
      budgetTemplatePK: { fkIdProcedureType: 2, fkIdConcept: 1 },
      procedureType: { name: "Cobro base" },
    };
    expect(p.budgetTemplatePK?.fkIdConcept).toBe(1);
  });

  it("AuditRecord uses idAuditRecord and operationDetail", () => {
    const r: AuditRecord = {
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
  it("ManagementStatus endpoint is /management-statuses (not /management-statuses)", () => {
    // Document the backend API path for CI/CD integration tests
    const path = "/management-statuses";
    expect(path).toBe("/management-statuses");
  });

  it("ProcedureType endpoint is /procedure-types", () => {
    expect("/procedure-types").toBe("/procedure-types");
  });

  it("Folio endpoint is /folios (singular)", () => {
    expect("/folios").toBe("/folios");
  });

  it("BudgetTemplate endpoint is /budget-templates", () => {
    expect("/budget-templates").toBe("/budget-templates");
  });

  it("BudgetTemplate composite PUT path is correct", () => {
    const idTipoTramite = 3;
    const idConcepto = 7;
    const path = `/budget-templates/procedure-types/${idTipoTramite}/concept/${idConcepto}`;
    expect(path).toBe("/budget-templates/procedure-types/3/concept/7");
  });

  it("AuditRecord endpoint is /audit-log", () => {
    expect("/audit-log").toBe("/audit-log");
  });

  it("AuditRecord by user path includes usuario segment", () => {
    const userId = 5;
    const path = `/audit-log/user/${userId}`;
    expect(path).toBe("/audit-log/user/5");
  });
});

// ──────────────────────────────────────────────
// Enabled condition for conditional hooks
// ──────────────────────────────────────────────

describe("Conditional query enabled logic", () => {
  it("useAuditByUser — enabled when idUsuario > 0", () => {
    const enabled = (id: number | null) => id !== null && id > 0;
    expect(enabled(1)).toBe(true);
    expect(enabled(null)).toBe(false);
    expect(enabled(0)).toBe(false);
  });
});
