/**
 * Payments page (CU47, #796, #821, #1544, #1557, #1246): a payment needs a budget, a date and an amount, shows the
 * budget's outstanding balance and status while it is entered, is created, edited (the budget is not resent) and
 * deleted with failures reported, issues a receipt, and the list narrows by budget and by an inclusive date range.
 */
import { describe, it, expect, vi, beforeEach } from "vitest";
import { fireEvent, render, screen, waitFor, within } from "@testing-library/react";
import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import type { ReactNode } from "react";

const apiGet = vi.fn();
const apiPost = vi.fn();
const apiPut = vi.fn();
const apiDelete = vi.fn();
const receipt = vi.fn();
const replace = vi.hoisted(() =>
  vi.fn((url: string) => {
    const q = url.includes("?") ? url.slice(url.indexOf("?") + 1) : "";
    searchParams = new URLSearchParams(q);
  }),
);
const toast = vi.hoisted(() => ({ success: vi.fn(), error: vi.fn(), warning: vi.fn(), info: vi.fn() }));
let searchParams = new URLSearchParams();

vi.mock("@/lib/api-client", async (orig) => ({
  ...(await orig<typeof import("@/lib/api-client")>()),
  apiGet: (path: string) => apiGet(path),
  apiPost: (path: string, body: unknown) => apiPost(path, body),
  apiPut: (path: string, body: unknown) => apiPut(path, body),
  apiDelete: (path: string) => apiDelete(path),
}));
vi.mock("sonner", () => ({ toast }));
vi.mock("next/navigation", () => ({
  useRouter: () => ({ push: vi.fn(), replace }),
  usePathname: () => "/dashboard/payments",
  useSearchParams: () => searchParams,
}));
vi.mock("next-intl", () => ({
  useTranslations: (ns: string) => (key: string) => `${ns}.${key}`,
  useLocale: () => "es",
}));
vi.mock("@/hooks/useReports", () => ({ usePaymentReceipt: () => ({ download: receipt }) }));
vi.mock("@/components/shared/BudgetPicker", () => ({
  budgetLabel: (b: { idBudget: number }) => `#${b.idBudget}`,
  BudgetPicker: (p: { onChange: (b?: { idBudget: number }) => void; "data-testid"?: string }) => (
    <div>
      <button type="button" data-testid={`${p["data-testid"]}-pick-7`} onClick={() => p.onChange({ idBudget: 7 })} />
      <button type="button" data-testid={`${p["data-testid"]}-clear`} onClick={() => p.onChange(undefined)} />
    </div>
  ),
}));

import { ApiError } from "@/lib/api-client";
import PaymentsPage from "@/app/dashboard/payments/page";

const ALL = [
  { idPayment: 1, idBudget: 3, amount: 10, date: "2026-01-05", paymentMethod: "Efectivo", notes: "n1" },
  { idPayment: 2, fkIdBudget: { idBudget: 9 }, amount: 20, date: "2026-03-10" },
  { idPayment: 3, amount: 30, date: "2026-06-15", paymentMethod: "Transferencia" },
];
const BY7 = [
  { idPayment: 5, idBudget: 7, amount: 5, date: "2026-02-01", paymentMethod: "Efectivo" },
  { idPayment: 6, idBudget: 7, amount: 6, date: "2026-04-01", paymentMethod: "Cheque" },
];

let summaryFails = false;
let summaryPending = false;
let status: string | null = "PARTIAL";
function route(path: string): Promise<unknown> {
  if (path === "/payments") return Promise.resolve(ALL);
  if (path === "/payments/budget/7") return Promise.resolve(BY7);
  if (path === "/payments/budget/7/balance") return Promise.resolve(40);
  if (path === "/payments/budget/7/status") return Promise.resolve(status);
  if (path === "/budgets/7/summary") {
    if (summaryPending) return new Promise(() => undefined);
    return summaryFails ? Promise.reject(new ApiError(500, path, "")) : Promise.resolve({ total: 100, pendingBalance: 40 });
  }
  if (path.startsWith("/payments/date?startDate=2026-02-01&endDate=2026-04-30")) return Promise.resolve([ALL[1]]);
  return Promise.resolve([]);
}

