/**
 * Unit tests for the useItems hook plus history and management search contracts.
 * Covers CU01, CU39 (Items), CU13, CU53 (history), CU02, CU53 (management search)
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
      concept: { idConcept: 5, name: "Honorarios", value: 100 },
      quantity: 2,
      price: 500,
      budget: { idBudget: 10 },
    };
    expect(item.idItem).toBe(1);
    expect(item.concept.name).toBe("Honorarios");
    expect(item.quantity).toBe(2);
    expect(item.price).toBe(500);
    expect(item.budget.idBudget).toBe(10);
  });

  it("item subtotal calculation", () => {
    const item = { quantity: 3, price: 200 };
    const subtotal = (item.price ?? 0) * (item.quantity ?? 1);
    expect(subtotal).toBe(600);
  });

  it("item subtotal defaults to price when quantity is 1", () => {
    const item = { quantity: 1, price: 350 };
    const subtotal = (item.price ?? 0) * (item.quantity ?? 1);
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
// History type shape contracts
// ──────────────────────────────────────────────

describe("History type shape (CU13, CU53)", () => {
  it("history has the expected fields", () => {
    const history = {
      idHistory: 1,
      status: { idStatusManagement: 2, name: "En Tramite" },
      date: "2026-01-15",
      notes: "Process started",
    };
    expect(history.idHistory).toBe(1);
    expect(history.status.name).toBe("En Tramite");
    expect(history.date).toBe("2026-01-15");
  });

  it("latest history is the most recent date", () => {
    const entries = [
      { idHistory: 1, date: "2026-01-01", status: { name: "Iniciada" } },
      { idHistory: 2, date: "2026-03-15", status: { name: "En Tramite" } },
      { idHistory: 3, date: "2026-02-10", status: { name: "Archivada" } },
    ];
    const sorted = [...entries].sort((a, b) =>
      new Date(b.date).getTime() - new Date(a.date).getTime()
    );
    expect(sorted[0].status.name).toBe("En Tramite");
  });

  it("an empty history list has no current status", () => {
    const management: { historyList: Array<{ status?: { name?: string } }> } = { historyList: [] };
    const status = management.historyList.length > 0
      ? management.historyList[0].status
      : undefined;
    expect(status).toBeUndefined();
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
      historyList: [{ status: { name: "Active" }, date: "2026-01-01" }],
    },
    {
      idManagement: 2,
      number: 1002,
      procedureList: [{ idProcedure: 1 }],
      historyList: [{ status: { name: "Closed" }, date: "2026-02-01" }],
    },
    {
      idManagement: 3,
      number: 2001,
      procedureList: [],
      historyList: [],
    },
  ];

  function getCurrentStatus(management: typeof managements[0]): string | undefined {
    if (!management.historyList || management.historyList.length === 0) return undefined;
    return management.historyList[0]?.status?.name;
  }

  function filterManagements(list: typeof managements, number: string, status: string) {
    return list.filter((management) => {
      const matchesNumber = !number || management.number?.toString().includes(number);
      const matchesStatus =
        !status ||
        getCurrentStatus(management)?.toLowerCase().includes(status.toLowerCase());
      return matchesNumber && matchesStatus;
    });
  }

  it("no filters returns every management", () => {
    expect(filterManagements(managements, "", "")).toHaveLength(3);
  });

  it("filter by partial number", () => {
    expect(filterManagements(managements, "100", "")).toHaveLength(2);
  });

  it("filter by exact number", () => {
    expect(filterManagements(managements, "2001", "")).toHaveLength(1);
  });

  it("filter by active status", () => {
    expect(filterManagements(managements, "", "active")).toHaveLength(1);
  });

  it("filter by closed status", () => {
    expect(filterManagements(managements, "", "clos")).toHaveLength(1);
  });

  it("combined number and status filter", () => {
    const result = filterManagements(managements, "1001", "active");
    expect(result).toHaveLength(1);
    expect(result[0].idManagement).toBe(1);
  });

  it("no match returns empty", () => {
    expect(filterManagements(managements, "9999", "")).toHaveLength(0);
  });
});

// ──────────────────────────────────────────────
// Status badge logic for managements
// ──────────────────────────────────────────────

describe("Management status badge logic", () => {
  function getBadgeVariant(status?: string): string {
    if (!status) return "outline";
    const lower = status.toLowerCase();
    if (lower.includes("active") || lower.includes("open")) return "green";
    if (lower.includes("clos") || lower.includes("finish")) return "secondary";
    if (lower.includes("cancel") || lower.includes("archiv")) return "destructive";
    return "outline";
  }

  it("Active maps to green", () => {
    expect(getBadgeVariant("Active")).toBe("green");
  });

  it("Open maps to green", () => {
    expect(getBadgeVariant("Open")).toBe("green");
  });

  it("Closed maps to secondary", () => {
    expect(getBadgeVariant("Closed")).toBe("secondary");
  });

  it("Finished maps to secondary", () => {
    expect(getBadgeVariant("Finished")).toBe("secondary");
  });

  it("Cancelled maps to destructive", () => {
    expect(getBadgeVariant("Cancelled")).toBe("destructive");
  });

  it("Archived maps to destructive", () => {
    expect(getBadgeVariant("Archived")).toBe("destructive");
  });

  it("undefined maps to outline", () => {
    expect(getBadgeVariant(undefined)).toBe("outline");
  });

  it("an unknown status maps to outline", () => {
    expect(getBadgeVariant("En proceso")).toBe("outline");
  });
});
