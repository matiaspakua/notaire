/**
 * Budgets and deeds lists read one server page at a time (#1340 slice 5, CU01/CU60, CU06/CU07).
 * Both requested size=1000; their filters also sent query parameters the backend ignores
 * (`estado` instead of `status`, `number` instead of `number`), so they returned every row.
 */
import { describe, it, expect, vi, beforeEach } from "vitest";
import { render, screen, waitFor, renderHook, fireEvent } from "@testing-library/react";
import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import type { ReactNode } from "react";

const mockFetch = vi.fn();
vi.stubGlobal("fetch", mockFetch);

let mockSearchParams = new URLSearchParams();
const mockReplace = vi.fn();
let mockPathname = "/dashboard/budgets";
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

import { useBudgetsPage } from "@/hooks/useBudgets";
import { useDeedsPage } from "@/hooks/useDeeds";
import BudgetsPage from "@/app/dashboard/budgets/page";
import DeedsPage from "@/app/dashboard/deeds/page";

const BUDGET = { idBudget: 1192, date: "2026-10-01", status: "BORRADOR", propertyAmount: 1000 };
const DEED = { idDeed: 845, number: 77001, status: "Borrador" };

function json(body: unknown) {
  const text = JSON.stringify(body);
  return Promise.resolve({ ok: true, status: 200, text: () => Promise.resolve(text) } as Response);
}
const EMPTY_PAGE = { content: [], number: 0, size: 20, totalElements: 0, totalPages: 0 };

function route(list: RegExp, page: object) {
  return (u: string) => {
    const url = String(u);
    if (list.test(url)) return json(page);
    if (/\/budgets\/\d+$/.test(url)) return json(BUDGET);
    if (/\/(buscar)\?/.test(url)) return json([]);
    if (/\?|size=/.test(url)) return json(EMPTY_PAGE);
    return json([]);
  };
}
function wrapper({ children }: { children: ReactNode }) {
  const client = new QueryClient({ defaultOptions: { queries: { retry: false } } });
  return <QueryClientProvider client={client}>{children}</QueryClientProvider>;
}
const urls = () => mockFetch.mock.calls.map((c) => String(c[0]));

beforeEach(() => {
  mockFetch.mockReset();
  mockReplace.mockReset();
  mockSearchParams = new URLSearchParams();
});

describe("paged hooks (#1340)", () => {
  it("useBudgetsPage requests one page, newest first", async () => {
    mockFetch.mockImplementation(route(/\/budgets\?/, { content: [BUDGET], number: 1, size: 50, totalElements: 1192, totalPages: 24 }));
    const { result } = renderHook(() => useBudgetsPage({ page: 1, size: 50 }), { wrapper });
    await waitFor(() => expect(result.current.isSuccess).toBe(true));
    expect(urls()[0]).toMatch(/\/budgets\?page=1&size=50&sort=idBudget%2Cdesc$/);
    expect(result.current.data?.totalElements).toBe(1192);
  });

  it("useDeedsPage requests one page, newest first", async () => {
    mockFetch.mockImplementation(route(/\/deeds\?/, { content: [DEED], number: 1, size: 50, totalElements: 845, totalPages: 17 }));
    const { result } = renderHook(() => useDeedsPage({ page: 1, size: 50 }), { wrapper });
    await waitFor(() => expect(result.current.isSuccess).toBe(true));
    expect(urls()[0]).toMatch(/\/deeds\?page=1&size=50&sort=idDeed%2Cdesc$/);
  });
});

describe("BudgetsPage (#1340)", () => {
  beforeEach(() => { mockPathname = "/dashboard/budgets"; });

  it("renders the server page with the footer and never asks for size=1000", async () => {
    mockFetch.mockImplementation(route(/\/budgets\?page=/, { content: [BUDGET], number: 0, size: 20, totalElements: 1192, totalPages: 60 }));
    render(<BudgetsPage />, { wrapper });
    expect(await screen.findByTestId("btn-items-budget-1192")).toBeInTheDocument();
    expect(screen.getByRole("navigation")).toBeInTheDocument();
    expect(urls().some((u) => /\/budgets\?page=0&size=20&sort=idBudget%2Cdesc$/.test(u))).toBe(true);
    expect(urls().filter((u) => /\/budgets\?/.test(u)).some((u) => u.includes("size=1000"))).toBe(false);
  });

  it("finds a budget by number outside the loaded page", async () => {
    mockFetch.mockImplementation(route(/\/budgets\?page=/, { content: [], number: 0, size: 20, totalElements: 1192, totalPages: 60 }));
    render(<BudgetsPage />, { wrapper });
    fireEvent.change(await screen.findByTestId("input-search-budget"), { target: { value: "1192" } });
    expect(await screen.findByTestId("btn-items-budget-1192")).toBeInTheDocument();
    expect(urls().some((u) => /\/budgets\/1192$/.test(u))).toBe(true);
  });

  it("clamps a page past the end", async () => {
    mockSearchParams = new URLSearchParams("page=99");
    mockFetch.mockImplementation(route(/\/budgets\?page=/, { content: [], number: 99, size: 20, totalElements: 1192, totalPages: 60 }));
    render(<BudgetsPage />, { wrapper });
    await waitFor(() => expect(mockReplace).toHaveBeenCalledWith("/dashboard/budgets?page=59", { scroll: false }));
  });
});

describe("DeedsPage (#1340)", () => {
  beforeEach(() => { mockPathname = "/dashboard/deeds"; });

  it("renders the server page with the footer and never asks for size=1000", async () => {
    mockFetch.mockImplementation(route(/\/deeds\?page=/, { content: [DEED], number: 0, size: 20, totalElements: 845, totalPages: 43 }));
    render(<DeedsPage />, { wrapper });
    expect(await screen.findByText("77001")).toBeInTheDocument();
    expect(screen.getByRole("navigation")).toBeInTheDocument();
    expect(urls().some((u) => /\/deeds\?page=0&size=20&sort=idDeed%2Cdesc$/.test(u))).toBe(true);
    expect(urls().filter((u) => /\/deeds\?/.test(u)).some((u) => u.includes("size=1000"))).toBe(false);
  });

  it("searches by number with the parameter the backend reads", async () => {
    mockFetch.mockImplementation(route(/\/deeds\?page=/, { content: [DEED], number: 0, size: 20, totalElements: 845, totalPages: 43 }));
    render(<DeedsPage />, { wrapper });
    fireEvent.change(await screen.findByTestId("input-search-deed"), { target: { value: "77001" } });
    await waitFor(() => expect(urls().some((u) => /\/deeds\/search\?number=77001$/.test(u))).toBe(true));
    expect(urls().some((u) => u.includes("numero="))).toBe(false);
  });
});
