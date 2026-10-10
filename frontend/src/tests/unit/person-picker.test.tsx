/**
 * Person dropdowns load one person page or a server search, never size=1000
 * (#1340 slice 3, CU02/CU19/CU39). The budget and management forms used to
 * list only the first 1000 people, so anyone created later could not be chosen.
 */
import { describe, it, expect, vi, beforeEach } from "vitest";
import { render, screen, waitFor, fireEvent, act } from "@testing-library/react";
import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import { readFileSync, readdirSync, statSync } from "node:fs";
import { join } from "node:path";
import { useState, type ReactNode } from "react";

const mockFetch = vi.fn();
vi.stubGlobal("fetch", mockFetch);
vi.mock("next-intl", () => ({
  useTranslations: (ns: string) => (key: string) => `${ns}.${key}`,
}));

import { PersonPicker } from "@/components/shared/PersonPicker";
import { buildPersonSearchQueries } from "@/hooks/usePersonSearch";
import type { Persona } from "@/types";

const NEWEST: Persona[] = [
  { personId: 1500, firstName: "Ana", lastName: "Nueva", identificationNumber: "111", isClient: true },
  { personId: 1499, firstName: "Bruno", lastName: "Reciente", identificationNumber: "222", isClient: false },
];
const FAR: Persona = { personId: 7, firstName: "Carla", lastName: "Garcia", identificationNumber: "30111222", isClient: true };

function json(body: unknown, status = 200) {
  const text = JSON.stringify(body);
  return Promise.resolve({ ok: status < 400, status, text: () => Promise.resolve(text) } as Response);
}

function route(url: string) {
  if (/\/people\?page=0&size=20&sort=idPerson%2Cdesc$/.test(url)) {
    return json({ content: NEWEST, number: 0, size: 20, totalElements: 1500, totalPages: 75 });
  }
  if (url.includes("/people/search")) {
    const q = new URL(url, "http://x").searchParams;
    if (q.get("identificationNumber") === "30111222") return json([FAR]);
    if (q.get("lastName")?.toLowerCase().startsWith("gar") || q.get("firstName")?.toLowerCase() === "carla") return json([FAR]);
    return json([]);
  }
  if (/\/people\/7$/.test(url)) return json(FAR);
  return json({}, 404);
}

function wrapper({ children }: { children: ReactNode }) {
  const client = new QueryClient({ defaultOptions: { queries: { retry: false } } });
  return <QueryClientProvider client={client}>{children}</QueryClientProvider>;
}

function Harness({ initial, clientsOnly, onPick }: { initial?: number; clientsOnly?: boolean; onPick?: (p?: Persona) => void }) {
  const [id, setId] = useState<number | undefined>(initial);
  return (
    <PersonPicker
      value={id}
      onChange={(p) => {
        setId(p?.personId);
        onPick?.(p);
      }}
      aria-label="Cliente"
      clientsOnly={clientsOnly}
      allowClear
      data-testid="picker"
    />
  );
}

const urls = () => mockFetch.mock.calls.map((c) => String(c[0]));

describe("buildPersonSearchQueries (#1340)", () => {
  it("searches a document number by identificationNumber", () => {
    expect(buildPersonSearchQueries("30111222")).toEqual([{ identificationNumber: "30111222" }]);
  });
  it("searches one word in the first and in the last name", () => {
    expect(buildPersonSearchQueries(" Gar ")).toEqual([{ firstName: "Gar" }, { lastName: "Gar" }]);
  });
  it("splits a full name into first and last name, and also tries it as a last name", () => {
    expect(buildPersonSearchQueries("Ana Tutorial 886798")).toEqual([
      { firstName: "Ana", lastName: "Tutorial 886798" },
      { lastName: "Ana Tutorial 886798" },
    ]);
  });
  it("does not search an empty query", () => {
    expect(buildPersonSearchQueries("  ")).toEqual([]);
  });
});

