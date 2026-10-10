/**
 * The managements list is read one server page at a time (#1340 slice 4, CU02/CU19).
 * It requested size=1000, so managements after the 1000th were unreachable and the
 * page rendered 1000 animated rows (leaving it timed out the TS-0070 tour).
 */
import { describe, it, expect, vi, beforeEach } from "vitest";
import { render, screen, waitFor, renderHook } from "@testing-library/react";
import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import type { ReactNode } from "react";

const mockFetch = vi.fn();
vi.stubGlobal("fetch", mockFetch);

let mockSearchParams = new URLSearchParams();
const mockReplace = vi.fn();
vi.mock("next/navigation", () => ({
  useRouter: () => ({ push: vi.fn(), replace: mockReplace }),
  usePathname: () => "/dashboard/gestiones",
  useSearchParams: () => mockSearchParams,
}));
vi.mock("next-intl", () => ({
  useTranslations: (ns: string) => (key: string) => `${ns}.${key}`,
  useLocale: () => "es",
}));
vi.mock("sonner", () => ({ toast: { error: vi.fn(), success: vi.fn() } }));

import { useGestionesPage } from "@/hooks/useGestiones";
import GestionesPage from "@/app/dashboard/gestiones/page";

const ROW = { idManagement: 1312, number: 98765, statusActual: "Iniciada", procedureCount: 1 };

function json(body: unknown) {
  const text = JSON.stringify(body);
  return Promise.resolve({ ok: true, status: 200, text: () => Promise.resolve(text) } as Response);
}
const EMPTY_PAGE = { content: [], number: 0, size: 20, totalElements: 0, totalPages: 0 };

function route(page: object) {
  return (u: string) => {
    const url = String(u);
    if (/\/gestiones\?/.test(url)) return json(page);
    if (/\?|size=/.test(url)) return json(EMPTY_PAGE);
    return json([]);
  };
}

function wrapper({ children }: { children: ReactNode }) {
  const client = new QueryClient({ defaultOptions: { queries: { retry: false } } });
  return <QueryClientProvider client={client}>{children}</QueryClientProvider>;
}
const gestionesUrls = () => mockFetch.mock.calls.map((c) => String(c[0])).filter((u) => /\/gestiones(\?|$)/.test(u));

describe("useGestionesPage (#1340)", () => {
  beforeEach(() => mockFetch.mockReset());

  it("requests one page, newest first, and keeps the total", async () => {
    mockFetch.mockImplementation(route({ content: [ROW], number: 2, size: 50, totalElements: 1312, totalPages: 27 }));
    const { result } = renderHook(() => useGestionesPage({ page: 2, size: 50 }), { wrapper });
    await waitFor(() => expect(result.current.isSuccess).toBe(true));
    expect(gestionesUrls()[0]).toMatch(/\/gestiones\?page=2&size=50&sort=idManagement%2Cdesc$/);
    expect(result.current.data?.totalElements).toBe(1312);
  });
});

describe("GestionesPage (#1340)", () => {
  beforeEach(() => {
    mockFetch.mockReset();
    mockReplace.mockReset();
    mockSearchParams = new URLSearchParams();
  });

  it("renders the server page with the pagination footer and never asks for size=1000", async () => {
    mockFetch.mockImplementation(route({ content: [ROW], number: 0, size: 20, totalElements: 1312, totalPages: 66 }));
    render(<GestionesPage />, { wrapper });
    expect(await screen.findByText("98765")).toBeInTheDocument();
    expect(screen.getByRole("navigation")).toBeInTheDocument();
    expect(gestionesUrls().some((u) => /\/gestiones\?page=0&size=20&sort=idManagement%2Cdesc$/.test(u))).toBe(true);
    expect(gestionesUrls().some((u) => u.includes("size=1000"))).toBe(false);
  });

  it("reads the page and size from the URL and clamps a page past the end", async () => {
    mockSearchParams = new URLSearchParams("page=99&size=50");
    mockFetch.mockImplementation(route({ content: [], number: 99, size: 50, totalElements: 1312, totalPages: 27 }));
    render(<GestionesPage />, { wrapper });
    await waitFor(() => expect(mockReplace).toHaveBeenCalledWith("/dashboard/gestiones?page=26&size=50", { scroll: false }));
    expect(gestionesUrls().some((u) => /page=99&size=50&sort=idManagement%2Cdesc$/.test(u))).toBe(true);
  });
});
