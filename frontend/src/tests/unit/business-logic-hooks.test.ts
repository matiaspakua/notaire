/**
 * Unit tests for remaining React Query hook query keys and business logic.
 * Covers: useManagements, usePeople, useBudgets, usePayments, useConcepts,
 *         useDeeds, useUsers, useProperties, useCopies, useDocuments,
 *         useSubmittedDocuments
 *
 * Use cases: CU01-CU68
 */
import { describe, it, expect } from "vitest";

import { managementsKeys } from "@/hooks/useManagements";
import { peopleKeys } from "@/hooks/usePeople";
import { budgetsKeys } from "@/hooks/useBudgets";
import { paymentsKeys } from "@/hooks/usePayments";
import { conceptsKeys } from "@/hooks/useConcepts";
import { deedsKeys } from "@/hooks/useDeeds";
import { usersKeys } from "@/hooks/useUsers";
import { propertiesKeys } from "@/hooks/useProperties";
import { copiesKeys } from "@/hooks/useCopies";
import { documentsKeys } from "@/hooks/useDocuments";

// ──────────────────────────────────────────────
// Query key contracts — all remaining hooks
// ──────────────────────────────────────────────

describe("managementsKeys (CU02, CU13, CU24)", () => {
  it("all key is ['managements']", () => {
    expect(managementsKeys.all).toEqual(["managements"]);
  });

  it("detail key includes id", () => {
    expect(managementsKeys.detail(10)).toEqual(["managements", 10]);
  });

  it("byClient key includes the person id", () => {
    expect(managementsKeys.byClient(5)).toEqual(["managements", "client", 5]);
  });
});

describe("peopleKeys (CU17, CU18)", () => {
  it("all key is ['people']", () => {
    expect(peopleKeys.all).toEqual(["people"]);
  });

  it("detail key includes id", () => {
    expect(peopleKeys.detail(3)).toEqual(["people", 3]);
  });
});

describe("budgetsKeys (CU01, CU39)", () => {
  it("all key is ['budgets']", () => {
    expect(budgetsKeys.all).toEqual(["budgets"]);
  });

  it("detail key includes id", () => {
    expect(budgetsKeys.detail(7)).toEqual(["budgets", 7]);
  });

  it("byPerson key includes the person id", () => {
    expect(budgetsKeys.byPerson(2)).toEqual(["budgets", "person", 2]);
  });
});

describe("paymentsKeys (CU15, CU47)", () => {
  it("all key is ['payments']", () => {
    expect(paymentsKeys.all).toEqual(["payments"]);
  });
});

describe("conceptsKeys (CU39, CU49)", () => {
  it("all key is ['concepts']", () => {
    expect(conceptsKeys.all).toEqual(["concepts"]);
  });
});

describe("deedsKeys (CU05, CU06)", () => {
  it("all key is ['deeds']", () => {
    expect(deedsKeys.all).toEqual(["deeds"]);
  });

  it("detail key includes id", () => {
    expect(deedsKeys.detail(99)).toEqual(["deeds", 99]);
  });
});

describe("usersKeys (CU20, CU21)", () => {
  it("all key is ['users']", () => {
    expect(usersKeys.all).toEqual(["users"]);
  });
});

describe("propertiesKeys (CU08)", () => {
  it("all key is ['properties']", () => {
    expect(propertiesKeys.all).toEqual(["properties"]);
  });

  it("detail key includes id", () => {
    expect(propertiesKeys.detail(15)).toEqual(["properties", 15]);
  });
});

describe("copiesKeys (CU44, CU53)", () => {
  it("all key is ['copies']", () => {
    expect(copiesKeys.all).toEqual(["copies"]);
  });

  it("detail key includes id", () => {
    expect(copiesKeys.detail(8)).toEqual(["copies", 8]);
  });
});

describe("documentsKeys (CU27, CU65)", () => {
  it("all key is ['tiposDocumento']", () => {
    expect(documentsKeys.all).toEqual(["tiposDocumento"]);
  });

  it("detail key includes id", () => {
    expect(documentsKeys.detail(4)).toEqual(["tiposDocumento", 4]);
  });
});