describe("PersonPicker (#1340)", () => {
  beforeEach(() => {
    mockFetch.mockReset();
    mockFetch.mockImplementation((u: string) => route(String(u)));
  });

  it("is an ARIA combobox that opens with the newest people and never asks for size=1000", async () => {
    render(<Harness />, { wrapper });
    const box = screen.getByRole("combobox", { name: "Cliente" });
    expect(box).toHaveAttribute("aria-expanded", "false");
    fireEvent.focus(box);
    expect(box).toHaveAttribute("aria-expanded", "false"); // focus alone does not open it
    fireEvent.click(box);
    expect(box).toHaveAttribute("aria-expanded", "true");
    const listbox = await screen.findByRole("listbox");
    expect(box).toHaveAttribute("aria-controls", listbox.id);
    expect(await screen.findByRole("option", { name: "Ana Nueva" })).toBeInTheDocument();
    expect(urls().some((u) => u.includes("size=1000"))).toBe(false);
  });

  it("searches the server after a short pause and lists the match", async () => {
    render(<Harness />, { wrapper });
    const box = screen.getByRole("combobox", { name: "Cliente" });
    fireEvent.click(box);
    fireEvent.change(box, { target: { value: "Gar" } });
    expect(await screen.findByRole("option", { name: "Carla Garcia" })).toBeInTheDocument();
    const search = urls().filter((u) => u.includes("/people/search"));
    expect(search.some((u) => u.endsWith("lastName=Gar"))).toBe(true);
    expect(search.some((u) => u.endsWith("firstName=Gar"))).toBe(true);
    // debounced: no request per keystroke for the intermediate values
    expect(search.some((u) => /Name=G$|Name=Ga$/.test(u))).toBe(false);
  });

  it("hides the newest people as soon as the user types, so Enter cannot pick a stale option", async () => {
    const onPick = vi.fn();
    render(<Harness onPick={onPick} />, { wrapper });
    const box = screen.getByRole("combobox", { name: "Cliente" });
    fireEvent.click(box);
    await screen.findByRole("option", { name: "Ana Nueva" });
    fireEvent.change(box, { target: { value: "Gar" } });
    expect(screen.queryByRole("option", { name: "Ana Nueva" })).not.toBeInTheDocument();
    expect(screen.getByRole("status")).toHaveTextContent("personPicker.searching");
    fireEvent.keyDown(box, { key: "ArrowDown" });
    fireEvent.keyDown(box, { key: "Enter" });
    expect(onPick).not.toHaveBeenCalled();
    expect(await screen.findByRole("option", { name: "Carla Garcia" })).toBeInTheDocument();
  });

  it("selects with the keyboard and closes on Escape without bubbling", async () => {
    const onPick = vi.fn();
    render(<Harness onPick={onPick} />, { wrapper });
    const box = screen.getByRole("combobox", { name: "Cliente" });
    fireEvent.click(box);
    await screen.findByRole("option", { name: "Ana Nueva" });
    fireEvent.keyDown(box, { key: "ArrowDown" });
    fireEvent.keyDown(box, { key: "ArrowDown" });
    const active = box.getAttribute("aria-activedescendant");
    expect(active).toBe(screen.getByRole("option", { name: "Bruno Reciente" }).id);
    fireEvent.keyDown(box, { key: "Enter" });
    expect(onPick).toHaveBeenCalledWith(expect.objectContaining({ personId: 1499 }));
    expect(box).toHaveAttribute("aria-expanded", "false");
    expect(box).toHaveValue("Bruno Reciente");

    fireEvent.keyDown(box, { key: "ArrowDown" });
    expect(box).toHaveAttribute("aria-expanded", "true");
    const parentKeyDown = vi.fn();
    box.parentElement!.parentElement!.addEventListener("keydown", parentKeyDown);
    fireEvent.keyDown(box, { key: "Escape" });
    expect(box).toHaveAttribute("aria-expanded", "false");
    expect(parentKeyDown).not.toHaveBeenCalled();
  });

  it("shows the current person of an edit form by loading it by id", async () => {
    render(<Harness initial={7} />, { wrapper });
    const box = screen.getByRole("combobox", { name: "Cliente" });
    await waitFor(() => expect(box).toHaveValue("Carla Garcia"));
    expect(urls().some((u) => /\/people\/7$/.test(u))).toBe(true);
  });

  it("stays closed after a mouse pick inside a form <label>", async () => {
    render(<label><span>Cliente</span><Harness /></label>, { wrapper });
    const box = screen.getByRole("combobox", { name: "Cliente" });
    fireEvent.click(box);
    fireEvent.click(await screen.findByRole("option", { name: "Ana Nueva" }));
    expect(box).toHaveValue("Ana Nueva");
    expect(box).toHaveAttribute("aria-expanded", "false");
  });

  it("lists only clients when clientsOnly is set", async () => {
    render(<Harness clientsOnly />, { wrapper });
    fireEvent.click(screen.getByRole("combobox", { name: "Cliente" }));
    expect(await screen.findByRole("option", { name: "Ana Nueva" })).toBeInTheDocument();
    expect(screen.queryByRole("option", { name: "Bruno Reciente" })).not.toBeInTheDocument();
  });

  it("announces when nothing matches and can clear the selection", async () => {
    const onPick = vi.fn();
    render(<Harness initial={7} onPick={onPick} />, { wrapper });
    const box = screen.getByRole("combobox", { name: "Cliente" });
    await waitFor(() => expect(box).toHaveValue("Carla Garcia"));
    fireEvent.click(screen.getByRole("button", { name: "personPicker.clear" }));
    expect(onPick).toHaveBeenCalledWith(undefined);
    fireEvent.click(box);
    fireEvent.change(box, { target: { value: "zzz" } });
    expect(await screen.findByText("personPicker.noResults")).toBeInTheDocument();
    expect(screen.getByRole("status")).toBeInTheDocument();
  });
});

describe("no screen loads every person (#1340)", () => {
  function files(dir: string): string[] {
    return readdirSync(dir).flatMap((f) => {
      const p = join(dir, f);
      return statSync(p).isDirectory() ? files(p) : /\.tsx?$/.test(f) ? [p] : [];
    });
  }
  it("no page or component calls usePersonas() (the size=1000 list)", () => {
    const root = join(__dirname, "..", "..");
    const offenders = [...files(join(root, "app")), ...files(join(root, "components"))].filter((f) =>
      /\busePersonas\(\)/.test(readFileSync(f, "utf8")),
    );
    expect(offenders).toEqual([]);
  });
});

void act;
