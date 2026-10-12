/**
 * #1545 / CU23: the audit log filters by user and an inclusive date range,
 * server-side (GET /audit-log?idUser=&from=&to=), kept in the URL.
 */
import { describe, it, expect, vi, beforeEach } from "vitest";
import { render, screen, waitFor, renderHook, fireEvent } from "@testing-library/react";
import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import type { ReactNode } from "react";

const mockFetch = vi.fn();
vi.stubGlobal("fetch", mockFetch);

let mockSearchParams = new URLSearchParams();
const mockReplace = vi.fn();
vi.mock("next/navigation", () => ({
  useRouter: () => ({ push: vi.fn(), replace: mockReplace }),
  usePathname: () => "/dashboard/audit",
  useSearchParams: () => mockSearchParams,
}));
vi.mock("next-intl", () => ({
  useTranslations: (ns: string) => (key: string) => `${ns}.${key}`,
  useLocale: () => "es",
}));

import { useAudit } from "@/hooks/useAudit";
import AuditPage from "@/app/dashboard/audit/page";

function ok(body: unknown) {
  const json = JSON.stringify(body);
  return Promise.resolve({ ok: true, status: 200, text: () => Promise.resolve(json) } as Response);
}
function wrapper({ children }: { children: ReactNode }) {
  const client = new QueryClient({ defaultOptions: { queries: { retry: false } } });
  return <QueryClientProvider client={client}>{children}</QueryClientProvider>;
}
const PAGE = { content: [], page: 0, size: 20, totalElements: 0, totalPages: 0 };

beforeEach(() => {
  mockFetch.mockReset();
  mockFetch.mockImplementation((u: unknown) =>
    /\/users$/.test(String(u)) ? ok([{ idUser: 1, name: "admin" }, { idUser: 4, name: "ana" }]) : ok(PAGE),
  );
  mockReplace.mockReset();
  mockSearchParams = new URLSearchParams();
});

const auditUrls = () => mockFetch.mock.calls.map((c) => String(c[0])).filter((u) => u.includes("/audit-log"));

describe("useAudit user/date filters (#1545)", () => {
  it("sends idUser, from and to", async () => {
    const { result } = renderHook(
      () => useAudit({ page: 0, size: 20, idUser: 4, from: "2026-10-01", to: "2026-10-12" }),
      { wrapper },
    );
    await waitFor(() => expect(result.current.isSuccess).toBe(true));
    expect(auditUrls()[0]).toMatch(/\/audit-log\?page=0&size=20&sort=date%2Cdesc&idUser=4&from=2026-10-01&to=2026-10-12$/);
  });
});

describe("AuditPage user/date filters (#1545)", () => {
  it("maps the URL filters to the request", async () => {
    mockSearchParams = new URLSearchParams("user=4&from=2026-10-01&to=2026-10-12&module=People");
    render(<AuditPage />, { wrapper });
    await waitFor(() => expect(auditUrls().length).toBeGreaterThan(0));
    const url = auditUrls().at(-1)!;
    expect(url).toContain("module=People");
    expect(url).toContain("idUser=4");
    expect(url).toContain("from=2026-10-01");
    expect(url).toContain("to=2026-10-12");
  });

  it("changing a filter writes the URL and resets the page", async () => {
    mockSearchParams = new URLSearchParams("page=3");
    render(<AuditPage />, { wrapper });
    await screen.findByRole("option", { name: "ana" });
    fireEvent.change(screen.getByTestId("select-audit-user"), { target: { value: "4" } });
    expect(mockReplace).toHaveBeenLastCalledWith("/dashboard/audit?user=4", { scroll: false });
    fireEvent.change(screen.getByTestId("input-audit-from"), { target: { value: "2026-10-01" } });
    expect(mockReplace).toHaveBeenLastCalledWith("/dashboard/audit?page=3&from=2026-10-01".replace("page=3&", ""), { scroll: false });
  });

  it("from after to shows an inline error and sends no filtered request", async () => {
    mockSearchParams = new URLSearchParams("from=2026-10-12&to=2026-10-01");
    render(<AuditPage />, { wrapper });
    expect(await screen.findByTestId("audit-range-error")).toHaveTextContent("auditoria.rangoInvalido");
    expect(auditUrls().some((u) => u.includes("from="))).toBe(false);
  });
});
