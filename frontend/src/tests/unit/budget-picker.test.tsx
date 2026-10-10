/**
 * Budget dropdowns load one budget page or a server search, never size=1000
 * (#1340, CU02/CU15). The management and payment forms listed only the first
 * 1000 budgets (the dev DB has ~1300), so newer budgets could not be chosen.
 */
import { describe, it, expect, vi, beforeEach } from "vitest";
import { render, screen, waitFor, fireEvent } from "@testing-library/react";
import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import { readFileSync, readdirSync, statSync } from "node:fs";
import { join } from "node:path";
import { useState, type ReactNode } from "react";

const mockFetch = vi.fn();
vi.stubGlobal("fetch", mockFetch);
vi.mock("next-intl", () => ({
  useTranslations: (ns: string) => (key: string) => `${ns}.${key}`,
}));

import { BudgetPicker } from "@/components/shared/BudgetPicker";
import type { Presupuesto } from "@/types";

const NEWEST: Presupuesto[] = [
  { idBudget: 1300, propertyAmount: 1000, person: { personId: 1500, name: "Ana", lastName: "Nueva" } },
  { idBudget: 1299, propertyAmount: 2000, person: { personId: 1499, name: "Bruno", lastName: "Reciente" } },
];
const FAR: Presupuesto = { idBudget: 12, propertyAmount: 5000, person: { personId: 7, name: "Carla", lastName: "Garcia" } };
const FAR2: Presupuesto = { idBudget: 40, propertyAmount: 7000, person: { personId: 7, name: "Carla", lastName: "Garcia" } };

function json(body: unknown, status = 200) {
  const text = JSON.stringify(body);
  return Promise.resolve({ ok: status < 400, status, text: () => Promise.resolve(text) } as Response);
}

function route(url: string) {
  if (/\/presupuestos\?page=0&size=20&sort=idBudget%2Cdesc$/.test(url)) {
    return json({ content: NEWEST, number: 0, size: 20, totalElements: 1300, totalPages: 65 });
  }
  if (url.includes("/people/search")) {
    const q = new URL(url, "http://x").searchParams;
    if (q.get("identificationNumber") === "30111222") return json([{ personId: 7, firstName: "Carla", lastName: "Garcia" }]);
    if (q.get("lastName")?.toLowerCase().startsWith("gar")) return json([{ personId: 7, firstName: "Carla", lastName: "Garcia" }]);
    return json([]);
  }
  if (/\/presupuestos\/persona\/7$/.test(url)) return json([FAR, FAR2]);
  if (/\/presupuestos\/12$/.test(url)) return json(FAR);
  return json({ message: "not found" }, 404);
}

function wrapper({ children }: { children: ReactNode }) {
  const client = new QueryClient({ defaultOptions: { queries: { retry: false } } });
  return <QueryClientProvider client={client}>{children}</QueryClientProvider>;
}

function Harness({ initial, onPick }: { initial?: number; onPick?: (b?: Presupuesto) => void }) {
  const [id, setId] = useState<number | undefined>(initial);
  return (
    <BudgetPicker
      value={id}
      onChange={(b) => {
        setId(b?.idBudget);
        onPick?.(b);
      }}
      aria-label="Presupuesto"
      data-testid="picker"
    />
  );
}

const urls = () => mockFetch.mock.calls.map((c) => String(c[0]));

