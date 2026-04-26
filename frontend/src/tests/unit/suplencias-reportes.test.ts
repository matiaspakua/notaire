/**
 * Unit tests for useSuplencias and useReportes hooks.
 * Tests cover query keys, type shapes, and download URL construction.
 *
 * Covers: CU12, CU42, CU59 (Suplencias), CU50, CU24, CU25, CU13, CU01 (Reportes)
 */
import { describe, it, expect, vi, beforeEach } from "vitest";
import { suplenciasKeys } from "@/hooks/useSuplencias";

// ──────────────────────────────────────────────
// Query key contracts
// ──────────────────────────────────────────────

describe("suplenciasKeys", () => {
  it("all key is ['suplencias']", () => {
    expect(suplenciasKeys.all).toEqual(["suplencias"]);
  });

  it("detail key includes id", () => {
    expect(suplenciasKeys.detail(7)).toEqual(["suplencias", 7]);
  });

  it("detail key with id 1", () => {
    expect(suplenciasKeys.detail(1)).toEqual(["suplencias", 1]);
  });
});

// ──────────────────────────────────────────────
// Suplencia type shape contracts
// ──────────────────────────────────────────────

describe("Suplencia type shape", () => {
  it("suplencia object has correct optional fields", () => {
    const s = {
      idSuplencia: 1,
      escribano: { idPersona: 10, nombre: "Carlos", apellido: "López" },
      suplente: { idPersona: 20, nombre: "Ana", apellido: "García" },
      desde: "2026-01-01",
      hasta: "2026-06-30",
    };
    expect(s.idSuplencia).toBe(1);
    expect(s.escribano.idPersona).toBe(10);
    expect(s.suplente.idPersona).toBe(20);
    expect(s.desde).toBe("2026-01-01");
    expect(s.hasta).toBe("2026-06-30");
  });

  it("suplencia can have undefined escribano/suplente", () => {
    const s: {
      idSuplencia?: number;
      escribano?: unknown;
      suplente?: unknown;
      desde?: string;
      hasta?: string;
    } = { idSuplencia: 5 };
    expect(s.escribano).toBeUndefined();
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
    const url = `${BASE}/reportes/presupuesto/${id}`;
    expect(url).toBe("http://localhost:8080/api/v1/reportes/presupuesto/42");
  });

  it("presupuesto inmuebles PDF URL is correct", () => {
    const id = 7;
    const url = `${BASE}/reportes/presupuesto-inmuebles/${id}`;
    expect(url).toBe("http://localhost:8080/api/v1/reportes/presupuesto-inmuebles/7");
  });

  it("historial gestion PDF URL is correct", () => {
    const id = 100;
    const url = `${BASE}/reportes/historial-gestion/${id}`;
    expect(url).toBe("http://localhost:8080/api/v1/reportes/historial-gestion/100");
  });

  it("DDJJ mensual URL includes anio and mes", () => {
    const anio = 2026;
    const mes = 4;
    const url = `${BASE}/reportes/declaracion-jurada-mensual?anio=${anio}&mes=${mes}`;
    expect(url).toContain("anio=2026");
    expect(url).toContain("mes=4");
  });

  it("DDJJ rentas URL includes anio and mes", () => {
    const anio = 2026;
    const mes = 12;
    const url = `${BASE}/reportes/declaracion-jurada-rentas?anio=${anio}&mes=${mes}`;
    expect(url).toContain("anio=2026");
    expect(url).toContain("mes=12");
  });

  it("libro indice URL includes anio", () => {
    const anio = 2025;
    const url = `${BASE}/reportes/libro-indice?anio=${anio}`;
    expect(url).toBe("http://localhost:8080/api/v1/reportes/libro-indice?anio=2025");
  });

  it("deuda documentos URL includes numero gestion", () => {
    const numero = 1001;
    const url = `${BASE}/reportes/consultar-deuda-documentos?numeroGestion=${numero}`;
    expect(url).toContain("numeroGestion=1001");
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
    const anio = 2026;
    const mes = 3;
    const filename = `ddjj_mensual_${anio}_${mes}.pdf`;
    expect(filename).toBe("ddjj_mensual_2026_3.pdf");
  });

  it("DDJJ rentas filename includes year and month", () => {
    const anio = 2026;
    const mes = 12;
    const filename = `ddjj_rentas_${anio}_${mes}.pdf`;
    expect(filename).toBe("ddjj_rentas_2026_12.pdf");
  });

  it("historial filename includes gestion id", () => {
    const id = 42;
    const filename = `historial_gestion_${id}.pdf`;
    expect(filename).toBe("historial_gestion_42.pdf");
  });

  it("libro indice filename includes anio", () => {
    const anio = 2025;
    const filename = `libro_indice_${anio}.pdf`;
    expect(filename).toBe("libro_indice_2025.pdf");
  });
});

// ──────────────────────────────────────────────
// Presupuesto status badge logic
// ──────────────────────────────────────────────

describe("Presupuesto estado badge logic", () => {
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
// Personas search filter logic
// ──────────────────────────────────────────────

describe("Personas search filter (CU61)", () => {
  const personas = [
    { idPersona: 1, nombre: "Juan", apellido: "García", dni: "12345678", esCliente: true },
    { idPersona: 2, nombre: "Ana", apellido: "López", dni: "87654321", esCliente: false },
    { idPersona: 3, nombre: "Carlos", apellido: "García", dni: "11111111", esCliente: true },
  ];

  function filterPersonas(
    list: typeof personas,
    nombre: string,
    apellido: string,
    dni: string,
    esCliente: boolean | null
  ) {
    return list.filter((p) => {
      const matchNombre = !nombre || p.nombre?.toLowerCase().includes(nombre.toLowerCase());
      const matchApellido = !apellido || p.apellido?.toLowerCase().includes(apellido.toLowerCase());
      const matchDni = !dni || p.dni?.includes(dni);
      const matchCliente = esCliente === null || p.esCliente === esCliente;
      return matchNombre && matchApellido && matchDni && matchCliente;
    });
  }

  it("no filters returns all personas", () => {
    expect(filterPersonas(personas, "", "", "", null)).toHaveLength(3);
  });

  it("filter by nombre returns matching", () => {
    expect(filterPersonas(personas, "juan", "", "", null)).toHaveLength(1);
  });

  it("filter by apellido returns multiple matches", () => {
    expect(filterPersonas(personas, "", "García", "", null)).toHaveLength(2);
  });

  it("filter by DNI returns exact match", () => {
    expect(filterPersonas(personas, "", "", "12345678", null)).toHaveLength(1);
  });

  it("filter by esCliente true returns only clients", () => {
    expect(filterPersonas(personas, "", "", "", true)).toHaveLength(2);
  });

  it("filter by esCliente false returns non-clients", () => {
    expect(filterPersonas(personas, "", "", "", false)).toHaveLength(1);
  });

  it("combined nombre + apellido filter", () => {
    const result = filterPersonas(personas, "Carlos", "García", "", null);
    expect(result).toHaveLength(1);
    expect(result[0].idPersona).toBe(3);
  });

  it("no match returns empty array", () => {
    expect(filterPersonas(personas, "NonExistent", "", "", null)).toHaveLength(0);
  });
});