function wrapper({ children }: { children: ReactNode }) {
  const client = new QueryClient({ defaultOptions: { queries: { retry: false } } });
  return <QueryClientProvider client={client}>{children}</QueryClientProvider>;
}

async function renderPage(query = "") {
  searchParams = new URLSearchParams(query);
  render(<PaymentsPage />, { wrapper });
  await waitFor(() => expect(screen.queryByText("pagos.noData")).toBeNull());
}

beforeEach(() => {
  summaryFails = false;
  summaryPending = false;
  status = "PARTIAL";
  searchParams = new URLSearchParams();
  replace.mockClear();
  apiGet.mockReset().mockImplementation(route);
  apiPost.mockReset().mockResolvedValue(undefined);
  apiPut.mockReset().mockResolvedValue(undefined);
  apiDelete.mockReset().mockResolvedValue(undefined);
  receipt.mockReset().mockResolvedValue(undefined);
  Object.values(toast).forEach((fn) => fn.mockReset());
});

const rowOf = (text: string) => screen.getAllByRole("row").find((row) => within(row).queryByText(text))!;
const amountInput = () => screen.getByTestId("input-amount-payment");
const dateField = () => document.querySelector<HTMLInputElement>('[role="dialog"] input[type="date"]')!;
const saveButton = () => within(screen.getByRole("dialog")).getByRole("button", { name: /common\.(create|update)/ });

async function openCreate() {
  fireEvent.click(screen.getByTestId("btn-new-payment"));
  await screen.findByTestId("input-amount-payment");
}

describe("list", () => {
  it("shows the budget of each payment, falling back to its link and to a dash", async () => {
    await renderPage();
    await screen.findByText("Efectivo");

    expect(within(rowOf("Efectivo")).getByText("#3")).toBeInTheDocument();
    expect(screen.getByText("#9")).toBeInTheDocument();
    expect(within(rowOf("Transferencia")).getByText("#—")).toBeInTheDocument();
    expect(within(rowOf("#9")).getByText("—")).toBeInTheDocument();
  });

  it("shows the load error and retries", async () => {
    let fail = true;
    apiGet.mockImplementation((path: string) =>
      path === "/payments" && fail ? Promise.reject(new ApiError(500, path, "")) : route(path),
    );
    searchParams = new URLSearchParams();
    render(<PaymentsPage />, { wrapper });

    expect(await screen.findByText("common.loadError")).toBeInTheDocument();
    fail = false;
    fireEvent.click(screen.getByRole("button", { name: /retry|reintentar/i }));

    expect(await screen.findByText("Efectivo")).toBeInTheDocument();
  });

  it("issues the receipt of a payment and reports a failure", async () => {
    await renderPage();
    await screen.findByText("Efectivo");

    fireEvent.click(within(rowOf("Efectivo")).getByTitle("pagos.emitirRecibo"));
    await waitFor(() => expect(receipt).toHaveBeenCalledWith(1));
    expect(toast.error).not.toHaveBeenCalled();

    receipt.mockRejectedValue(new ApiError(500, "/reports/payment-receipt/1", ""));
    fireEvent.click(within(rowOf("Efectivo")).getByTitle("pagos.emitirRecibo"));
    await waitFor(() => expect(toast.error).toHaveBeenCalledWith("pagos.errorRecibo"));
  });
});

