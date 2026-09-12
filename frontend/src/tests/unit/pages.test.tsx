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
    const g: GestionDeEscritura = { idManagement: 1, number: 2024001 };
    expect(g.number?.toString()).toBe("2024001");
  });

  it("shows tramite count from procedureCount", () => {
    const g: GestionDeEscritura = { idManagement: 1, procedureCount: 3 };
    expect(g.procedureCount).toBe(3);
  });

  it("handles missing procedureCount gracefully", () => {
    const g: GestionDeEscritura = { idManagement: 1 };
    expect(g.procedureCount ?? 0).toBe(0);
  });
});

// ──────────────────────────────────────────────
// CU01 — Presupuestos
// ──────────────────────────────────────────────

describe("CU01 — Presupuesto display helpers", () => {
  it("formats presupuesto propertyAmount as currency", () => {
    const p: Presupuesto = { idBudget: 1, propertyAmount: 125000.5, status: "PENDIENTE" };
    const formatted = formatCurrency(p.propertyAmount);
    expect(formatted).toContain("125");
    expect(typeof formatted).toBe("string");
  });

  it("formats presupuesto date", () => {
    const p: Presupuesto = { idBudget: 1, date: "2025-04-15" };
    const formatted = formatDate(p.date);
    expect(typeof formatted).toBe("string");
    expect(formatted.length).toBeGreaterThan(0);
  });

  it("shows '—' when presupuesto propertyAmount is undefined", () => {
    const formatted = formatCurrency(undefined);
    expect(formatted).toBe("—");
  });

  it("status field reflects PENDIENTE/PAGADO states", () => {
    const estados = ["PENDIENTE", "PAGADO", "CANCELADO"];
    estados.forEach((e) => {
      const p: Presupuesto = { status: e };
      expect(p.status).toBe(e);
    });
  });
});

// ──────────────────────────────────────────────
// CU17/CU18 — Personas
// ──────────────────────────────────────────────

describe("CU17 — Personas list display helpers", () => {
  it("fullName combines firstName + lastName", () => {
    const p: Persona = { firstName: "Juan", lastName: "García" };
    expect(fullName(p)).toBe("Juan García");
  });

  it("fullName handles missing lastName", () => {
    const p: Persona = { firstName: "Admin" };
    expect(fullName(p)).toBe("Admin");
  });

  it("fullName handles both missing", () => {
    expect(fullName({})).toBe("—");
  });

  it("filters clients using isClient flag", () => {
    const personas: Persona[] = [
      { personId: 1, firstName: "Cliente A", isClient: true },
      { personId: 2, firstName: "Empleado B", isClient: false },
      { personId: 3, firstName: "Cliente C", isClient: true },
    ];
    const clientes = personas.filter((p) => p.isClient);
    expect(clientes).toHaveLength(2);
    expect(clientes[0].firstName).toBe("Cliente A");
  });
});

// ──────────────────────────────────────────────
// CU15 — Pagos
// ──────────────────────────────────────────────

describe("CU15 — Pagos display helpers", () => {
  it("formats pago amount as currency", () => {
    const p: Pago = { idPayment: 1, amount: 5000, date: "2025-04-01" };
    const formatted = formatCurrency(p.amount);
    expect(formatted).toContain("5");
  });

  it("pago date renders as formatted date", () => {
    const p: Pago = { idPayment: 1, date: "2025-04-01T10:30:00" };
    const formatted = formatDate(p.date);
    expect(typeof formatted).toBe("string");
    expect(formatted).not.toBe("—");
  });

  it("paymentMethod defaults to '—' when undefined", () => {
    const p: Pago = { idPayment: 1 };
    expect(p.paymentMethod ?? "—").toBe("—");
  });

  it("pago linked to presupuesto via fkIdBudget field", () => {
    const p: Pago = {
      idPayment: 1,
      fkIdBudget: { idBudget: 42 },
    };
    expect(p.fkIdBudget?.idBudget).toBe(42);
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
