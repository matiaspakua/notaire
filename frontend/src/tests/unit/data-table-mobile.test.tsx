/**
 * DataTable mobile card layout (issue #1356, RNF-05/RNF-06).
 *
 * Below the `md` breakpoint (768px) every row renders as a card: the primary
 * column is the title, the other columns are label/value pairs in a <dl>, and
 * the actions column is a visible row of buttons. Desktop keeps the table.
 */
import { afterEach, describe, expect, it, vi } from "vitest";
import { cleanup, render, screen, within } from "@testing-library/react";
import { DataTable, type Column } from "@/components/shared/DataTable";

interface Row {
  id: number;
  name: string;
  email: string;
  internal: string;
}

const columns: Column<Row>[] = [
  { key: "id", header: "ID", render: (r) => r.id },
  { key: "name", header: "Nombre", render: (r) => r.name },
  { key: "email", header: "Email", render: (r) => r.email },
  { key: "internal", header: "Interno", render: (r) => r.internal, mobile: "hidden" },
  {
    key: "actions",
    header: "",
    render: (r) => (
      <button type="button" aria-label={`Editar ${r.name}`}>
        e
      </button>
    ),
  },
];

const data: Row[] = [
  { id: 1, name: "Juan García", email: "juan@example.com", internal: "x1" },
  { id: 2, name: "Ana López", email: "ana@example.com", internal: "x2" },
];

function mockViewport(width: number) {
  vi.stubGlobal(
    "matchMedia",
    (query: string) => {
      const max = /max-width:\s*(\d+)px/.exec(query);
      const matches = max ? width <= Number(max[1]) : false;
      return {
        matches,
        media: query,
        onchange: null,
        addEventListener: () => {},
        removeEventListener: () => {},
        addListener: () => {},
        removeListener: () => {},
        dispatchEvent: () => false,
      };
    },
  );
}

afterEach(() => {
  cleanup();
  vi.unstubAllGlobals();
});

describe("DataTable on mobile (#1356)", () => {
  it("renders one card per row instead of a table below 768px", () => {
    mockViewport(390);
    render(<DataTable data={data} columns={columns} keyExtractor={(r) => r.id} />);
    expect(screen.queryByRole("table")).toBeNull();
    const list = screen.getByRole("list");
    expect(within(list).getAllByRole("listitem")).toHaveLength(2);
  });

  it("uses the first non-id column as the card title and the rest as label/value pairs", () => {
    mockViewport(390);
    render(<DataTable data={data} columns={columns} keyExtractor={(r) => r.id} />);
    const card = screen.getAllByRole("listitem")[0];
    expect(within(card).getByTestId("data-table-card-title").textContent).toBe("Juan García");
    const terms = within(card).getAllByRole("term").map((el) => el.textContent);
    expect(terms).toEqual(["ID", "Email"]);
    const values = within(card).getAllByRole("definition").map((el) => el.textContent);
    expect(values).toEqual(["1", "juan@example.com"]);
  });

  it("honours an explicit primary column", () => {
    mockViewport(390);
    const cols = columns.map((c) => (c.key === "email" ? { ...c, mobile: "primary" as const } : c));
    render(<DataTable data={data} columns={cols} keyExtractor={(r) => r.id} />);
    const card = screen.getAllByRole("listitem")[0];
    expect(within(card).getByTestId("data-table-card-title").textContent).toBe("juan@example.com");
  });

  it("leaves out columns marked hidden on mobile", () => {
    mockViewport(390);
    render(<DataTable data={data} columns={columns} keyExtractor={(r) => r.id} />);
    expect(screen.queryByText("x1")).toBeNull();
    expect(screen.queryByText("Interno")).toBeNull();
  });

  it("shows the row actions in the card", () => {
    mockViewport(390);
    render(<DataTable data={data} columns={columns} keyExtractor={(r) => r.id} />);
    const card = screen.getAllByRole("listitem")[1];
    expect(within(within(card).getByTestId("data-table-card-actions")).getByRole("button", { name: "Editar Ana López" })).toBeDefined();
  });

  it("shows the empty message as a card list state", () => {
    mockViewport(390);
    render(<DataTable data={[]} columns={columns} keyExtractor={(r) => r.id} emptyMessage="Nada" />);
    expect(screen.queryByRole("table")).toBeNull();
    expect(screen.getByText("Nada")).toBeDefined();
  });

  it("keeps the table at desktop width", () => {
    mockViewport(1440);
    render(<DataTable data={data} columns={columns} keyExtractor={(r) => r.id} />);
    expect(screen.getByRole("table")).toBeDefined();
    expect(screen.queryByTestId("data-table-cards")).toBeNull();
    expect(screen.getByText("x1")).toBeDefined();
  });
});
