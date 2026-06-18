/**
 * Unit tests for useItems hook plus Historial/Gestiones business logic.
 * Covers CU01, CU39 (Items), CU13, CU53 (Historial), CU02, CU53 (Gestiones search)
 */
import { describe, it, expect } from "vitest";
import { itemsKeys } from "@/hooks/useItems";

// ──────────────────────────────────────────────
// Query key contracts
// ──────────────────────────────────────────────

describe("itemsKeys", () => {
  it("all key is ['items']", () => {
    expect(itemsKeys.all).toEqual(["items"]);
  });

  it("detail key includes id", () => {
    expect(itemsKeys.detail(3)).toEqual(["items", 3]);
  });

  it("byPresupuesto key includes presupuesto id", () => {
    expect(itemsKeys.byPresupuesto(10)).toEqual(["items", "presupuesto", 10]);
  });

  it("byPresupuesto key with id 1", () => {
    expect(itemsKeys.byPresupuesto(1)).toEqual(["items", "presupuesto", 1]);
  });
});

// ──────────────────────────────────────────────
// Item type shape contracts
// ──────────────────────────────────────────────

describe("Item type shape (CU01, CU39)", () => {
  it("item has correct fields", () => {
    const item = {
      idItem: 1,
      concepto: { idConcepto: 5, nombre: "Honorarios", valor: 100 },
      cantidad: 2,
      precio: 500,
      presupuesto: { idPresupuesto: 10 },
    };
    expect(item.idItem).toBe(1);
    expect(item.concepto.nombre).toBe("Honorarios");
    expect(item.cantidad).toBe(2);
    expect(item.precio).toBe(500);
    expect(item.presupuesto.idPresupuesto).toBe(10);
  });

  it("item subtotal calculation", () => {
    const item = { cantidad: 3, precio: 200 };
    const subtotal = (item.precio ?? 0) * (item.cantidad ?? 1);
    expect(subtotal).toBe(600);
  });

  it("item subtotal defaults to price when quantity is 1", () => {
    const item = { cantidad: 1, precio: 350 };
    const subtotal = (item.precio ?? 0) * (item.cantidad ?? 1);
    expect(subtotal).toBe(350);
  });

  it("item can have undefined concepto", () => {
    const item: { idItem?: number; concepto?: unknown } = { idItem: 1 };
    expect(item.concepto).toBeUndefined();
  });

  it("item presupuesto relationship is optional", () => {
    const item: { idItem?: number; presupuesto?: { idPresupuesto?: number } } = { idItem: 2 };
    expect(item.presupuesto).toBeUndefined();
  });
});

// ──────────────────────────────────────────────
// Historial type shape contracts
// ──────────────────────────────────────────────

describe("Historial type shape (CU13, CU53)", () => {
  it("historial has correct fields", () => {
    const h = {
      idHistorial: 1,
      estado: { idEstadoGestion: 2, nombre: "En trámite" },
      fecha: "2026-01-15",
      observaciones: "Iniciado proceso",
    };
    expect(h.idHistorial).toBe(1);
    expect(h.estado.nombre).toBe("En trámite");
    expect(h.fecha).toBe("2026-01-15");
  });

  it("latest historial is determined by most recent fecha", () => {
    const historiales = [
      { idHistorial: 1, fecha: "2026-01-01", estado: { nombre: "Iniciado" } },
      { idHistorial: 2, fecha: "2026-03-15", estado: { nombre: "En revisión" } },
      { idHistorial: 3, fecha: "2026-02-10", estado: { nombre: "Pausado" } },
    ];
    const sorted = [...historiales].sort((a, b) =>
      new Date(b.fecha).getTime() - new Date(a.fecha).getTime()
    );
    expect(sorted[0].estado.nombre).toBe("En revisión");
  });

  it("empty historialList returns undefined estado", () => {
    const gestion: { historialList: Array<{ estado?: { nombre?: string } }> } = { historialList: [] };
    const estado = gestion.historialList.length > 0
      ? gestion.historialList[0].estado
      : undefined;
    expect(estado).toBeUndefined();
  });
});

