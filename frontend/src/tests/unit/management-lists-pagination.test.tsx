/**
 * Screens that list or pick managements never load GET /managements?size=1000
 * (#1340 slice 7, CU31/CU32/CU84). The dev DB has more than 1000 managements, so
 * the external-entity documents and re-entry lists and the document form's
 * management select silently missed every case after the 1000th.
 */
import { describe, it, expect, vi, beforeEach } from "vitest";
import { render, screen, waitFor, fireEvent } from "@testing-library/react";
import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import { useState, type ReactNode } from "react";

const mockFetch = vi.fn();
vi.stubGlobal("fetch", mockFetch);

let mockSearchParams = new URLSearchParams();
let mockPathname = "/dashboard/document-reentry";
const mockReplace = vi.fn();
vi.mock("next/navigation", () => ({
  useRouter: () => ({ push: vi.fn(), replace: mockReplace }),
  usePathname: () => mockPathname,
  useSearchParams: () => mockSearchParams,
}));
vi.mock("next-intl", () => ({
  useTranslations: (ns: string) => (key: string) => `${ns}.${key}`,
  useLocale: () => "es",
}));
vi.mock("sonner", () => ({ toast: { error: vi.fn(), success: vi.fn() } }));

import ReingresoDocumentacionPage from "@/app/dashboard/document-reentry/page";
import DocumentosEntidadesExternasPage from "@/app/dashboard/external-entity-documents/page";
import { ManagementPicker } from "@/components/shared/ManagementPicker";
import type { DeedManagement } from "@/types";

const NEWEST: DeedManagement = { idManagement: 1312, number: 98765, encabezado: "Compraventa Nueva", statusActual: "Iniciada" };
const OLD: DeedManagement = { idManagement: 12, number: 4321, encabezado: "Hipoteca Garcia", statusActual: "Iniciada" };
const OLD2: DeedManagement = { idManagement: 40, number: 5555, encabezado: "Donación Garcia", statusActual: "Iniciada" };

function json(body: unknown, status = 200) {
  const text = JSON.stringify(body);
  return Promise.resolve({ ok: status < 400, status, text: () => Promise.resolve(text) } as Response);
}

function route(page: object) {
  return (u: string) => {
    const url = String(u);
    if (/\/managements\?/.test(url)) return json(page);
    if (url.includes("/people/search")) {
      const q = new URL(url, "http://x").searchParams;
      if (q.get("identificationNumber") === "30111222") return json([{ personId: 7, firstName: "Carla", lastName: "Garcia" }]);
      if (q.get("lastName")?.toLowerCase().startsWith("gar")) return json([{ personId: 7, firstName: "Carla", lastName: "Garcia" }]);
      return json([]);
    }
    if (/\/managements\/client\/7$/.test(url)) return json([OLD, OLD2]);
    if (/\/managements\/number\/4321$/.test(url)) return json(OLD);
    if (/\/managements\/12$/.test(url)) return json(OLD);
    return json({ message: "not found" }, 404);
  };
}

function wrapper({ children }: { children: ReactNode }) {
  const client = new QueryClient({ defaultOptions: { queries: { retry: false } } });
  return <QueryClientProvider client={client}>{children}</QueryClientProvider>;
}
const urls = () => mockFetch.mock.calls.map((c) => String(c[0]));
const PAGE = { content: [NEWEST], number: 0, size: 20, totalElements: 1312, totalPages: 66 };

describe.each([
  ["reingreso-documentacion", ReingresoDocumentacionPage],
  ["documentos-entidades-externas", DocumentosEntidadesExternasPage],
])("%s list (#1340)", (path, Page) => {
  beforeEach(() => {
    mockFetch.mockReset();
    mockReplace.mockReset();
    mockSearchParams = new URLSearchParams();
    mockPathname = `/dashboard/${path}`;
  });

  it("renders one server page, newest first, with the pagination footer and never asks for size=1000", async () => {
    mockFetch.mockImplementation(route(PAGE));
    render(<Page />, { wrapper });
    expect(await screen.findByText("98765")).toBeInTheDocument();
    expect(screen.getByRole("navigation")).toBeInTheDocument();
    expect(urls().some((u) => /\/managements\?page=0&size=20&sort=idManagement%2Cdesc$/.test(u))).toBe(true);
    expect(urls().some((u) => u.includes("size=1000"))).toBe(false);
  });

  it("reads the page and size from the URL and clamps a page past the end", async () => {
    mockSearchParams = new URLSearchParams("page=99&size=50");
    mockFetch.mockImplementation(route({ content: [], number: 99, size: 50, totalElements: 1312, totalPages: 27 }));
    render(<Page />, { wrapper });
    await waitFor(() => expect(mockReplace).toHaveBeenCalledWith(`/dashboard/${path}?page=26&size=50`, { scroll: false }));
  });
});

function Harness({ initial, onPick }: { initial?: number; onPick?: (g?: DeedManagement) => void }) {
  const [id, setId] = useState<number | undefined>(initial);
  return (
    <ManagementPicker
      value={id}
      onChange={(g) => {
        setId(g?.idManagement);
        onPick?.(g);
      }}
      aria-label="Gestión"
      data-testid="picker"
    />
  );
}

describe("ManagementPicker (#1340)", () => {
  beforeEach(() => {
    mockFetch.mockReset();
    mockFetch.mockImplementation(route(PAGE));
  });

  it("is an ARIA combobox that opens with the newest managements and never asks for size=1000", async () => {
    render(<Harness />, { wrapper });
    const box = screen.getByRole("combobox", { name: "Gestión" });
    fireEvent.click(box);
    // The number leads the label, so it reads the same in es and en.
    expect(await screen.findByRole("option", { name: "98765 — Compraventa Nueva" })).toBeInTheDocument();
    expect(screen.getByRole("status")).toHaveTextContent("managementPicker.recentHint");
    expect(urls().some((u) => u.includes("size=1000"))).toBe(false);
  });

  it("finds a management by its number, and every management of a client by name or document", async () => {
    render(<Harness />, { wrapper });
    const box = screen.getByRole("combobox", { name: "Gestión" });
    fireEvent.click(box);
    fireEvent.change(box, { target: { value: "4321" } });
    expect(await screen.findByRole("option", { name: "4321 — Hipoteca Garcia" })).toBeInTheDocument();
    fireEvent.change(box, { target: { value: "Gar" } });
    expect(await screen.findByRole("option", { name: "5555 — Donación Garcia" })).toBeInTheDocument();
    expect(screen.getByRole("option", { name: "4321 — Hipoteca Garcia" })).toBeInTheDocument();
    fireEvent.change(box, { target: { value: "30111222" } });
    expect(await screen.findByRole("option", { name: "5555 — Donación Garcia" })).toBeInTheDocument();
  });

  it("picks an option and shows the current value loaded by id", async () => {
    const onPick = vi.fn();
    render(<Harness initial={12} onPick={onPick} />, { wrapper });
    const box = screen.getByRole("combobox", { name: "Gestión" });
    await waitFor(() => expect(box).toHaveValue("4321 — Hipoteca Garcia"));
    fireEvent.click(box);
    fireEvent.click(await screen.findByRole("option", { name: "98765 — Compraventa Nueva" }));
    expect(onPick).toHaveBeenCalledWith(expect.objectContaining({ idManagement: 1312 }));
  });
});
