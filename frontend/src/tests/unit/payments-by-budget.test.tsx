/**
 * #1544 / CU47: consult one budget's payments. The payments page gets a budget
 * picker kept in the URL (?budget=); with a budget selected it shows a summary
 * (total, paid, balance, status) and only that budget's payments.
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
  BudgetPicker: (p: { onChange: (b?: { idBudget: number }) => void; "data-testid"?: string }) => (
    <div>
      <button data-testid={`${p["data-testid"]}-pick-7`} onClick={() => p.onChange({ idBudget: 7 })}>pick</button>
      <button data-testid={`${p["data-testid"]}-clear`} onClick={() => p.onChange(undefined)}>clear</button>
    </div>
  ),
}));

import { usePaymentsByBudget, useBudgetBalance } from "@/hooks/usePayments";
import PaymentsPage from "@/app/dashboard/payments/page";

function ok(body: unknown) {
  const json = JSON.stringify(body);
  return Promise.resolve({ ok: true, status: 200, text: () => Promise.resolve(json) } as Response);
}
function wrapper({ children }: { children: ReactNode }) {
  const client = new QueryClient({ defaultOptions: { queries: { retry: false } } });
  return <QueryClientProvider client={client}>{children}</QueryClientProvider>;
}

const ALL = [
  { idPayment: 1, idBudget: 3, amount: 10, date: "2026-01-01", paymentMethod: "AllRowsMethod" },
  { idPayment: 2, idBudget: 7, amount: 20, date: "2026-01-02", paymentMethod: "Efectivo" },
];
const BY7 = [
  { idPayment: 2, idBudget: 7, amount: 20, date: "2026-01-02", paymentMethod: "Efectivo" },
  { idPayment: 5, idBudget: 7, amount: 30, date: "2026-03-02", paymentMethod: "Transferencia" },
];

function route(url: string) {
  if (/\/payments\/budget\/7\/balance$/.test(url)) return ok(50);
  if (/\/payments\/budget\/7\/status$/.test(url)) return ok("PARTIAL");
  if (/\/payments\/budget\/7$/.test(url)) return ok(BY7);
  if (/\/payments\/budget\/9$/.test(url)) return ok([]);
  if (/\/payments\/budget\/9\/balance$/.test(url)) return ok(100);
  if (/\/payments\/budget\/9\/status$/.test(url)) return ok("NoPayments");
  if (/\/budgets\/(7|9)\/summary$/.test(url)) return ok({ total: 100, pendingBalance: 50 });
  if (/\/budgets\/(7|9)$/.test(url)) return ok({ idBudget: 7 });
  if (/\/payments$/.test(url)) return ok(ALL);
  return ok([]);
}

beforeEach(() => {
  mockFetch.mockReset();
  mockFetch.mockImplementation((u: unknown) => route(String(u)));
  mockReplace.mockReset();
  mockSearchParams = new URLSearchParams();
});

describe("payments-by-budget hooks (#1544)", () => {
  it("usePaymentsByBudget calls GET /payments/budget/{id}", async () => {
    const { result } = renderHook(() => usePaymentsByBudget(7), { wrapper });
    await waitFor(() => expect(result.current.isSuccess).toBe(true));
    expect(String(mockFetch.mock.calls[0][0])).toMatch(/\/payments\/budget\/7$/);
    expect(result.current.data).toHaveLength(2);
  });

  it("useBudgetBalance calls GET /payments/budget/{id}/balance", async () => {
    const { result } = renderHook(() => useBudgetBalance(7), { wrapper });
    await waitFor(() => expect(result.current.isSuccess).toBe(true));
    expect(String(mockFetch.mock.calls[0][0])).toMatch(/\/payments\/budget\/7\/balance$/);
    expect(result.current.data).toBe(50);
  });

  it("does not fetch without a budget", () => {
    renderHook(() => usePaymentsByBudget(null), { wrapper });
    renderHook(() => useBudgetBalance(null), { wrapper });
    expect(mockFetch).not.toHaveBeenCalled();
  });
});

describe("PaymentsPage consult by budget (#1544)", () => {
  it("shows the full list without a budget", async () => {
    render(<PaymentsPage />, { wrapper });
    expect((await screen.findAllByText("AllRowsMethod")).length).toBeGreaterThan(0);
    expect(screen.queryByTestId("payment-budget-summary")).toBeNull();
  });

  it("with ?budget=7 shows the summary and only that budget's payments, newest first", async () => {
    mockSearchParams = new URLSearchParams("budget=7");
    render(<PaymentsPage />, { wrapper });
    const summary = await screen.findByTestId("payment-budget-summary");
    await waitFor(() => expect(screen.getByTestId("payment-budget-balance").textContent).toMatch(/50/));
    expect(screen.getByTestId("payment-budget-total").textContent).toMatch(/100/);
    expect(screen.getByTestId("payment-budget-paid").textContent).toMatch(/50/);
    await waitFor(() => expect(screen.getByTestId("payment-budget-status").textContent).toBe("pagos.estadoParcial"));
    expect(summary).toBeInTheDocument();
    expect(screen.queryAllByText("AllRowsMethod")).toHaveLength(0);
    const methods = screen.getAllByText(/^(Efectivo|Transferencia)$/).map((e) => e.textContent);
    expect(methods.indexOf("Transferencia")).toBeLessThan(methods.indexOf("Efectivo"));
  });

  it("shows the empty state for a budget without payments", async () => {
    mockSearchParams = new URLSearchParams("budget=9");
    render(<PaymentsPage />, { wrapper });
    expect((await screen.findAllByText("pagos.consulta.sinPagos")).length).toBeGreaterThan(0);
  });

  it("picking and clearing a budget updates the URL", async () => {
    render(<PaymentsPage />, { wrapper });
    fireEvent.click(screen.getByTestId("filter-budget-payments-pick-7"));
    expect(mockReplace).toHaveBeenLastCalledWith("/dashboard/payments?budget=7", { scroll: false });
    fireEvent.click(screen.getByTestId("filter-budget-payments-clear"));
    expect(mockReplace).toHaveBeenLastCalledWith("/dashboard/payments", { scroll: false });
  });
});