describe("budget consultation", () => {
  it("shows the summary and only that budget's payments, newest first", async () => {
    await renderPage("budget=7");

    expect(await screen.findByText("Cheque")).toBeInTheDocument();
    expect(screen.queryByText("Transferencia")).toBeNull();
    const rows = screen.getAllByRole("row");
    expect(rows.findIndex((r) => within(r).queryByText("Cheque"))).toBeLessThan(
      rows.findIndex((r) => within(r).queryByText("Efectivo")),
    );
    expect(screen.getByTestId("payment-budget-status")).toHaveTextContent("pagos.estadoParcial");
    expect(screen.getByTestId("payment-budget-total")).toHaveTextContent("100");
  });

  it.each([
    ["PAID", "pagos.estadoSaldado"],
    ["NoPayments", "pagos.estadoSinPagos"],
    [null, "—"],
  ])("labels the status %s as %s", async (value, label) => {
    status = value;
    await renderPage("budget=7");

    await waitFor(() => expect(screen.getByTestId("payment-budget-status")).toHaveTextContent(label));
  });

  it("ignores a budget that is not a positive integer", async () => {
    await renderPage("budget=abc");

    expect(screen.queryByTestId("payment-budget-summary")).toBeNull();
    expect(apiGet).not.toHaveBeenCalledWith("/payments/budget/NaN");
  });

  it("puts the chosen budget in the address and removes it when cleared", async () => {
    await renderPage();

    fireEvent.click(screen.getByTestId("filter-budget-payments-pick-7"));
    expect(replace).toHaveBeenLastCalledWith("/dashboard/payments?budget=7", { scroll: false });

    fireEvent.click(screen.getByTestId("filter-budget-payments-clear"));
    expect(replace).toHaveBeenLastCalledWith("/dashboard/payments", { scroll: false });
  });

  it("says the budget has no payments when the list is empty", async () => {
    apiGet.mockImplementation((path: string) => (path === "/payments/budget/7" ? Promise.resolve([]) : route(path)));
    searchParams = new URLSearchParams("budget=7");
    render(<PaymentsPage />, { wrapper });

    expect(await screen.findByText("pagos.consulta.sinPagos")).toBeInTheDocument();
  });
});

describe("date range", () => {
  it("narrows on the client with a single bound", async () => {
    await renderPage("from=2026-03-01");
    await screen.findByText("Transferencia");

    expect(screen.queryByText("Efectivo")).toBeNull();
    expect(apiGet).not.toHaveBeenCalledWith(expect.stringContaining("/payments/date"));
  });

  it("narrows on the client with an upper bound only", async () => {
    await renderPage("to=2026-03-31");
    await screen.findByText("Efectivo");

    expect(screen.queryByText("Transferencia")).toBeNull();
  });

  it("asks the server for a range with both days", async () => {
    await renderPage("from=2026-02-01&to=2026-04-30");

    await waitFor(() =>
      expect(apiGet).toHaveBeenCalledWith("/payments/date?startDate=2026-02-01&endDate=2026-04-30"),
    );
    await waitFor(() => expect(screen.queryByText("Efectivo")).toBeNull());
  });

  it("narrows on the client when a budget is also chosen", async () => {
    await renderPage("budget=7&from=2026-03-01&to=2026-12-31");

    expect(await screen.findByText("Cheque")).toBeInTheDocument();
    expect(screen.queryByText("Efectivo")).toBeNull();
    expect(apiGet).not.toHaveBeenCalledWith(expect.stringContaining("/payments/date"));
  });

  it("flags a range that ends before it starts and lists nothing", async () => {
    await renderPage("from=2026-05-01&to=2026-01-01");

    expect(await screen.findByTestId("payments-range-error")).toHaveTextContent("pagos.consulta.rangoInvalido");
    expect(screen.queryByText("Efectivo")).toBeNull();
    expect(screen.getByTestId("input-payments-from")).toHaveAttribute("aria-invalid", "true");
  });

  it("ignores a day that is not in ISO form", async () => {
    await renderPage("from=01/02/2026");

    expect(screen.getByTestId("input-payments-from")).toHaveValue("");
    expect(await screen.findByText("Efectivo")).toBeInTheDocument();
  });

  it("keeps the typed days in the address and removes one that is cleared", async () => {
    await renderPage();

    fireEvent.change(screen.getByTestId("input-payments-from"), { target: { value: "2026-02-01" } });
    expect(replace).toHaveBeenLastCalledWith("/dashboard/payments?from=2026-02-01", { scroll: false });
    fireEvent.change(screen.getByTestId("input-payments-to"), { target: { value: "2026-04-30" } });
    expect(replace).toHaveBeenLastCalledWith("/dashboard/payments?from=2026-02-01&to=2026-04-30", { scroll: false });
    fireEvent.change(screen.getByTestId("input-payments-from"), { target: { value: "" } });
    expect(replace).toHaveBeenLastCalledWith("/dashboard/payments?to=2026-04-30", { scroll: false });
  });
});

