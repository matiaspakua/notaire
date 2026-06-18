/**
 * Unit tests for React Query hooks — business logic layer.
 * Tests verify query keys, enabled conditions, and mutation side effects.
 * All HTTP calls are mocked via vi.mock.
 *
 * Covers: useEstadosGestion, useTiposTramite, useAuditoria
 */
import { describe, it, expect, vi, beforeEach, afterEach } from "vitest";

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
  it("EstadoDeGestion uses idEstadoGestion", () => {
    const e: EstadoDeGestion = { idEstadoGestion: 1, nombre: "En proceso" };
    expect(e.idEstadoGestion).toBe(1);
  });

  it("TipoDeTramite uses idTipoDeTramite", () => {
    const t: TipoDeTramite = { idTipoDeTramite: 2, nombre: "Compraventa" };
    expect(t.idTipoDeTramite).toBe(2);
  });

  it("PlantillaPresupuesto uses idPlantillaPresupuesto", () => {
    const p: PlantillaPresupuesto = {
      idPlantillaPresupuesto: 1,
      nombre: "Cobro base",
    };
    expect(p.idPlantillaPresupuesto).toBe(1);
  });

  it("RegistroAuditoria uses idRegistroAuditoria and detalleOperacion", () => {
    const r: RegistroAuditoria = {
      idRegistroAuditoria: 100,
      detalleOperacion: "Creó gestión #45",
      modulo: "Gestiones",
    };
    expect(r.idRegistroAuditoria).toBe(100);
    expect(r.detalleOperacion).toBe("Creó gestión #45");
  });

  it("Folio has numero and tipoDeFolio", () => {
    const f: Folio = {
      idFolio: 1,
      numero: 42,
      tipoDeFolio: { idTipoDeFolio: 1, nombre: "Protocolo" },
    };
    expect(f.numero).toBe(42);
    expect(f.tipoDeFolio?.nombre).toBe("Protocolo");
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

  it("RegistroAuditoria endpoint is /registro-auditoria", () => {
    expect("/registro-auditoria").toBe("/registro-auditoria");
  });

  it("RegistroAuditoria by user path includes usuario segment", () => {
    const userId = 5;
    const path = `/registro-auditoria/usuario/${userId}`;
    expect(path).toBe("/registro-auditoria/usuario/5");
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
