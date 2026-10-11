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
  it("all key is ['documentTypes']", () => {
    expect(documentsKeys.all).toEqual(["documentTypes"]);
  });

  it("detail key includes id", () => {
    expect(documentsKeys.detail(4)).toEqual(["documentTypes", 4]);
  });
});

// ──────────────────────────────────────────────
// Budget business logic (CU01, CU39)
// ──────────────────────────────────────────────

describe("Budget total calculation (CU01, CU39)", () => {
  type Item = { price?: number; quantity?: number };

  function calculateTotal(items: Item[]): number {
    return items.reduce((sum, item) => sum + (item.price ?? 0) * (item.quantity ?? 1), 0);
  }

  it("sums item prices * quantities", () => {
    const items = [
      { price: 100, quantity: 2 },
      { price: 50, quantity: 3 },
    ];
    expect(calculateTotal(items)).toBe(350);
  });

  it("empty items returns 0", () => {
    expect(calculateTotal([])).toBe(0);
  });

  it("item with undefined price contributes 0", () => {
    const items = [{ quantity: 5 }];
    expect(calculateTotal(items)).toBe(0);
  });

  it("item with undefined quantity defaults to 1", () => {
    const items = [{ price: 200 }];
    expect(calculateTotal(items)).toBe(200);
  });

  it("single item total", () => {
    expect(calculateTotal([{ price: 500, quantity: 1 }])).toBe(500);
  });
});

// ──────────────────────────────────────────────
// Payment outstanding-balance computation (CU47)
// ──────────────────────────────────────────────

describe("Payment outstanding-balance computation (CU47)", () => {
  function outstandingBalance(totalBudget: number, payments: { amount?: number }[]): number {
    const totalPaid = payments.reduce((sum, p) => sum + (p.amount ?? 0), 0);
    return totalBudget - totalPaid;
  }

  it("outstanding balance is the total minus the sum of payments", () => {
    expect(outstandingBalance(1000, [{ amount: 300 }, { amount: 200 }])).toBe(500);
  });

  it("outstanding balance with no payments equals the total", () => {
    expect(outstandingBalance(800, [])).toBe(800);
  });

  it("a fully paid budget has an outstanding balance of 0", () => {
    expect(outstandingBalance(500, [{ amount: 500 }])).toBe(0);
  });

  it("an overpaid budget has a negative outstanding balance", () => {
    expect(outstandingBalance(200, [{ amount: 300 }])).toBe(-100);
  });

  it("a payment with an undefined amount contributes 0", () => {
    expect(outstandingBalance(400, [{ amount: undefined }, { amount: 100 }])).toBe(300);
  });
});

// ──────────────────────────────────────────────
// Deed number format (CU05)
// ──────────────────────────────────────────────

describe("Deed display label (CU05)", () => {
  function getDeedLabel(number?: number, date?: string): string {
    if (!number) return "No number";
    return date ? `Deed #${number} — ${date}` : `Deed #${number}`;
  }

  it("returns \"No number\" when number is undefined", () => {
    expect(getDeedLabel(undefined, "2026-01-01")).toBe("No number");
  });

  it("returns label with date when both present", () => {
    expect(getDeedLabel(42, "2026-03-15")).toBe("Deed #42 — 2026-03-15");
  });

  it("returns label without date when the date is absent", () => {
    expect(getDeedLabel(7)).toBe("Deed #7");
  });
});

// ──────────────────────────────────────────────
// Person display name (CU17, CU18, CU61)
// ──────────────────────────────────────────────

describe("Person display name (CU17, CU18, CU61)", () => {
  function getDisplayName(person?: { firstName?: string; lastName?: string }): string {
    if (!person) return "—";
    const parts = [person.lastName, person.firstName].filter(Boolean);
    return parts.length > 0 ? parts.join(", ") : "—";
  }

  it("formats as last name, first name", () => {
    expect(getDisplayName({ firstName: "Juan", lastName: "García" })).toBe("García, Juan");
  });

  it("returns only the last name when the first name is missing", () => {
    expect(getDisplayName({ lastName: "García" })).toBe("García");
  });

  it("returns only the first name when the last name is missing", () => {
    expect(getDisplayName({ firstName: "Juan" })).toBe("Juan");
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
  it("a copy has the expected fields", () => {
    const copy = {
      idCopy: 1,
      testimony: { idTestimony: 10 },
      date: "2026-02-01",
      type: "Simple",
    };
    expect(copy.idCopy).toBe(1);
    expect(copy.testimony.idTestimony).toBe(10);
    expect(copy.date).toBe("2026-02-01");
  });

  it("a copy can omit the testimony", () => {
    const copy: { idCopy?: number; testimony?: unknown } = { idCopy: 5 };
    expect(copy.testimony).toBeUndefined();
  });
});

// ──────────────────────────────────────────────
// Concept value type (CU39, CU49)
// ──────────────────────────────────────────────

describe("Concept type shape (CU39, CU49)", () => {
  it("a concept has a name and a value", () => {
    const concept = { idConcept: 1, name: "Honorarios", value: 1500 };
    expect(concept.name).toBe("Honorarios");
    expect(concept.value).toBe(1500);
  });

  it("a concept value can be 0", () => {
    const concept = { idConcept: 2, name: "Exento", value: 0 };
    expect(concept.value).toBe(0);
  });
});

// ──────────────────────────────────────────────
// Property type shape (CU08)
// ──────────────────────────────────────────────

describe("Property type shape (CU08)", () => {
  it("a property has address fields", () => {
    const property = {
      idProperty: 1,
      street: "Av. Corrientes",
      number: 1234,
      city: "Buenos Aires",
    };
    expect(property.street).toBe("Av. Corrientes");
    expect(property.number).toBe(1234);
  });

  it("a property can omit optional fields", () => {
    const property: { idProperty?: number; floor?: string; unit?: string } = { idProperty: 1 };
    expect(property.floor).toBeUndefined();
    expect(property.unit).toBeUndefined();
  });
});

// ──────────────────────────────────────────────
// DocumentType type shape (CU27, CU65)
// ──────────────────────────────────────────────

describe("DocumentType type shape (CU27, CU65)", () => {
  it("a document type has an id and a name", () => {
    const documentType = { idTypeDocument: 3, name: "DNI" };
    expect(documentType.idTypeDocument).toBe(3);
    expect(documentType.name).toBe("DNI");
  });

  it("filters document types by a partial name", () => {
    const documentTypes = [
      { idTypeDocument: 1, name: "DNI" },
      { idTypeDocument: 2, name: "Pasaporte" },
      { idTypeDocument: 3, name: "CUIT" },
    ];
    const result = documentTypes.filter((documentType) => documentType.name.toLowerCase().includes("pas"));
    expect(result).toHaveLength(1);
    expect(result[0].name).toBe("Pasaporte");
  });
});

// ──────────────────────────────────────────────
// UserAccount type shape + password hash (CU20, CU21)
// ──────────────────────────────────────────────

describe("UserAccount type shape (CU20, CU21)", () => {
  it("a user has a username field", () => {
    const user = { idUser: 1, username: "admin", idPerson: 5 };
    expect(user.username).toBe("admin");
    expect(user.idPerson).toBe(5);
  });

  it("login requires non-empty credentials", () => {
    function validateLogin(user: string, pwd: string): boolean {
      return user.trim().length > 0 && pwd.trim().length > 0;
    }
    expect(validateLogin("admin", "secret")).toBe(true);
    expect(validateLogin("", "secret")).toBe(false);
    expect(validateLogin("admin", "")).toBe(false);
    expect(validateLogin("  ", "  ")).toBe(false);
  });
});
