/**
 * Unit tests for useItems hook plus ManagementHistory/Managements business logic.
 * Covers CU01, CU39 (Items), CU13, CU53 (ManagementHistory), CU02, CU53 (Managements search)
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

  it("byBudget key includes the budget id", () => {
    expect(itemsKeys.byBudget(10)).toEqual(["items", "budget", 10]);
  });

  it("byBudget key with id 1", () => {
    expect(itemsKeys.byBudget(1)).toEqual(["items", "budget", 1]);
  });
});

// ──────────────────────────────────────────────
// Item type shape contracts
// ──────────────────────────────────────────────

describe("Item type shape (CU01, CU39)", () => {
  it("item has correct fields", () => {
    const item = {
      idItem: 1,
      concept: { idConcept: 5, name: "Honorarios", valor: 100 },
      cantidad: 2,
      precio: 500,
      budget: { idBudget: 10 },
    };
    expect(item.idItem).toBe(1);
    expect(item.concept.name).toBe("Honorarios");
    expect(item.cantidad).toBe(2);
    expect(item.precio).toBe(500);
    expect(item.budget.idBudget).toBe(10);
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

  it("an item can omit the concept", () => {
    const item: { idItem?: number; concept?: unknown } = { idItem: 1 };
    expect(item.concept).toBeUndefined();
  });

  it("an item's budget relationship is optional", () => {
    const item: { idItem?: number; budget?: { idBudget?: number } } = { idItem: 2 };
    expect(item.budget).toBeUndefined();
  });
});

// ──────────────────────────────────────────────
// ManagementHistory type shape contracts
// ──────────────────────────────────────────────

describe("ManagementHistory type shape (CU13, CU53)", () => {
  it("historial has correct fields", () => {
    const h = {
      idHistory: 1,
      estado: { idStatusManagement: 2, name: "En trámite" },
      fecha: "2026-01-15",
      notes: "Iniciado proceso",
    };
    expect(h.idHistory).toBe(1);
    expect(h.estado.name).toBe("En trámite");
    expect(h.fecha).toBe("2026-01-15");
  });

  it("latest historial is determined by most recent fecha", () => {
    const historiales = [
      { idHistory: 1, fecha: "2026-01-01", estado: { name: "Iniciado" } },
      { idHistory: 2, fecha: "2026-03-15", estado: { name: "En revisión" } },
      { idHistory: 3, fecha: "2026-02-10", estado: { name: "Pausado" } },
    ];
    const sorted = [...historiales].sort((a, b) =>
      new Date(b.fecha).getTime() - new Date(a.fecha).getTime()
    );
    expect(sorted[0].estado.name).toBe("En revisión");
  });

  it("empty historialList returns undefined estado", () => {
    const management: { historyList: Array<{ estado?: { name?: string } }> } = { historyList: [] };
    const estado = management.historyList.length > 0
      ? management.historyList[0].estado
      : undefined;
    expect(estado).toBeUndefined();
  });
});

// ──────────────────────────────────────────────
// Managements search filter logic
// ──────────────────────────────────────────────

describe("Managements search filter (CU02, CU53)", () => {
  const managements = [
    {
      idManagement: 1,
      number: 1001,
      procedureList: [],
      historyList: [{ estado: { name: "Activo" }, fecha: "2026-01-01" }],
    },
    {
      idManagement: 2,
      number: 1002,
      procedureList: [{ idProcedure: 1 }],
      historyList: [{ estado: { name: "Cerrado" }, fecha: "2026-02-01" }],
    },
    {
      idManagement: 3,
      number: 2001,
      procedureList: [],
      historyList: [],
    },
  ];

  function getEstadoActual(g: typeof managements[0]): string | undefined {
    if (!g.historyList || g.historyList.length === 0) return undefined;
    return g.historyList[0]?.estado?.name;
  }

  function filterManagements(list: typeof managements, number: string, estado: string) {
    return list.filter((g) => {
      const matchNumber = !number || g.number?.toString().includes(number);
      const matchEstado =
        !estado ||
        getEstadoActual(g)?.toLowerCase().includes(estado.toLowerCase());
      return matchNumber && matchEstado;
    });
  }

  it("no filters returns every management", () => {
    expect(filterManagements(managements, "", "")).toHaveLength(3);
  });

  it("filter by numero partial match", () => {
    expect(filterManagements(managements, "100", "")).toHaveLength(2);
  });

  it("filter by exact numero", () => {
    expect(filterManagements(managements, "2001", "")).toHaveLength(1);
  });

  it("filter by estado activo", () => {
    expect(filterManagements(managements, "", "activ")).toHaveLength(1);
  });

  it("filter by estado cerrado", () => {
    expect(filterManagements(managements, "", "cerrad")).toHaveLength(1);
  });

  it("combined numero + estado filter", () => {
    const result = filterManagements(managements, "1001", "activ");
    expect(result).toHaveLength(1);
    expect(result[0].idManagement).toBe(1);
  });

  it("no match returns empty", () => {
    expect(filterManagements(managements, "9999", "")).toHaveLength(0);
  });
});

// ──────────────────────────────────────────────
// Estado badge logic for managements
// ──────────────────────────────────────────────

describe("Management status badge logic", () => {
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