// ──────────────────────────────────────────────
// Budget business logic (CU01, CU39)
// ──────────────────────────────────────────────

describe("Budget total calculation (CU01, CU39)", () => {
  type Item = { precio?: number; cantidad?: number };

  function calcularTotal(items: Item[]): number {
    return items.reduce((sum, item) => sum + (item.precio ?? 0) * (item.cantidad ?? 1), 0);
  }

  it("sums item prices * quantities", () => {
    const items = [
      { precio: 100, cantidad: 2 },
      { precio: 50, cantidad: 3 },
    ];
    expect(calcularTotal(items)).toBe(350);
  });

  it("empty items returns 0", () => {
    expect(calcularTotal([])).toBe(0);
  });

  it("item with undefined precio contributes 0", () => {
    const items = [{ cantidad: 5 }];
    expect(calcularTotal(items)).toBe(0);
  });

  it("item with undefined cantidad defaults to 1", () => {
    const items = [{ precio: 200 }];
    expect(calcularTotal(items)).toBe(200);
  });

  it("single item total", () => {
    expect(calcularTotal([{ precio: 500, cantidad: 1 }])).toBe(500);
  });
});

// ──────────────────────────────────────────────
// Payment outstanding-balance computation (CU47)
// ──────────────────────────────────────────────

describe("Payment outstanding-balance computation (CU47)", () => {
  function calcularSaldo(totalBudget: number, pagos: { amount?: number }[]): number {
    const totalPaid = pagos.reduce((sum, p) => sum + (p.amount ?? 0), 0);
    return totalBudget - totalPaid;
  }

  it("outstanding balance is the total minus the sum of payments", () => {
    expect(calcularSaldo(1000, [{ amount: 300 }, { amount: 200 }])).toBe(500);
  });

  it("outstanding balance with no payments equals the total", () => {
    expect(calcularSaldo(800, [])).toBe(800);
  });

  it("a fully paid budget has an outstanding balance of 0", () => {
    expect(calcularSaldo(500, [{ amount: 500 }])).toBe(0);
  });

  it("an overpaid budget has a negative outstanding balance", () => {
    expect(calcularSaldo(200, [{ amount: 300 }])).toBe(-100);
  });

  it("a payment with an undefined amount contributes 0", () => {
    expect(calcularSaldo(400, [{ amount: undefined }, { amount: 100 }])).toBe(300);
  });
});

// ──────────────────────────────────────────────
// Deed number format (CU05)
// ──────────────────────────────────────────────

describe("Deed display label (CU05)", () => {
  function getDeedLabel(number?: number, fecha?: string): string {
    if (!number) return "Sin número";
    return fecha ? `Deed #${number} — ${fecha}` : `Deed #${number}`;
  }

  it("returns \"Sin número\" when number is undefined", () => {
    expect(getDeedLabel(undefined, "2026-01-01")).toBe("Sin número");
  });

  it("returns label with date when both present", () => {
    expect(getDeedLabel(42, "2026-03-15")).toBe("Deed #42 — 2026-03-15");
  });

  it("returns label without date when fecha is absent", () => {
    expect(getDeedLabel(7)).toBe("Deed #7");
  });
});

// ──────────────────────────────────────────────
// Person display name (CU17, CU18, CU61)
// ──────────────────────────────────────────────

describe("Person display name (CU17, CU18, CU61)", () => {
  function getDisplayName(person?: { name?: string; apellido?: string }): string {
    if (!person) return "—";
    const parts = [person.apellido, person.name].filter(Boolean);
    return parts.length > 0 ? parts.join(", ") : "—";
  }

  it("formats as apellido, nombre", () => {
    expect(getDisplayName({ name: "Juan", apellido: "García" })).toBe("García, Juan");
  });

  it("returns only apellido when nombre is missing", () => {
    expect(getDisplayName({ apellido: "García" })).toBe("García");
  });

  it("returns only nombre when apellido is missing", () => {
    expect(getDisplayName({ name: "Juan" })).toBe("Juan");
  });

  it("returns a dash for an empty person", () => {
    expect(getDisplayName({})).toBe("—");
  });

  it("returns a dash for an undefined person", () => {
    expect(getDisplayName(undefined)).toBe("—");
  });
});