describe("payment form", () => {
  it("needs a budget, a date and an amount", async () => {
    await renderPage();
    await openCreate();

    fireEvent.click(saveButton());

    expect(await screen.findAllByText("common.fieldRequired")).toHaveLength(3);
    expect(apiPost).not.toHaveBeenCalled();
  });

  it("shows the budget's outstanding balance and status once a budget is chosen", async () => {
    await renderPage();
    await openCreate();

    fireEvent.click(screen.getByTestId("select-budget-payment-pick-7"));

    expect(await screen.findByTestId("pending-balance-amount")).toHaveTextContent("40");
    expect(await screen.findByTestId("status-payment-badge")).toHaveTextContent("pagos.estadoParcial");
  });

  it("shows a loading text while the balance loads and an error when it cannot be read", async () => {
    summaryPending = true;
    await renderPage();
    await openCreate();
    fireEvent.click(screen.getByTestId("select-budget-payment-pick-7"));
    expect(await screen.findByText("pagos.saldo.loading")).toBeInTheDocument();
  });

  it("says the balance cannot be read when the summary fails", async () => {
    summaryFails = true;
    await renderPage();
    await openCreate();

    fireEvent.click(screen.getByTestId("select-budget-payment-pick-7"));

    expect(await screen.findByText("pagos.saldo.error")).toBeInTheDocument();
  });

  it("creates the payment with every value", async () => {
    await renderPage();
    await openCreate();
    fireEvent.click(screen.getByTestId("select-budget-payment-pick-7"));
    fireEvent.change(dateField(), { target: { value: "2026-05-02" } });
    fireEvent.change(amountInput(), { target: { value: "12.5" } });
    const dialog = screen.getByRole("dialog");
    const [method, notes] = Array.from(dialog.querySelectorAll<HTMLInputElement>('input:not([type="number"]):not([type="date"])'));
    fireEvent.change(method, { target: { value: "Cheque" } });
    fireEvent.change(notes, { target: { value: "obs" } });

    fireEvent.click(saveButton());

    await waitFor(() =>
      expect(apiPost).toHaveBeenCalledWith("/payments", {
        idBudget: 7, amount: 12.5, date: "2026-05-02", paymentMethod: "Cheque", notes: "obs",
      }),
    );
    expect(toast.success).toHaveBeenCalledWith("pagos.created");
    await waitFor(() => expect(screen.queryByTestId("input-amount-payment")).toBeNull());
  });

  it("clears the budget when the picker is cleared", async () => {
    await renderPage();
    await openCreate();
    fireEvent.click(screen.getByTestId("select-budget-payment-pick-7"));
    await screen.findByTestId("pending-balance-amount");

    fireEvent.click(screen.getByTestId("select-budget-payment-clear"));

    await waitFor(() => expect(screen.queryByTestId("pending-balance-amount")).toBeNull());
  });

  it("explains a payment that exceeds the balance when the server gives no reason", async () => {
    apiPost.mockRejectedValue(new ApiError(409, "/payments", ""));
    await renderPage();
    await openCreate();
    fireEvent.click(screen.getByTestId("select-budget-payment-pick-7"));
    fireEvent.change(dateField(), { target: { value: "2026-05-02" } });
    fireEvent.change(amountInput(), { target: { value: "999" } });

    fireEvent.click(saveButton());

    await waitFor(() => expect(toast.error).toHaveBeenCalledWith("pagos.saldoExcedido"));
    expect(screen.getByTestId("input-amount-payment")).toBeInTheDocument();
  });

  it("falls back to the generic message for any other failure", async () => {
    apiPost.mockRejectedValue(new ApiError(500, "/payments", ""));
    await renderPage();
    await openCreate();
    fireEvent.click(screen.getByTestId("select-budget-payment-pick-7"));
    fireEvent.change(dateField(), { target: { value: "2026-05-02" } });
    fireEvent.change(amountInput(), { target: { value: "5" } });

    fireEvent.click(saveButton());

    await waitFor(() => expect(toast.error).toHaveBeenCalledWith("pagos.errorSave"));
  });

  it("shows the server's reason and marks the field it names", async () => {
    apiPost.mockRejectedValue(new ApiError(400, "/payments", '{"message":"amount: debe ser positivo"}'));
    await renderPage();
    await openCreate();
    fireEvent.click(screen.getByTestId("select-budget-payment-pick-7"));
    fireEvent.change(dateField(), { target: { value: "2026-05-02" } });
    fireEvent.change(amountInput(), { target: { value: "5" } });

    fireEvent.click(saveButton());

    expect(await screen.findByText("debe ser positivo")).toBeInTheDocument();
  });

  it("edits a payment with PUT without resending the budget", async () => {
    await renderPage();
    await screen.findByText("Efectivo");
    fireEvent.click(within(rowOf("Efectivo")).getByRole("button", { name: "common.edit" }));
    await screen.findByTestId("input-amount-payment");
    expect(dateField()).toHaveValue("2026-01-05");

    fireEvent.change(amountInput(), { target: { value: "11" } });
    fireEvent.click(saveButton());

    await waitFor(() =>
      expect(apiPut).toHaveBeenCalledWith("/payments/1", {
        amount: 11, date: "2026-01-05", paymentMethod: "Efectivo", notes: "n1",
      }),
    );
    expect(toast.success).toHaveBeenCalledWith("pagos.updated");
    expect(apiPost).not.toHaveBeenCalled();
  });

  it("starts a new payment empty after editing another", async () => {
    await renderPage();
    await screen.findByText("Efectivo");
    fireEvent.click(within(rowOf("Efectivo")).getByRole("button", { name: "common.edit" }));
    await screen.findByTestId("input-amount-payment");
    fireEvent.click(within(screen.getByRole("dialog")).getByRole("button", { name: "common.cancel" }));
    await waitFor(() => expect(screen.queryByTestId("input-amount-payment")).toBeNull());

    await openCreate();

    expect(amountInput()).toHaveValue(null);
    expect(dateField()).toHaveValue("");
  });
});

describe("delete", () => {
  it("deletes after the confirmation", async () => {
    await renderPage();
    await screen.findByText("Efectivo");

    fireEvent.click(within(rowOf("Efectivo")).getByRole("button", { name: "common.delete" }));
    expect(apiDelete).not.toHaveBeenCalled();
    fireEvent.click(await screen.findByTestId("confirm-dialog-confirm"));

    await waitFor(() => expect(apiDelete).toHaveBeenCalledWith("/payments/1"));
    expect(toast.success).toHaveBeenCalledWith("pagos.deleted");
  });

  it("reports a payment that cannot be deleted", async () => {
    apiDelete.mockRejectedValue(new ApiError(500, "/payments/1", ""));
    await renderPage();
    await screen.findByText("Efectivo");
    fireEvent.click(within(rowOf("Efectivo")).getByRole("button", { name: "common.delete" }));

    fireEvent.click(await screen.findByTestId("confirm-dialog-confirm"));

    await waitFor(() => expect(toast.warning.mock.calls.length + toast.error.mock.calls.length).toBeGreaterThan(0));
    expect(toast.success).not.toHaveBeenCalled();
  });
});
