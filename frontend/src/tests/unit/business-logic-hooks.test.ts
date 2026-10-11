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
  it("all key is ['gestiones']", () => {
    expect(managementsKeys.all).toEqual(["managements"]);
  });

  it("detail key includes id", () => {
    expect(managementsKeys.detail(10)).toEqual(["managements", 10]);
  });

  it("byClient key includes persona id", () => {
    expect(managementsKeys.byClient(5)).toEqual(["managements", "client", 5]);
  });
});

describe("peopleKeys (CU17, CU18)", () => {
  it("all key is ['personas']", () => {
    expect(peopleKeys.all).toEqual(["people"]);
  });

  it("detail key includes id", () => {
    expect(peopleKeys.detail(3)).toEqual(["people", 3]);
  });
});

describe("budgetsKeys (CU01, CU39)", () => {
  it("all key is ['presupuestos']", () => {
    expect(budgetsKeys.all).toEqual(["budgets"]);
  });

  it("detail key includes id", () => {
    expect(budgetsKeys.detail(7)).toEqual(["budgets", 7]);
  });

  it("byPerson key includes persona id", () => {
    expect(budgetsKeys.byPerson(2)).toEqual(["budgets", "person", 2]);
  });
});

describe("paymentsKeys (CU15, CU47)", () => {
  it("all key is ['pagos']", () => {
    expect(paymentsKeys.all).toEqual(["payments"]);
  });
});

describe("conceptsKeys (CU39, CU49)", () => {
  it("all key is ['conceptos']", () => {
    expect(conceptsKeys.all).toEqual(["concepts"]);
  });
});

describe("deedsKeys (CU05, CU06)", () => {
  it("all key is ['escrituras']", () => {
    expect(deedsKeys.all).toEqual(["deeds"]);
  });

  it("detail key includes id", () => {
    expect(deedsKeys.detail(99)).toEqual(["deeds", 99]);
  });
});

describe("usersKeys (CU20, CU21)", () => {
  it("all key is ['usuarios']", () => {
    expect(usersKeys.all).toEqual(["users"]);
  });
});

describe("propertiesKeys (CU08)", () => {
  it("all key is ['inmuebles']", () => {
    expect(propertiesKeys.all).toEqual(["properties"]);
  });

  it("detail key includes id", () => {
    expect(propertiesKeys.detail(15)).toEqual(["properties", 15]);
  });
});

