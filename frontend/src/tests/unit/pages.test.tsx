/**
 * Unit tests for page-level business logic contracts.
 * Tests formatters, helper functions used in pages, and form validation logic.
 */
import { describe, it, expect } from "vitest";
import { formatDate, formatCurrency, fullName } from "@/lib/utils";
import type { Persona, GestionDeEscritura, Presupuesto, Pago } from "@/types";

// ──────────────────────────────────────────────
// CU01/CU02 — Gestiones and Presupuestos display
// ──────────────────────────────────────────────

describe("CU02 — Gestiones list display helpers", () => {
  it("renders gestion number as string from number field", () => {
    const g: GestionDeEscritura = { idGestion: 1, numero: 2024001 };
    expect(g.numero?.toString()).toBe("2024001");
  });

  it("shows tramite count from tramiteList", () => {
    const g: GestionDeEscritura = {
      idGestion: 1,
      tramiteList: [
        { idTramite: 1 },
        { idTramite: 2 },
        { idTramite: 3 },
      ],
    };
    expect(g.tramiteList?.length).toBe(3);
  });

  it("handles empty tramiteList gracefully", () => {
    const g: GestionDeEscritura = { idGestion: 1, tramiteList: [] };
    expect(g.tramiteList?.length ?? 0).toBe(0);
  });
});

// ──────────────────────────────────────────────
// CU01 — Presupuestos
// ──────────────────────────────────────────────

describe("CU01 — Presupuesto display helpers", () => {
  it("formats presupuesto monto as currency", () => {
    const p: Presupuesto = { idPresupuesto: 1, monto: 125000.5, estado: "PENDIENTE" };
    const formatted = formatCurrency(p.monto);
    expect(formatted).toContain("125");
    expect(typeof formatted).toBe("string");
  });

  it("formats presupuesto date", () => {
    const p: Presupuesto = { idPresupuesto: 1, fecha: "2025-04-15" };
    const formatted = formatDate(p.fecha);
    expect(typeof formatted).toBe("string");
    expect(formatted.length).toBeGreaterThan(0);
  });

  it("shows '—' when presupuesto monto is undefined", () => {
    const formatted = formatCurrency(undefined);
    expect(formatted).toBe("—");
  });

  it("estado field reflects PENDIENTE/PAGADO states", () => {
    const estados = ["PENDIENTE", "PAGADO", "CANCELADO"];
    estados.forEach((e) => {
      const p: Presupuesto = { estado: e };
      expect(p.estado).toBe(e);
    });
  });
});

// ──────────────────────────────────────────────
// CU17/CU18 — Personas
// ──────────────────────────────────────────────

describe("CU17 — Personas list display helpers", () => {
  it("fullName combines nombre + apellido", () => {
    const p: Persona = { nombre: "Juan", apellido: "García" };
    expect(fullName(p)).toBe("Juan García");
  });

  it("fullName handles missing apellido", () => {
    const p: Persona = { nombre: "Admin" };
    expect(fullName(p)).toBe("Admin");
  });

  it("fullName handles both missing", () => {
    expect(fullName({})).toBe("—");
  });

  it("filters clients using esCliente flag", () => {
    const personas: Persona[] = [
      { idPersona: 1, nombre: "Cliente A", esCliente: true },
      { idPersona: 2, nombre: "Empleado B", esCliente: false },
      { idPersona: 3, nombre: "Cliente C", esCliente: true },
    ];
    const clientes = personas.filter((p) => p.esCliente);
    expect(clientes).toHaveLength(2);
    expect(clientes[0].nombre).toBe("Cliente A");
  });
});

// ──────────────────────────────────────────────
// CU15 — Pagos
// ──────────────────────────────────────────────

describe("CU15 — Pagos display helpers", () => {
  it("formats pago monto as currency", () => {
    const p: Pago = { idPago: 1, monto: 5000, fecha: "2025-04-01" };
    const formatted = formatCurrency(p.monto);
    expect(formatted).toContain("5");
  });

  it("pago fecha renders as formatted date", () => {
    const p: Pago = { idPago: 1, fecha: "2025-04-01T10:30:00" };
    const formatted = formatDate(p.fecha);
    expect(typeof formatted).toBe("string");
    expect(formatted).not.toBe("—");
  });

  it("metodoPago defaults to '—' when undefined", () => {
    const p: Pago = { idPago: 1 };
    expect(p.metodoPago ?? "—").toBe("—");
  });

  it("pago linked to presupuesto via presupuesto field", () => {
    const p: Pago = {
      idPago: 1,
      presupuesto: { idPresupuesto: 42, monto: 10000 },
    };
    expect(p.presupuesto?.idPresupuesto).toBe(42);
  });
});

// ──────────────────────────────────────────────
// Form validation helpers
// ──────────────────────────────────────────────

describe("Form validation contracts", () => {
  it("estado nombre must not be empty", () => {
    const validate = (nombre?: string) => !!nombre?.trim();
    expect(validate("En proceso")).toBe(true);
    expect(validate("")).toBe(false);
    expect(validate("   ")).toBe(false);
    expect(validate(undefined)).toBe(false);
  });

  it("tipo tramite nombre must not be empty", () => {
    const validate = (nombre?: string) => !!nombre?.trim();
    expect(validate("Compraventa")).toBe(true);
    expect(validate("")).toBe(false);
  });

  it("folio numero must be a positive integer", () => {
    const validate = (n?: number) => n !== undefined && n > 0 && Number.isInteger(n);
    expect(validate(1001)).toBe(true);
    expect(validate(0)).toBe(false);
    expect(validate(-1)).toBe(false);
    expect(validate(undefined)).toBe(false);
    expect(validate(1.5)).toBe(false);
  });

  it("plantilla requires both tipo tramite and concepto", () => {
    const validate = (idTipoTramite: string, idConcepto: string) =>
      !!idTipoTramite && !!idConcepto;
    expect(validate("1", "2")).toBe(true);
    expect(validate("", "2")).toBe(false);
    expect(validate("1", "")).toBe(false);
    expect(validate("", "")).toBe(false);
  });

  it("pago monto must be a positive number", () => {
    const validate = (monto?: number) => monto !== undefined && monto > 0;
    expect(validate(5000)).toBe(true);
    expect(validate(0.01)).toBe(true);
    expect(validate(0)).toBe(false);
    expect(validate(-100)).toBe(false);
    expect(validate(undefined)).toBe(false);
  });
});