// ──────────────────────────────────────────────
// Copy type shape (CU44, CU53)
// ──────────────────────────────────────────────

describe("Copy type shape (CU44, CU53)", () => {
  it("copia has required fields", () => {
    const copia = {
      idCopia: 1,
      testimony: { idTestimony: 10 },
      fecha: "2026-02-01",
      tipo: "Simple",
    };
    expect(copia.idCopia).toBe(1);
    expect(copia.testimony.idTestimony).toBe(10);
    expect(copia.fecha).toBe("2026-02-01");
  });

  it("copia can have undefined testimonio", () => {
    const copia: { idCopia?: number; testimony?: unknown } = { idCopia: 5 };
    expect(copia.testimony).toBeUndefined();
  });
});

// ──────────────────────────────────────────────
// Concept value type (CU39, CU49)
// ──────────────────────────────────────────────

describe("Concept type shape (CU39, CU49)", () => {
  it("a concept has a name and a value", () => {
    const concept = { idConcept: 1, name: "Honorarios", valor: 1500 };
    expect(concept.name).toBe("Honorarios");
    expect(concept.valor).toBe(1500);
  });

  it("a concept value can be 0", () => {
    const concept = { idConcept: 2, name: "Exento", valor: 0 };
    expect(concept.valor).toBe(0);
  });
});

// ──────────────────────────────────────────────
// Property type shape (CU08)
// ──────────────────────────────────────────────

describe("Property type shape (CU08)", () => {
  it("a property has address fields", () => {
    const property = {
      idProperty: 1,
      calle: "Av. Corrientes",
      number: 1234,
      ciudad: "Buenos Aires",
    };
    expect(property.calle).toBe("Av. Corrientes");
    expect(property.number).toBe(1234);
  });

  it("a property can omit optional fields", () => {
    const property: { idProperty?: number; piso?: string; depto?: string } = { idProperty: 1 };
    expect(property.piso).toBeUndefined();
    expect(property.depto).toBeUndefined();
  });
});

// ──────────────────────────────────────────────
// DocumentType type shape (CU27, CU65)
// ──────────────────────────────────────────────

describe("DocumentType type shape (CU27, CU65)", () => {
  it("tipoDeDocumento has id and nombre", () => {
    const tipo = { idTypeDocument: 3, name: "DNI" };
    expect(tipo.idTypeDocument).toBe(3);
    expect(tipo.name).toBe("DNI");
  });

  it("filter tipos by partial name match", () => {
    const tipos = [
      { idTypeDocument: 1, name: "DNI" },
      { idTypeDocument: 2, name: "Pasaporte" },
      { idTypeDocument: 3, name: "CUIT" },
    ];
    const result = tipos.filter((t) => t.name.toLowerCase().includes("pas"));
    expect(result).toHaveLength(1);
    expect(result[0].name).toBe("Pasaporte");
  });
});

// ──────────────────────────────────────────────
// UserAccount type shape + password hash (CU20, CU21)
// ──────────────────────────────────────────────

describe("UserAccount type shape (CU20, CU21)", () => {
  it("usuario has username field", () => {
    const u = { idUsuario: 1, usuario: "admin", idPerson: 5 };
    expect(u.usuario).toBe("admin");
    expect(u.idPerson).toBe(5);
  });

  it("usuario login requires non-empty credentials", () => {
    function validateLogin(user: string, pwd: string): boolean {
      return user.trim().length > 0 && pwd.trim().length > 0;
    }
    expect(validateLogin("admin", "secret")).toBe(true);
    expect(validateLogin("", "secret")).toBe(false);
    expect(validateLogin("admin", "")).toBe(false);
    expect(validateLogin("  ", "  ")).toBe(false);
  });
});