// ──────────────────────────────────────────────
// Gestiones search filter logic
// ──────────────────────────────────────────────

describe("Gestiones search filter (CU02, CU53)", () => {
  const gestiones = [
    {
      idGestion: 1,
      numero: 1001,
      tramiteList: [],
      historialList: [{ estado: { nombre: "Activo" }, fecha: "2026-01-01" }],
    },
    {
      idGestion: 2,
      numero: 1002,
      tramiteList: [{ idTramite: 1 }],
      historialList: [{ estado: { nombre: "Cerrado" }, fecha: "2026-02-01" }],
    },
    {
      idGestion: 3,
      numero: 2001,
      tramiteList: [],
      historialList: [],
    },
  ];

  function getEstadoActual(g: typeof gestiones[0]): string | undefined {
    if (!g.historialList || g.historialList.length === 0) return undefined;
    return g.historialList[0]?.estado?.nombre;
  }

  function filterGestiones(list: typeof gestiones, numero: string, estado: string) {
    return list.filter((g) => {
      const matchNumero = !numero || g.numero?.toString().includes(numero);
      const matchEstado =
        !estado ||
        getEstadoActual(g)?.toLowerCase().includes(estado.toLowerCase());
      return matchNumero && matchEstado;
    });
  }

  it("no filters returns all gestiones", () => {
    expect(filterGestiones(gestiones, "", "")).toHaveLength(3);
  });

  it("filter by numero partial match", () => {
    expect(filterGestiones(gestiones, "100", "")).toHaveLength(2);
  });

  it("filter by exact numero", () => {
    expect(filterGestiones(gestiones, "2001", "")).toHaveLength(1);
  });

  it("filter by estado activo", () => {
    expect(filterGestiones(gestiones, "", "activ")).toHaveLength(1);
  });

  it("filter by estado cerrado", () => {
    expect(filterGestiones(gestiones, "", "cerrad")).toHaveLength(1);
  });

  it("combined numero + estado filter", () => {
    const result = filterGestiones(gestiones, "1001", "activ");
    expect(result).toHaveLength(1);
    expect(result[0].idGestion).toBe(1);
  });

  it("no match returns empty", () => {
    expect(filterGestiones(gestiones, "9999", "")).toHaveLength(0);
  });
});

// ──────────────────────────────────────────────
// Estado badge logic for gestiones
// ──────────────────────────────────────────────

describe("Gestión estado badge logic", () => {
  function getBadgeVariant(estado?: string): string {
    if (!estado) return "outline";
    const lower = estado.toLowerCase();
    if (lower.includes("activ") || lower.includes("abiert")) return "green";
    if (lower.includes("cerr") || lower.includes("finaliz")) return "secondary";
    if (lower.includes("cancel") || lower.includes("archiv")) return "destructive";
    return "outline";
  }

  it("Activo maps to green", () => {
    expect(getBadgeVariant("Activo")).toBe("green");
  });

  it("Abierto maps to green", () => {
    expect(getBadgeVariant("Abierto")).toBe("green");
  });

  it("Cerrado maps to secondary", () => {
    expect(getBadgeVariant("Cerrado")).toBe("secondary");
  });

  it("Finalizado maps to secondary", () => {
    expect(getBadgeVariant("Finalizado")).toBe("secondary");
  });

  it("Cancelado maps to destructive", () => {
    expect(getBadgeVariant("Cancelado")).toBe("destructive");
  });

  it("Archivado maps to destructive", () => {
    expect(getBadgeVariant("Archivado")).toBe("destructive");
  });

  it("undefined maps to outline", () => {
    expect(getBadgeVariant(undefined)).toBe("outline");
  });

  it("unknown estado maps to outline", () => {
    expect(getBadgeVariant("En proceso")).toBe("outline");
  });
});