describe("BudgetPicker (#1340)", () => {
  beforeEach(() => {
    mockFetch.mockReset();
    mockFetch.mockImplementation((u: string) => route(String(u)));
  });

  it("is an ARIA combobox that opens with the newest budgets and never asks for size=1000", async () => {
    render(<Harness />, { wrapper });
    const box = screen.getByRole("combobox", { name: "Presupuesto" });
    fireEvent.focus(box);
    expect(box).toHaveAttribute("aria-expanded", "false");
    fireEvent.click(box);
    const listbox = await screen.findByRole("listbox");
    expect(box).toHaveAttribute("aria-controls", listbox.id);
    expect(await screen.findByRole("option", { name: "#1300 — Ana Nueva" })).toBeInTheDocument();
    expect(screen.getByRole("status")).toHaveTextContent("budgetPicker.recentHint");
    expect(urls().some((u) => u.includes("size=1000"))).toBe(false);
  });

  it("finds every budget of a client searched by name", async () => {
    render(<Harness />, { wrapper });
    const box = screen.getByRole("combobox", { name: "Presupuesto" });
    fireEvent.click(box);
    fireEvent.change(box, { target: { value: "Gar" } });
    expect(await screen.findByRole("option", { name: "#40 — Carla Garcia" })).toBeInTheDocument();
    expect(screen.getByRole("option", { name: "#12 — Carla Garcia" })).toBeInTheDocument();
    expect(urls().some((u) => /\/presupuestos\/persona\/7$/.test(u))).toBe(true);
  });

  it("finds a budget by its number and a client by document number", async () => {
    render(<Harness />, { wrapper });
    const box = screen.getByRole("combobox", { name: "Presupuesto" });
    fireEvent.click(box);
    fireEvent.change(box, { target: { value: "12" } });
    expect(await screen.findByRole("option", { name: "#12 — Carla Garcia" })).toBeInTheDocument();
    fireEvent.change(box, { target: { value: "30111222" } });
    expect(await screen.findByRole("option", { name: "#40 — Carla Garcia" })).toBeInTheDocument();
  });

  it("selects with the keyboard and never acts on a stale list while typing", async () => {
    const onPick = vi.fn();
    render(<Harness onPick={onPick} />, { wrapper });
    const box = screen.getByRole("combobox", { name: "Presupuesto" });
    fireEvent.click(box);
    await screen.findByRole("option", { name: "#1300 — Ana Nueva" });
    fireEvent.change(box, { target: { value: "Gar" } });
    expect(screen.queryByRole("option", { name: "#1300 — Ana Nueva" })).not.toBeInTheDocument();
    fireEvent.keyDown(box, { key: "ArrowDown" });
    fireEvent.keyDown(box, { key: "Enter" });
    expect(onPick).not.toHaveBeenCalled();
    await screen.findByRole("option", { name: "#40 — Carla Garcia" });
    fireEvent.keyDown(box, { key: "ArrowDown" });
    fireEvent.keyDown(box, { key: "Enter" });
    expect(onPick).toHaveBeenCalledWith(expect.objectContaining({ idBudget: 40 }));
    expect(box).toHaveValue("#40 — Carla Garcia");
    expect(box).toHaveAttribute("aria-expanded", "false");
  });

  it("shows the current budget of a form by loading it by id", async () => {
    render(<Harness initial={12} />, { wrapper });
    await waitFor(() => expect(screen.getByRole("combobox", { name: "Presupuesto" })).toHaveValue("#12 — Carla Garcia"));
  });

  it("announces when nothing matches", async () => {
    render(<Harness />, { wrapper });
    const box = screen.getByRole("combobox", { name: "Presupuesto" });
    fireEvent.click(box);
    fireEvent.change(box, { target: { value: "zzz" } });
    expect(await screen.findByText("budgetPicker.noResults")).toBeInTheDocument();
  });
});

describe("no screen loads every budget (#1340)", () => {
  function files(dir: string): string[] {
    return readdirSync(dir).flatMap((f) => {
      const p = join(dir, f);
      return statSync(p).isDirectory() ? files(p) : /\.tsx?$/.test(f) ? [p] : [];
    });
  }
  it("no page or component calls usePresupuestos() (the size=1000 list)", () => {
    const root = join(__dirname, "..", "..");
    const offenders = [...files(join(root, "app")), ...files(join(root, "components"))].filter((f) =>
      /\busePresupuestos\(\)/.test(readFileSync(f, "utf8")),
    );
    expect(offenders).toEqual([]);
  });
});
