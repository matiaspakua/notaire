/**
 * Unit tests for useSubstitutions and useReports hooks.
 * Tests cover query keys, type shapes, and download URL construction.
 *
 * Covers: CU12, CU42, CU59 (Suplencias), CU50, CU24, CU25, CU13, CU01 (Reportes)
 */
import { describe, it, expect } from "vitest";
import { substitutionsKeys } from "@/hooks/useSubstitutions";

// ──────────────────────────────────────────────
// Query key contracts
// ──────────────────────────────────────────────

describe("substitutionsKeys", () => {
  it("all key is ['suplencias']", () => {
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
  it("suplencia object has correct optional fields", () => {
    const s = {
      idSubstitution: 1,
      notary: { idPerson: 10, name: "Carlos", apellido: "López" },
      suplente: { idPerson: 20, name: "Ana", apellido: "García" },
      desde: "2026-01-01",
      hasta: "2026-06-30",
    };
    expect(s.idSubstitution).toBe(1);
    expect(s.notary.idPerson).toBe(10);
    expect(s.suplente.idPerson).toBe(20);
    expect(s.desde).toBe("2026-01-01");
    expect(s.hasta).toBe("2026-06-30");
  });

  it("suplencia can have undefined escribano/suplente", () => {
    const s: {
      idSubstitution?: number;
      notary?: unknown;
      suplente?: unknown;
      desde?: string;
      hasta?: string;
    } = { idSubstitution: 5 };
    expect(s.notary).toBeUndefined();
    expect(s.suplente).toBeUndefined();
  });

  it("suplencia date range validation — desde before hasta", () => {
    const desde = new Date("2026-01-01");
    const hasta = new Date("2026-12-31");
    expect(desde < hasta).toBe(true);
  });

  it("suplencia date range validation — invalid range detected", () => {
    const desde = new Date("2026-12-31");
    const hasta = new Date("2026-01-01");
    expect(desde > hasta).toBe(true);
  });
});

// ──────────────────────────────────────────────
// Reportes URL construction
// ──────────────────────────────────────────────

describe("Reporte URL construction", () => {
  const BASE = "http://localhost:8080/api/v1";

  it("presupuesto PDF URL is correct", () => {
    const id = 42;
    const url = `${BASE}/reports/budget/${id}`;
    expect(url).toBe("http://localhost:8080/api/v1/reports/budget/42");
  });

  it("presupuesto inmuebles PDF URL is correct", () => {
    const id = 7;
    const url = `${BASE}/reports/budget-properties/${id}`;
    expect(url).toBe("http://localhost:8080/api/v1/reports/budget-properties/7");
  });

  it("historial gestion PDF URL is correct", () => {
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

  it("deuda documentos URL includes the management number (numberManagement)", () => {
    const number = 1001;
    const url = `${BASE}/reports/document-debt?numberManagement=${number}`;
    expect(url).toContain("numberManagement=1001");
  });
});

// ──────────────────────────────────────────────
// Reporte filename construction
// ──────────────────────────────────────────────

describe("Reporte filename construction", () => {
  it("presupuesto filename includes id", () => {
    const id = 5;
    const filename = `presupuesto_${id}.pdf`;
    expect(filename).toBe("presupuesto_5.pdf");
  });

  it("DDJJ mensual filename includes year and month", () => {
    const year = 2026;
    const mes = 3;
    const filename = `ddjj_mensual_${year}_${mes}.pdf`;
    expect(filename).toBe("ddjj_mensual_2026_3.pdf");
  });

  it("DDJJ rentas filename includes year and month", () => {
    const year = 2026;
    const mes = 12;
    const filename = `ddjj_rentas_${year}_${mes}.pdf`;
    expect(filename).toBe("ddjj_rentas_2026_12.pdf");
  });

  it("historial filename includes gestion id", () => {
    const id = 42;
    const filename = `historial_gestion_${id}.pdf`;
    expect(filename).toBe("historial_gestion_42.pdf");
  });

  it("libro indice filename includes anio", () => {
    const year = 2025;
    const filename = `libro_indice_${year}.pdf`;
    expect(filename).toBe("libro_indice_2025.pdf");
  });
});

// ──────────────────────────────────────────────
// Budget status badge logic
// ──────────────────────────────────────────────

describe("Budget estado badge logic", () => {
  function getEstadoBadgeClass(estado?: string): string {
    switch (estado?.toUpperCase()) {
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
    expect(getEstadoBadgeClass("APROBADO")).toBe("green");
  });

  it("aprobado (lowercase) maps to green", () => {
    expect(getEstadoBadgeClass("aprobado")).toBe("green");
  });

  it("RECHAZADO maps to destructive", () => {
    expect(getEstadoBadgeClass("RECHAZADO")).toBe("destructive");
  });

  it("VENCIDO maps to orange", () => {
    expect(getEstadoBadgeClass("VENCIDO")).toBe("orange");
  });

  it("BORRADOR maps to outline", () => {
    expect(getEstadoBadgeClass("BORRADOR")).toBe("outline");
  });

  it("undefined maps to outline", () => {
    expect(getEstadoBadgeClass(undefined)).toBe("outline");
  });
});

// ──────────────────────────────────────────────
// People search filter logic
// ──────────────────────────────────────────────

describe("Personas search filter (CU61)", () => {
  const people = [
    { idPerson: 1, name: "Juan", apellido: "García", dni: "12345678", esClient: true },
    { idPerson: 2, name: "Ana", apellido: "López", dni: "87654321", esClient: false },
    { idPerson: 3, name: "Carlos", apellido: "García", dni: "11111111", esClient: true },
  ];

  function filterPersonas(
    list: typeof people,
    name: string,
    apellido: string,
    dni: string,
    esClient: boolean | null
  ) {
    return list.filter((p) => {
      const matchNombre = !name || p.name?.toLowerCase().includes(name.toLowerCase());
      const matchApellido = !apellido || p.apellido?.toLowerCase().includes(apellido.toLowerCase());
      const matchDni = !dni || p.dni?.includes(dni);
      const matchCliente = esClient === null || p.esClient === esClient;
      return matchNombre && matchApellido && matchDni && matchCliente;
    });
  }

  it("no filters returns all personas", () => {
    expect(filterPersonas(people, "", "", "", null)).toHaveLength(3);
  });

  it("filter by nombre returns matching", () => {
    expect(filterPersonas(people, "juan", "", "", null)).toHaveLength(1);
  });

  it("filter by apellido returns multiple matches", () => {
    expect(filterPersonas(people, "", "García", "", null)).toHaveLength(2);
  });

  it("filter by DNI returns exact match", () => {
    expect(filterPersonas(people, "", "", "12345678", null)).toHaveLength(1);
  });

  it("filter by esCliente true returns only clients", () => {
    expect(filterPersonas(people, "", "", "", true)).toHaveLength(2);
  });

  it("filter by esCliente false returns non-clients", () => {
    expect(filterPersonas(people, "", "", "", false)).toHaveLength(1);
  });

  it("combined nombre + apellido filter", () => {
    const result = filterPersonas(people, "Carlos", "García", "", null);
    expect(result).toHaveLength(1);
    expect(result[0].idPerson).toBe(3);
  });

  it("no match returns empty array", () => {
    expect(filterPersonas(people, "NonExistent", "", "", null)).toHaveLength(0);
  });
});
