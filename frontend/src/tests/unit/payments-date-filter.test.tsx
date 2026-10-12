/**
 * #1557 / CU47: the payments list filters by an inclusive date range (?from=&to=),
 * server-side via GET /payments/date, client-side when a budget is also selected.
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
  usePathname: () => "/dashboard/payments",
  useSearchParams: () => mockSearchParams,
}));
vi.mock("next-intl", () => ({
  useTranslations: (ns: string) => (key: string) => `${ns}.${key}`,
  useLocale: () => "es",
}));
vi.mock("sonner", () => ({ toast: { error: vi.fn(), success: vi.fn() } }));
vi.mock("@/components/shared/BudgetPicker", () => ({
  budgetLabel: (b: { idBudget: number }) => `#${b.idBudget}`,
  BudgetPicker: () => null,
}));

import { usePaymentsByDate } from "@/hooks/usePayments";
import PaymentsPage from "@/app/dashboard/payments/page";

function ok(body: unknown) {
  const json = JSON.stringify(body);
  return Promise.resolve({ ok: true, status: 200, text: () => Promise.resolve(json) } as Response);
}
function wrapper({ children }: { children: ReactNode }) {
  const client = new QueryClient({ defaultOptions: { queries: { retry: false } } });
  return <QueryClientProvider client={client}>{children}</QueryClientProvider>;
}
const ALL = [{ idPayment: 1, idBudget: 3, amount: 10, date: "2026-01-01", paymentMethod: "AllRows" }];
const DATED = [{ idPayment: 2, idBudget: 3, amount: 20, date: "2026-03-02", paymentMethod: "DatedRow" }];
const BY7 = [
  { idPayment: 5, idBudget: 7, amount: 30, date: "2026-03-01", paymentMethod: "InRange" },
  { idPayment: 6, idBudget: 7, amount: 40, date: "2026-05-01", paymentMethod: "OutOfRange" },
  // A server instant for local midnight of the "from" day (server zone UTC+1): still that day.
  { idPayment: 8, idBudget: 7, amount: 50, date: "2026-02-28T23:00:00.000+00:00", paymentMethod: "BoundaryInstant" },
];

const urls = () => mockFetch.mock.calls.map((c) => String(c[0]));

beforeEach(() => {
  mockFetch.mockReset();
  mockFetch.mockImplementation((u: unknown) => {
    const url = String(u);
    if (url.includes("/payments/date")) return ok(DATED);
    if (/\/payments\/budget\/7$/.test(url)) return ok(BY7);
    if (/\/payments$/.test(url)) return ok(ALL);
    return ok({});
  });
  mockReplace.mockReset();
  mockSearchParams = new URLSearchParams();
});

describe("usePaymentsByDate (#1557)", () => {
  it("calls GET /payments/date with startDate and endDate", async () => {
    const { result } = renderHook(() => usePaymentsByDate("2026-03-01", "2026-03-31"), { wrapper });
    await waitFor(() => expect(result.current.isSuccess).toBe(true));
    expect(urls()[0]).toMatch(/\/payments\/date\?startDate=2026-03-01&endDate=2026-03-31$/);
  });
  it("does not fetch without both dates", () => {
    renderHook(() => usePaymentsByDate("2026-03-01", ""), { wrapper });
    expect(mockFetch).not.toHaveBeenCalled();
  });
});

describe("PaymentsPage date filter (#1557)", () => {
  it("with ?from=&to= lists the server's date-range result", async () => {
    mockSearchParams = new URLSearchParams("from=2026-03-01&to=2026-03-31");
    render(<PaymentsPage />, { wrapper });
    expect((await screen.findAllByText("DatedRow")).length).toBeGreaterThan(0);
    expect(screen.queryAllByText("AllRows")).toHaveLength(0);
  });

  it("with a budget, narrows its payments to the range on the client (inclusive)", async () => {
    mockSearchParams = new URLSearchParams("budget=7&from=2026-03-01&to=2026-03-31");
    render(<PaymentsPage />, { wrapper });
    expect((await screen.findAllByText("InRange")).length).toBeGreaterThan(0);
    expect(screen.getAllByText("BoundaryInstant").length).toBeGreaterThan(0);
    expect(screen.queryAllByText("OutOfRange")).toHaveLength(0);
    expect(urls().some((u) => u.includes("/payments/date"))).toBe(false);
  });

  it("an inverted range shows an inline error and sends no request", async () => {
    mockSearchParams = new URLSearchParams("from=2026-03-31&to=2026-03-01");
    render(<PaymentsPage />, { wrapper });
    expect(await screen.findByTestId("payments-range-error")).toHaveTextContent("pagos.consulta.rangoInvalido");
    expect(urls().some((u) => u.includes("/payments/date"))).toBe(false);
    // No misleading unfiltered list while the range is invalid.
    expect(screen.queryAllByText("AllRows")).toHaveLength(0);
  });

  it("writes the dates to the URL", async () => {
    render(<PaymentsPage />, { wrapper });
    fireEvent.change(screen.getByTestId("input-payments-from"), { target: { value: "2026-03-01" } });
    expect(mockReplace).toHaveBeenLastCalledWith("/dashboard/payments?from=2026-03-01", { scroll: false });
  });
});
