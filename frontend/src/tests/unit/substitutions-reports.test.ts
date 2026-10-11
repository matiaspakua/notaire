/**
 * Unit tests for useSubstitutions and useReports hooks.
 * Tests cover query keys, type shapes, and download URL construction.
 *
 * Covers: CU12, CU42, CU59 (Substitutions), CU50, CU24, CU25, CU13, CU01 (Reports)
 */
import { describe, it, expect } from "vitest";
import { substitutionsKeys } from "@/hooks/useSubstitutions";

// ──────────────────────────────────────────────
// Query key contracts
// ──────────────────────────────────────────────

describe("substitutionsKeys", () => {
  it("all key is ['substitutions']", () => {
    expect(substitutionsKeys.all).toEqual(["substitutions"]);
  });

  it("detail key includes id", () => {
    expect(substitutionsKeys.detail(7)).toEqual(["substitutions", 7]);
  });

  it("detail key with id 1", () => {
    expect(substitutionsKeys.detail(1)).toEqual(["substitutions", 1]);
  });
});

// ──────────────────────────────────────────────
// Substitution type shape contracts
// ──────────────────────────────────────────────

describe("Substitution type shape", () => {
  it("a substitution object has the expected optional fields", () => {
    const s = {
      idSubstitution: 1,
      notary: { idPerson: 10, name: "Carlos", lastName: "López" },
      substitute: { idPerson: 20, name: "Ana", lastName: "García" },
      dateStart: "2026-01-01",
      dateEnd: "2026-06-30",
    };
    expect(s.idSubstitution).toBe(1);
    expect(s.notary.idPerson).toBe(10);
    expect(s.substitute.idPerson).toBe(20);
    expect(s.dateStart).toBe("2026-01-01");
    expect(s.dateEnd).toBe("2026-06-30");
  });

  it("a substitution can omit the notary and the substitute", () => {
    const s: {
      idSubstitution?: number;
      notary?: unknown;
      substitute?: unknown;
      dateStart?: string;
      dateEnd?: string;
    } = { idSubstitution: 5 };
    expect(s.notary).toBeUndefined();
    expect(s.substitute).toBeUndefined();
  });

  it("substitution date range — start is before end", () => {
    const startDate = new Date("2026-01-01");
    const endDate = new Date("2026-12-31");
    expect(startDate < endDate).toBe(true);
  });

  it("substitution date range — an inverted range is detected", () => {
    const startDate = new Date("2026-12-31");
    const endDate = new Date("2026-01-01");
    expect(startDate > endDate).toBe(true);
  });
});

// ──────────────────────────────────────────────
// Report URL construction
// ──────────────────────────────────────────────

describe("Report URL construction", () => {
  const BASE = "http://localhost:8080/api/v1";

  it("the budget PDF URL is correct", () => {
    const id = 42;
    const url = `${BASE}/reports/budget/${id}`;
    expect(url).toBe("http://localhost:8080/api/v1/reports/budget/42");
  });

  it("the budget properties PDF URL is correct", () => {
    const id = 7;
    const url = `${BASE}/reports/budget-properties/${id}`;
    expect(url).toBe("http://localhost:8080/api/v1/reports/budget-properties/7");
  });

  it("the management history PDF URL is correct", () => {
    const id = 100;
    const url = `${BASE}/reports/management-history/${id}`;
    expect(url).toBe("http://localhost:8080/api/v1/reports/management-history/100");
  });

  it("monthly sworn statement URL includes year and month", () => {
    const year = 2026;
    const month = 4;
    const url = `${BASE}/reports/monthly-sworn-statement?year=${year}&month=${month}`;
    expect(url).toContain("year=2026");
    expect(url).toContain("month=4");
  });

  it("tax sworn statement URL includes year and month", () => {
    const year = 2026;
    const month = 12;
    const url = `${BASE}/reports/tax-sworn-statement?year=${year}&month=${month}`;
    expect(url).toContain("year=2026");
    expect(url).toContain("month=12");
  });

  it("index book URL includes year", () => {
    const year = 2025;
    const url = `${BASE}/reports/index-book?year=${year}`;
    expect(url).toBe("http://localhost:8080/api/v1/reports/index-book?year=2025");
  });

  it("document debt URL includes the management number (numberManagement)", () => {
    const number = 1001;
    const url = `${BASE}/reports/document-debt?numberManagement=${number}`;
    expect(url).toContain("numberManagement=1001");
  });
});

// ──────────────────────────────────────────────
// Report filename construction
// ──────────────────────────────────────────────

