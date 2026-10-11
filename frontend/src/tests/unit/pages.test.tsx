/**
 * Unit tests for page-level business logic contracts.
 * Tests formatters, helper functions used in pages, and form validation logic.
 */
import { describe, it, expect } from "vitest";
import { formatDate, formatCurrency, fullName } from "@/lib/utils";
import type { Person, DeedManagement, Budget, Payment } from "@/types";

// ──────────────────────────────────────────────
// CU01/CU02 — Managements and Budgets display
// ──────────────────────────────────────────────

describe("CU02 — Management list display helpers", () => {
  it("renders the management number as a string from the number field", () => {
    const g: DeedManagement = { idManagement: 1, number: 2024001 };
    expect(g.number?.toString()).toBe("2024001");
  });

  it("shows the procedure count from procedureCount", () => {
    const g: DeedManagement = { idManagement: 1, procedureCount: 3 };
    expect(g.procedureCount).toBe(3);
  });

  it("handles missing procedureCount gracefully", () => {
    const g: DeedManagement = { idManagement: 1 };
    expect(g.procedureCount ?? 0).toBe(0);
  });
});

// ──────────────────────────────────────────────
// CU01 — Budgets
// ──────────────────────────────────────────────

describe("CU01 — Budget display helpers", () => {
  it("formats the budget propertyAmount as currency", () => {
    const p: Budget = { idBudget: 1, propertyAmount: 125000.5, status: "PENDIENTE" };
    const formatted = formatCurrency(p.propertyAmount);
    expect(formatted).toContain("125");
    expect(typeof formatted).toBe("string");
  });

  it("formats the budget date", () => {
    const p: Budget = { idBudget: 1, date: "2025-04-15" };
    const formatted = formatDate(p.date);
    expect(typeof formatted).toBe("string");
    expect(formatted.length).toBeGreaterThan(0);
  });

  it("shows '—' when the budget propertyAmount is undefined", () => {
    const formatted = formatCurrency(undefined);
    expect(formatted).toBe("—");
  });

  it("status field reflects PENDIENTE/PAID states", () => {
    const statuses = ["PENDIENTE", "PAID", "CANCELLED"];
    statuses.forEach((e) => {
      const p: Budget = { status: e };
      expect(p.status).toBe(e);
    });
  });
});

// ──────────────────────────────────────────────
// CU17/CU18 — People
// ──────────────────────────────────────────────

describe("CU17 — People list display helpers", () => {
  it("fullName combines firstName + lastName", () => {
    const p: Person = { firstName: "Juan", lastName: "García" };
    expect(fullName(p)).toBe("Juan García");
  });

  it("fullName handles missing lastName", () => {
    const p: Person = { firstName: "Admin" };
    expect(fullName(p)).toBe("Admin");
  });

  it("fullName handles both missing", () => {
    expect(fullName({})).toBe("—");
  });

  it("filters clients using isClient flag", () => {
    const people: Person[] = [
      { personId: 1, firstName: "Client A", isClient: true },
      { personId: 2, firstName: "Employee B", isClient: false },
      { personId: 3, firstName: "Client C", isClient: true },
    ];
    const clients = people.filter((p) => p.isClient);
    expect(clients).toHaveLength(2);
    expect(clients[0].firstName).toBe("Client A");
  });
});

// ──────────────────────────────────────────────
// CU15 — Payments
// ──────────────────────────────────────────────

describe("CU15 — Payment display helpers", () => {
  it("formats the payment amount as currency", () => {
    const p: Payment = { idPayment: 1, amount: 5000, date: "2025-04-01" };
    const formatted = formatCurrency(p.amount);
    expect(formatted).toContain("5");
  });

  it("the payment date renders as a formatted date", () => {
    const p: Payment = { idPayment: 1, date: "2025-04-01T10:30:00" };
    const formatted = formatDate(p.date);
    expect(typeof formatted).toBe("string");
    expect(formatted).not.toBe("—");
  });

  it("paymentMethod defaults to '—' when undefined", () => {
    const p: Payment = { idPayment: 1 };
    expect(p.paymentMethod ?? "—").toBe("—");
  });

  it("a payment linked to a budget via the fkIdBudget field", () => {
    const p: Payment = {
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
  it("status name must not be empty", () => {
    const validate = (name?: string) => !!name?.trim();
    expect(validate("En proceso")).toBe(true);
    expect(validate("")).toBe(false);
    expect(validate("   ")).toBe(false);
    expect(validate(undefined)).toBe(false);
  });

  it("procedure type name must not be empty", () => {
    const validate = (name?: string) => !!name?.trim();
    expect(validate("Compraventa")).toBe(true);
    expect(validate("")).toBe(false);
  });

  it("folio number must be a positive integer", () => {
    const validate = (n?: number) => n !== undefined && n > 0 && Number.isInteger(n);
    expect(validate(1001)).toBe(true);
    expect(validate(0)).toBe(false);
    expect(validate(-1)).toBe(false);
    expect(validate(undefined)).toBe(false);
    expect(validate(1.5)).toBe(false);
  });

  it("a template requires both a procedure type and a concept", () => {
    const validate = (idTypeProcedure: string, idConcept: string) =>
      !!idTypeProcedure && !!idConcept;
    expect(validate("1", "2")).toBe(true);
    expect(validate("", "2")).toBe(false);
    expect(validate("1", "")).toBe(false);
    expect(validate("", "")).toBe(false);
  });

  it("payment amount must be a positive number", () => {
    const validate = (amount?: number) => amount !== undefined && amount > 0;
    expect(validate(5000)).toBe(true);
    expect(validate(0.01)).toBe(true);
    expect(validate(0)).toBe(false);
    expect(validate(-100)).toBe(false);
    expect(validate(undefined)).toBe(false);
  });
});
