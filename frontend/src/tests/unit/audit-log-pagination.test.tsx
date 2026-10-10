/**
 * The audit log is read one server page at a time (#1340, RF-44).
 */
import { describe, it, expect, vi, beforeEach } from "vitest";
import { renderHook, waitFor } from "@testing-library/react";
import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import type { ReactNode } from "react";

const mockFetch = vi.fn();
vi.stubGlobal("fetch", mockFetch);

import { apiGetPage } from "@/lib/api-client";
import { useAuditoria } from "@/hooks/useAuditoria";

function pageResponse(body: unknown) {
  const json = JSON.stringify(body);
  return Promise.resolve({ ok: true, status: 200, text: () => Promise.resolve(json) } as Response);
}

function wrapper({ children }: { children: ReactNode }) {
  const client = new QueryClient({ defaultOptions: { queries: { retry: false } } });
  return <QueryClientProvider client={client}>{children}</QueryClientProvider>;
}

describe("apiGetPage (#1340)", () => {
  beforeEach(() => mockFetch.mockReset());

  it("sends page, size and sort and keeps the total", async () => {
    mockFetch.mockReturnValueOnce(
      pageResponse({ content: [{ id: 1 }], number: 2, size: 50, totalElements: 1384, totalPages: 28 }),
    );
    const page = await apiGetPage<{ id: number }>("/people", { page: 2, size: 50, sort: "lastName,asc" });
    expect(mockFetch.mock.calls[0][0]).toMatch(/\/people\?page=2&size=50&sort=lastName%2Casc$/);
    expect(page).toEqual({ content: [{ id: 1 }], number: 2, size: 50, totalElements: 1384, totalPages: 28 });
  });

  it("normalises the audit-log page shape (page instead of number) and adds filters", async () => {
    mockFetch.mockReturnValueOnce(
      pageResponse({ content: [], page: 3, size: 20, totalElements: 61, totalPages: 4, first: false, last: true }),
    );
    const page = await apiGetPage("/audit-log", { page: 3, size: 20, params: { module: "People", empty: "" } });
    expect(mockFetch.mock.calls[0][0]).toMatch(/\/audit-log\?page=3&size=20&module=People$/);
    expect(page.number).toBe(3);
    expect(page.totalElements).toBe(61);
  });
});

describe("useAuditoria (#1340)", () => {
  beforeEach(() => mockFetch.mockReset());

  it("requests one sorted page with the module filter instead of size=1000", async () => {
    mockFetch.mockReturnValue(
      pageResponse({ content: [{ idAuditRecord: 9 }], page: 1, size: 50, totalElements: 120, totalPages: 3 }),
    );
    const { result } = renderHook(() => useAuditoria({ page: 1, size: 50, module: "People" }), { wrapper });
    await waitFor(() => expect(result.current.isSuccess).toBe(true));
    const url = String(mockFetch.mock.calls[0][0]);
    expect(url).toMatch(/\/audit-log\?page=1&size=50&sort=date%2Cdesc&module=People$/);
    expect(url).not.toContain("size=1000");
    expect(result.current.data?.totalElements).toBe(120);
    expect(result.current.data?.content).toHaveLength(1);
  });
});
