/**
 * Deed dropdowns load one page of signed deeds or a server search, never
 * size=1000 (#1340 slice 8b, CU07/CU08/CU16). Testimonies, registration drafts
 * and folios filtered a size=1000 list client-side, so once the table passes
 * 1000 deeds a newly signed deed could not be chosen.
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
  useTranslations: (ns: string) => (key: string, values?: Record<string, unknown>) =>
    values ? `${ns}.${key} ${Object.values(values).join(" ")}` : `${ns}.${key}`,
  useLocale: () => "es",
}));

import { DeedPicker } from "@/components/shared/DeedPicker";
import type { Deed } from "@/types";

const NEWEST: Deed[] = [
  { idDeed: 1500, number: 1500, status: "Firmada" },
  { idDeed: 1499, number: 1499, status: "Firmada" },
];
const OLD_SIGNED: Deed = { idDeed: 12, number: 77, status: "Firmada" };
const OLD_UNSIGNED: Deed = { idDeed: 13, number: 77, status: "Sin Firmar" };

function json(body: unknown, status = 200) {
  const text = JSON.stringify(body);
  return Promise.resolve({ ok: status < 400, status, text: () => Promise.resolve(text) } as Response);
}

function route(url: string) {
  if (/\/deeds\?page=0&size=20&sort=idDeed%2Cdesc&status=Firmada$/.test(url)) {
    return json({ content: NEWEST, number: 0, size: 20, totalElements: 1200, totalPages: 60 });
  }
  if (/\/deeds\/search\?number=77$/.test(url)) return json([OLD_SIGNED, OLD_UNSIGNED]);
  if (/\/deeds\/search\?number=\d+$/.test(url)) return json([]);
  if (/\/deeds\/12$/.test(url)) return json(OLD_SIGNED);
  return json({ message: "not found" }, 404);
}

function wrapper({ children }: { children: ReactNode }) {
  const client = new QueryClient({ defaultOptions: { queries: { retry: false } } });
  return <QueryClientProvider client={client}>{children}</QueryClientProvider>;
}

function Harness({
  initial,
  onPick,
  exclude,
}: {
  initial?: number;
  onPick?: (d?: Deed) => void;
  exclude?: (d: Deed) => boolean;
}) {
  const [id, setId] = useState<number | undefined>(initial);
  return (
    <DeedPicker
      value={id}
      onChange={(d) => {
        setId(d?.idDeed);
        onPick?.(d);
      }}
      exclude={exclude}
      aria-label="Deed"
      data-testid="picker"
    />
  );
}

const urls = () => mockFetch.mock.calls.map((c) => String(c[0]));

describe("DeedPicker (#1340)", () => {
  beforeEach(() => {
    mockFetch.mockReset();
    mockFetch.mockImplementation((u: string) => route(String(u)));
  });

  it("is an ARIA combobox that opens with the newest signed deeds and never asks for size=1000", async () => {
    render(<Harness />, { wrapper });
    const box = screen.getByRole("combobox", { name: "Deed" });
    fireEvent.click(box);
    const listbox = await screen.findByRole("listbox");
    expect(box).toHaveAttribute("aria-controls", listbox.id);
    expect(await screen.findByRole("option", { name: "deedPicker.option 1500" })).toBeInTheDocument();
    expect(screen.getByRole("status")).toHaveTextContent("deedPicker.recentHint");
    expect(urls().some((u) => u.includes("status=Firmada"))).toBe(true);
    expect(urls().some((u) => u.includes("size=1000"))).toBe(false);
  });

  it("finds an old deed by number on the server and lists only signed ones", async () => {
    const onPick = vi.fn();
    render(<Harness onPick={onPick} />, { wrapper });
    const box = screen.getByRole("combobox", { name: "Deed" });
    fireEvent.click(box);
    fireEvent.change(box, { target: { value: "77" } });
    const options = await screen.findAllByRole("option", { name: "deedPicker.option 77" });
    expect(options).toHaveLength(1);
    fireEvent.click(options[0]);
    expect(onPick).toHaveBeenCalledWith(expect.objectContaining({ idDeed: 12 }));
    expect(box).toHaveValue("deedPicker.option 77");
    expect(urls().some((u) => /\/deeds\/search\?number=77$/.test(u))).toBe(true);
  });

  it("hides excluded deeds (folio form: already linked to another folio)", async () => {
    render(<Harness exclude={(d) => d.idDeed === 1500} />, { wrapper });
    const box = screen.getByRole("combobox", { name: "Deed" });
    fireEvent.click(box);
    expect(await screen.findByRole("option", { name: "deedPicker.option 1499" })).toBeInTheDocument();
    expect(screen.queryByRole("option", { name: "deedPicker.option 1500" })).not.toBeInTheDocument();
  });

  it("shows the current deed of a form by loading it by id", async () => {
    render(<Harness initial={12} />, { wrapper });
    await waitFor(() => expect(screen.getByRole("combobox", { name: "Deed" })).toHaveValue("deedPicker.option 77"));
  });

  it("announces when nothing matches", async () => {
    render(<Harness />, { wrapper });
    const box = screen.getByRole("combobox", { name: "Deed" });
    fireEvent.click(box);
    fireEvent.change(box, { target: { value: "999999" } });
    expect(await screen.findByText("deedPicker.noResults")).toBeInTheDocument();
  });
});

describe("no screen loads every deed (#1340)", () => {
  function files(dir: string): string[] {
    return readdirSync(dir).flatMap((f) => {
      const p = join(dir, f);
      return statSync(p).isDirectory() ? files(p) : /\.tsx?$/.test(f) ? [p] : [];
    });
  }
  const root = join(__dirname, "..", "..");
  it("useDeeds() (the size=1000 list) is gone and no screen calls it", () => {
    const offenders = [...files(join(root, "app")), ...files(join(root, "components")), ...files(join(root, "hooks"))]
      .filter((f) => /\buseDeeds\(\)/.test(readFileSync(f, "utf8")));
    expect(offenders).toEqual([]);
  });
  it("the testimony, registration-draft and folio forms use the DeedPicker", () => {
    for (const page of ["testimonies", "registration-drafts", "administration/folios"]) {
      expect(readFileSync(join(root, "app", "dashboard", page, "page.tsx"), "utf8")).toMatch(/<DeedPicker\b/);
    }
  });
});