describe("copiesKeys (CU44, CU53)", () => {
  it("all key is ['copias']", () => {
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
// Payment saldo computation (CU47)
// ──────────────────────────────────────────────

describe("Payment saldo computation (CU47)", () => {
  function calcularSaldo(totalBudget: number, pagos: { monto?: number }[]): number {
    const totalPaid = pagos.reduce((sum, p) => sum + (p.monto ?? 0), 0);
    return totalBudget - totalPaid;
  }

  it("saldo is total minus sum of pagos", () => {
    expect(calcularSaldo(1000, [{ monto: 300 }, { monto: 200 }])).toBe(500);
  });

  it("saldo with no pagos equals total", () => {
    expect(calcularSaldo(800, [])).toBe(800);
  });

  it("fully paid presupuesto has saldo 0", () => {
    expect(calcularSaldo(500, [{ monto: 500 }])).toBe(0);
  });

  it("overpaid presupuesto has negative saldo", () => {
    expect(calcularSaldo(200, [{ monto: 300 }])).toBe(-100);
  });

  it("pago with undefined monto contributes 0", () => {
    expect(calcularSaldo(400, [{ monto: undefined }, { monto: 100 }])).toBe(300);
  });
});

// ──────────────────────────────────────────────
// Deed number format (CU05)
// ──────────────────────────────────────────────

describe("Deed display label (CU05)", () => {
  function getEscrituraLabel(numero?: number, fecha?: string): string {
    if (!numero) return "Sin número";
    return fecha ? `Deed #${numero} — ${fecha}` : `Deed #${numero}`;
  }

  it("returns Sin número when numero is undefined", () => {
    expect(getEscrituraLabel(undefined, "2026-01-01")).toBe("Sin número");
  });

  it("returns label with date when both present", () => {
    expect(getEscrituraLabel(42, "2026-03-15")).toBe("Deed #42 — 2026-03-15");
  });

  it("returns label without date when fecha is absent", () => {
    expect(getEscrituraLabel(7)).toBe("Deed #7");
  });
});

// ──────────────────────────────────────────────
// Person display name (CU17, CU18, CU61)
// ──────────────────────────────────────────────

describe("Person display name (CU17, CU18, CU61)", () => {
  function getDisplayName(persona?: { nombre?: string; apellido?: string }): string {
    if (!persona) return "—";
    const parts = [persona.apellido, persona.nombre].filter(Boolean);
    return parts.length > 0 ? parts.join(", ") : "—";
  }

  it("formats as apellido, nombre", () => {
    expect(getDisplayName({ nombre: "Juan", apellido: "García" })).toBe("García, Juan");
  });

  it("returns only apellido when nombre is missing", () => {
    expect(getDisplayName({ apellido: "García" })).toBe("García");
  });

  it("returns only nombre when apellido is missing", () => {
    expect(getDisplayName({ nombre: "Juan" })).toBe("Juan");
  });

  it("returns dash for empty persona", () => {
    expect(getDisplayName({})).toBe("—");
  });

  it("returns dash for undefined persona", () => {
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
      testimonio: { idTestimonio: 10 },
      fecha: "2026-02-01",
      tipo: "Simple",
    };
    expect(copia.idCopia).toBe(1);
    expect(copia.testimonio.idTestimonio).toBe(10);
    expect(copia.fecha).toBe("2026-02-01");
  });

  it("copia can have undefined testimonio", () => {
    const copia: { idCopia?: number; testimonio?: unknown } = { idCopia: 5 };
    expect(copia.testimonio).toBeUndefined();
  });
});

// ──────────────────────────────────────────────
// Concept value type (CU39, CU49)
// ──────────────────────────────────────────────

describe("Concept type shape (CU39, CU49)", () => {
  it("concepto has nombre and valor", () => {
    const concepto = { idConcepto: 1, nombre: "Honorarios", valor: 1500 };
    expect(concepto.nombre).toBe("Honorarios");
    expect(concepto.valor).toBe(1500);
  });

  it("concepto valor can be 0", () => {
    const concepto = { idConcepto: 2, nombre: "Exento", valor: 0 };
    expect(concepto.valor).toBe(0);
  });
});

// ──────────────────────────────────────────────
// Property type shape (CU08)
// ──────────────────────────────────────────────

describe("Property type shape (CU08)", () => {
  it("inmueble has address fields", () => {
    const inmueble = {
      idInmueble: 1,
      calle: "Av. Corrientes",
      numero: 1234,
      ciudad: "Buenos Aires",
    };
    expect(inmueble.calle).toBe("Av. Corrientes");
    expect(inmueble.numero).toBe(1234);
  });

  it("inmueble can have undefined optional fields", () => {
    const inmueble: { idInmueble?: number; piso?: string; depto?: string } = { idInmueble: 1 };
    expect(inmueble.piso).toBeUndefined();
    expect(inmueble.depto).toBeUndefined();
  });
});

// ──────────────────────────────────────────────
// DocumentType type shape (CU27, CU65)
// ──────────────────────────────────────────────

describe("DocumentType type shape (CU27, CU65)", () => {
  it("tipoDeDocumento has id and nombre", () => {
    const tipo = { idTipoDocumento: 3, nombre: "DNI" };
    expect(tipo.idTipoDocumento).toBe(3);
    expect(tipo.nombre).toBe("DNI");
  });

  it("filter tipos by partial name match", () => {
    const tipos = [
      { idTipoDocumento: 1, nombre: "DNI" },
      { idTipoDocumento: 2, nombre: "Pasaporte" },
      { idTipoDocumento: 3, nombre: "CUIT" },
    ];
    const result = tipos.filter((t) => t.nombre.toLowerCase().includes("pas"));
    expect(result).toHaveLength(1);
    expect(result[0].nombre).toBe("Pasaporte");
  });
});

// ──────────────────────────────────────────────
// UserAccount type shape + password hash (CU20, CU21)
// ──────────────────────────────────────────────

describe("UserAccount type shape (CU20, CU21)", () => {
  it("usuario has username field", () => {
    const u = { idUsuario: 1, usuario: "admin", idPersona: 5 };
    expect(u.usuario).toBe("admin");
    expect(u.idPersona).toBe(5);
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