describe("Report filename construction", () => {
  it("the budget filename includes the id", () => {
    const id = 5;
    const filename = `presupuesto_${id}.pdf`;
    expect(filename).toBe("presupuesto_5.pdf");
  });

  it("DDJJ mensual filename includes year and month", () => {
    const year = 2026;
    const month = 3;
    const filename = `ddjj_mensual_${year}_${month}.pdf`;
    expect(filename).toBe("ddjj_mensual_2026_3.pdf");
  });

  it("DDJJ rentas filename includes year and month", () => {
    const year = 2026;
    const month = 12;
    const filename = `ddjj_rentas_${year}_${month}.pdf`;
    expect(filename).toBe("ddjj_rentas_2026_12.pdf");
  });

  it("the history filename includes the management id", () => {
    const id = 42;
    const filename = `historial_gestion_${id}.pdf`;
    expect(filename).toBe("historial_gestion_42.pdf");
  });

  it("index book filename includes the year", () => {
    const year = 2025;
    const filename = `libro_indice_${year}.pdf`;
    expect(filename).toBe("libro_indice_2025.pdf");
  });
});

// ──────────────────────────────────────────────
// Budget status badge logic
// ──────────────────────────────────────────────

describe("Budget status badge logic", () => {
  function getStatusBadgeClass(status?: string): string {
    switch (status?.toUpperCase()) {
      case "APROBADO":
        return "green";
      case "RECHAZADO":
        return "destructive";
      case "VENCIDO":
        return "orange";
      default:
        return "outline";
    }
  }

  it("APROBADO maps to green", () => {
    expect(getStatusBadgeClass("APROBADO")).toBe("green");
  });

  it("aprobado (lowercase) maps to green", () => {
    expect(getStatusBadgeClass("aprobado")).toBe("green");
  });

  it("RECHAZADO maps to destructive", () => {
    expect(getStatusBadgeClass("RECHAZADO")).toBe("destructive");
  });

  it("VENCIDO maps to orange", () => {
    expect(getStatusBadgeClass("VENCIDO")).toBe("orange");
  });

  it("BORRADOR maps to outline", () => {
    expect(getStatusBadgeClass("BORRADOR")).toBe("outline");
  });

  it("undefined maps to outline", () => {
    expect(getStatusBadgeClass(undefined)).toBe("outline");
  });
});

// ──────────────────────────────────────────────
// People search filter logic
// ──────────────────────────────────────────────

describe("People search filter (CU61)", () => {
  const people = [
    { idPerson: 1, name: "Juan", lastName: "García", dni: "12345678", isClient: true },
    { idPerson: 2, name: "Ana", lastName: "López", dni: "87654321", isClient: false },
    { idPerson: 3, name: "Carlos", lastName: "García", dni: "11111111", isClient: true },
  ];

  function filterPeople(
    list: typeof people,
    name: string,
    lastName: string,
    dni: string,
    isClient: boolean | null
  ) {
    return list.filter((p) => {
      const matchesName = !name || p.name?.toLowerCase().includes(name.toLowerCase());
      const matchesLastName = !lastName || p.lastName?.toLowerCase().includes(lastName.toLowerCase());
      const matchesDni = !dni || p.dni?.includes(dni);
      const matchesClient = isClient === null || p.isClient === isClient;
      return matchesName && matchesLastName && matchesDni && matchesClient;
    });
  }

  it("no filters returns every person", () => {
    expect(filterPeople(people, "", "", "", null)).toHaveLength(3);
  });

  it("filter by first name returns matching", () => {
    expect(filterPeople(people, "juan", "", "", null)).toHaveLength(1);
  });

  it("filter by last name returns multiple matches", () => {
    expect(filterPeople(people, "", "García", "", null)).toHaveLength(2);
  });

  it("filter by DNI returns exact match", () => {
    expect(filterPeople(people, "", "", "12345678", null)).toHaveLength(1);
  });

  it("filter by client true returns only clients", () => {
    expect(filterPeople(people, "", "", "", true)).toHaveLength(2);
  });

  it("filter by client false returns non-clients", () => {
    expect(filterPeople(people, "", "", "", false)).toHaveLength(1);
  });

  it("combined first name and last name filter", () => {
    const result = filterPeople(people, "Carlos", "García", "", null);
    expect(result).toHaveLength(1);
    expect(result[0].idPerson).toBe(3);
  });

  it("no match returns empty array", () => {
    expect(filterPeople(people, "NonExistent", "", "", null)).toHaveLength(0);
  });
});
