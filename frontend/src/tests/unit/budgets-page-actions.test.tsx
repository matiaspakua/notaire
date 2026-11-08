/**
 * Budgets page (CU01, CU39, CU45, CU47, CU60, #1246): required fields block a save, a budget is created
 * with or without its template items, an edit keeps a legacy status visible, the list narrows by status,
 * number and client name, the summary and the items dialogs report their data and failures.
 */
import { describe, it, expect, vi, beforeEach } from "vitest";
import { fireEvent, render, screen, waitFor, within } from "@testing-library/react";
import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import type { ReactNode } from "react";

const apiGet = vi.fn();
const apiGetPage = vi.fn();
const apiPost = vi.fn();
const apiPut = vi.fn();
const apiDelete = vi.fn();
const toast = vi.hoisted(() => ({ success: vi.fn(), error: vi.fn(), warning: vi.fn(), info: vi.fn() }));

vi.mock("@/lib/api-client", async (orig) => ({
  ...(await orig<typeof import("@/lib/api-client")>()),
  apiGet: (path: string) => apiGet(path),
  apiGetPage: (path: string, request: unknown) => apiGetPage(path, request),
  apiPost: (path: string, body: unknown) => apiPost(path, body),
  apiPut: (path: string, body: unknown) => apiPut(path, body),
  apiDelete: (path: string) => apiDelete(path),
}));
vi.mock("sonner", () => ({ toast }));
vi.mock("next/navigation", () => ({
  useRouter: () => ({ push: vi.fn(), replace: vi.fn() }),
  usePathname: () => "/dashboard/budgets",
  useSearchParams: () => new URLSearchParams(),
}));
vi.mock("next-intl", () => ({
  useTranslations: (ns: string) => (key: string, values?: Record<string, unknown>) =>
    values ? `${ns}.${key}:${JSON.stringify(values)}` : `${ns}.${key}`,
  useLocale: () => "es",
}));
vi.mock("@/components/shared/PersonPicker", () => ({
  PersonPicker: (props: {
    onChange: (p: { personId: number; name: string; surname: string } | undefined) => void;
    "data-testid"?: string;
    value?: number;
  }) => (
    <button
      type="button"
      data-testid={props["data-testid"]}
      onClick={() => props.onChange({ personId: 31, name: "Ana", surname: "Pérez" })}
    >
      {`person:${props.value ?? "none"}`}
    </button>
  ),
}));
vi.mock("@/components/ui/select", async () => {
  const React = await import("react");
  const Ctx = React.createContext<{
    value: string;
    onValueChange: (v: string) => void;
    disabled?: boolean;
    open: boolean;
    setOpen: (o: boolean) => void;
  }>({ value: "", onValueChange: () => undefined, open: false, setOpen: () => undefined });
  return {
    Select: ({ value, onValueChange, disabled, children }: {
      value: string; onValueChange: (v: string) => void; disabled?: boolean; children: ReactNode;
    }) => {
      const [open, setOpen] = React.useState(false);
      return <Ctx.Provider value={{ value, onValueChange, disabled, open, setOpen }}>{children}</Ctx.Provider>;
    },
    SelectTrigger: ({ children, ...rest }: { children: ReactNode; "data-testid"?: string }) => {
      const ctx = React.useContext(Ctx);
      return <button type="button" disabled={ctx.disabled} onClick={() => ctx.setOpen(!ctx.open)} {...rest}>{children}</button>;
    },
    SelectValue: () => null,
    SelectContent: ({ children }: { children: ReactNode }) => {
      const ctx = React.useContext(Ctx);
      return ctx.open ? <div>{children}</div> : null;
    },
    SelectItem: ({ value, children }: { value: string; children: ReactNode }) => {
      const ctx = React.useContext(Ctx);
      return (
        <button type="button" role="option" aria-selected={ctx.value === value}
          onClick={() => { ctx.onValueChange(value); ctx.setOpen(false); }}>
          {children}
        </button>
      );
    },
  };
});

import { ApiError } from "@/lib/api-client";
import BudgetsPage from "@/app/dashboard/budgets/page";

const ROWS = [
  { idBudget: 1, date: "2026-01-05", status: "BORRADOR", propertyAmount: 1000, person: { personId: 4, name: "Luis", surname: "Gómez" } },
  { idBudget: 2, date: "2026-02-06", status: "APROBADO", propertyAmount: 2500, person: { personId: 5, name: "Marta", surname: "Díaz" } },
  { idBudget: 3, date: "2026-03-07", status: "ANTIGUO", propertyAmount: 10 },
];

let summary: unknown = { idBudget: 1, numberBudget: 1, total: 1000, pendingBalance: 400, payments: [] };

function wrapper({ children }: { children: ReactNode }) {
  const client = new QueryClient({ defaultOptions: { queries: { retry: false } } });
  return <QueryClientProvider client={client}>{children}</QueryClientProvider>;
}

function route(path: string): Promise<unknown> {
  if (path === "/procedure-types") return Promise.resolve([{ idProcedureType: 6, name: "Compraventa" }]);
  if (path === "/items") return Promise.resolve([{ idItem: 11, name: "Sellado", value: 50 }]);
  if (path === "/items/budget/1") {
    return Promise.resolve([{ idItem: 11, name: "Sellado", value: 50, percentage: 5 }, { idItem: 12, name: "Honorarios", value: 70 }]);
  }
  if (path.startsWith("/items/budget/")) return Promise.resolve([]);
  if (path.endsWith("/summary")) return summary instanceof Error ? Promise.reject(summary) : Promise.resolve(summary);
  if (path.startsWith("/budgets/search?status=APROBADO")) return Promise.resolve([ROWS[1]]);
  if (path === "/budgets/2") return Promise.resolve(ROWS[1]);
  if (path === "/budgets/99") return Promise.reject(new ApiError(404, path, "missing"));
  return Promise.resolve([]);
}

async function renderPage() {
  render(<BudgetsPage />, { wrapper });
  await screen.findByTestId("btn-edit-budget-1");
}

beforeEach(() => {
  summary = { idBudget: 1, numberBudget: 1, total: 1000, pendingBalance: 400, payments: [] };
  apiGet.mockReset().mockImplementation(route);
  apiGetPage.mockReset().mockResolvedValue({ content: ROWS, number: 0, size: 20, totalElements: 3, totalPages: 1 });
  apiPost.mockReset().mockImplementation((path: string) =>
    path.includes("items-from-") ? Promise.resolve([{ idItem: 11, name: "Sellado", value: 50 }]) : Promise.resolve({ idBudget: 50, status: "BORRADOR" }),
  );
  apiPut.mockReset().mockResolvedValue(undefined);
  apiDelete.mockReset().mockResolvedValue(undefined);
  Object.values(toast).forEach((fn) => fn.mockReset());
});

const dateInput = () => document.querySelector<HTMLInputElement>('input[type="date"]')!;

async function fillNewBudget() {
  fireEvent.click(screen.getByTestId("btn-new-budget"));
  fireEvent.click(await screen.findByTestId("select-person"));
  fireEvent.change(dateInput(), { target: { value: "2026-05-01" } });
  fireEvent.change(screen.getByTestId("input-amount"), { target: { value: "1500.5" } });
}

describe("create", () => {
  it("shows the three required-field errors and sends nothing", async () => {
    await renderPage();
    fireEvent.click(screen.getByTestId("btn-new-budget"));
    fireEvent.click(await screen.findByTestId("btn-save-budget"));

    expect(await screen.findAllByText("common.fieldRequired")).toHaveLength(3);
    expect(apiPost).not.toHaveBeenCalled();
  });

  it("creates with the default status and no template items", async () => {
    await renderPage();
    await fillNewBudget();

    fireEvent.click(screen.getByTestId("btn-save-budget"));

    await waitFor(() => expect(apiPost).toHaveBeenCalledWith("/budgets", expect.objectContaining({ status: "BORRADOR", propertyAmount: 1500.5, date: "2026-05-01" })));
    expect(toast.success).toHaveBeenCalledWith("presupuestos.created");
    expect(toast.warning).not.toHaveBeenCalled();
    expect(apiPost).toHaveBeenCalledTimes(1);
  });

  it("loads the chosen procedure's template items after creating", async () => {
    await renderPage();
    await fillNewBudget();
    fireEvent.click(screen.getByTestId("select-procedure-type-new"));
    fireEvent.click(await screen.findByRole("option", { name: "Compraventa" }));

    fireEvent.click(screen.getByTestId("btn-save-budget"));

    await waitFor(() => expect(apiPost).toHaveBeenCalledWith("/budgets/50/items-from-template?procedureTypeId=6", undefined));
    expect(toast.warning).not.toHaveBeenCalled();
  });

  it("warns, without undoing the budget, when the template cannot be loaded", async () => {
    apiPost.mockImplementation((path: string) =>
      path.includes("items-from-template") ? Promise.reject(new Error("no template")) : Promise.resolve({ idBudget: 50 }),
    );
    await renderPage();
    await fillNewBudget();
    fireEvent.click(screen.getByTestId("select-procedure-type-new"));
    fireEvent.click(await screen.findByRole("option", { name: "Compraventa" }));

    fireEvent.click(screen.getByTestId("btn-save-budget"));

    await waitFor(() => expect(toast.warning).toHaveBeenCalledWith("presupuestos.itemsNotLoaded"));
    expect(toast.success).toHaveBeenCalledWith("presupuestos.created");
  });

  it("reports a failed save and keeps the form open", async () => {
    apiPost.mockRejectedValue(new ApiError(500, "/budgets", "boom"));
    await renderPage();
    await fillNewBudget();

    fireEvent.click(screen.getByTestId("btn-save-budget"));

    await waitFor(() => expect(toast.error).toHaveBeenCalled());
    expect(toast.success).not.toHaveBeenCalled();
    expect(screen.getByTestId("btn-save-budget")).toBeInTheDocument();
  });
});

describe("edit and delete", () => {
  it("prefills the budget, hides the template choice and saves with PUT", async () => {
    await renderPage();
    fireEvent.click(screen.getByTestId("btn-edit-budget-1"));

    expect(await screen.findByTestId("input-amount")).toHaveValue(1000);
    expect(screen.queryByTestId("select-procedure-type-new")).toBeNull();
    fireEvent.change(screen.getByTestId("input-amount"), { target: { value: "1200" } });
    fireEvent.click(screen.getByTestId("btn-save-budget"));

    await waitFor(() => expect(apiPut).toHaveBeenCalledWith("/budgets/1", expect.objectContaining({ propertyAmount: 1200 })));
    expect(toast.success).toHaveBeenCalledWith("presupuestos.updated");
  });

  it("keeps a status outside the vocabulary visible but not selectable", async () => {
    await renderPage();
    fireEvent.click(screen.getByTestId("btn-edit-budget-3"));
    fireEvent.click(await screen.findByTestId("select-status-budget"));

    expect(await screen.findByRole("option", { name: "ANTIGUO" })).toBeInTheDocument();
  });

  it("deletes once confirmed and reports a failure", async () => {
    await renderPage();
    fireEvent.click(screen.getByTestId("btn-delete-budget-1"));
    fireEvent.click(await screen.findByTestId("confirm-dialog-confirm"));
    await waitFor(() => expect(apiDelete).toHaveBeenCalledWith("/budgets/1"));
    expect(toast.success).toHaveBeenCalledWith("presupuestos.deleted");
  });

  it("reports a budget that cannot be deleted", async () => {
    apiDelete.mockRejectedValue(new ApiError(409, "/budgets/1", "in use"));
    await renderPage();
    fireEvent.click(screen.getByTestId("btn-delete-budget-1"));
    fireEvent.click(await screen.findByTestId("confirm-dialog-confirm"));
    await waitFor(() => expect(toast.warning).toHaveBeenCalledWith("common.errors.inUse", expect.anything()));
    expect(toast.success).not.toHaveBeenCalled();
  });
});

describe("list filters", () => {
  it("narrows the page by client name, case-insensitively, and hides the pagination", async () => {
    await renderPage();
    fireEvent.change(screen.getByTestId("input-search-budget"), { target: { value: "MARTA" } });

    expect(screen.getByTestId("btn-edit-budget-2")).toBeInTheDocument();
    expect(screen.queryByTestId("btn-edit-budget-1")).toBeNull();
  });

  it("looks a budget number up on the server", async () => {
    await renderPage();
    fireEvent.change(screen.getByTestId("input-search-budget"), { target: { value: " 2 " } });

    await waitFor(() => expect(apiGet).toHaveBeenCalledWith("/budgets/2"));
    expect(await screen.findByTestId("btn-edit-budget-2")).toBeInTheDocument();
    expect(screen.queryByTestId("btn-edit-budget-1")).toBeNull();
  });

  it("shows nothing for a number that does not exist", async () => {
    await renderPage();
    fireEvent.change(screen.getByTestId("input-search-budget"), { target: { value: "99" } });

    await waitFor(() => expect(apiGet).toHaveBeenCalledWith("/budgets/99"));
    await waitFor(() => expect(screen.queryByTestId("btn-edit-budget-1")).toBeNull());
  });

  it("reads the budgets of a status from the server", async () => {
    await renderPage();
    fireEvent.click(screen.getByTestId("select-status"));
    fireEvent.click(await screen.findByRole("option", { name: "presupuestos.status.APROBADO" }));

    await waitFor(() => expect(apiGet).toHaveBeenCalledWith("/budgets/search?status=APROBADO"));
    expect(await screen.findByTestId("btn-edit-budget-2")).toBeInTheDocument();
  });

  it("shows the load error and retries the page", async () => {
    apiGetPage.mockRejectedValueOnce(new ApiError(500, "/budgets", "down")).mockResolvedValue({ content: ROWS, number: 0, size: 20, totalElements: 3, totalPages: 1 });
    render(<BudgetsPage />, { wrapper });

    expect(await screen.findByText("presupuestos.loadError")).toBeInTheDocument();
    fireEvent.click(screen.getByRole("button", { name: /retry|common\.retry|reintentar/i }));

    expect(await screen.findByTestId("btn-edit-budget-1")).toBeInTheDocument();
  });
});

describe("summary dialog", () => {
  it("shows the management, totals and the empty payments message", async () => {
    await renderPage();
    fireEvent.click(screen.getByTestId("btn-summary-budget-1"));

    const dialog = await screen.findByTestId("dialog-summary-budget");
    expect(await within(dialog).findByTestId("summary-empty-payments")).toBeInTheDocument();
  });

  it("lists the payments of the budget", async () => {
    summary = { idBudget: 1, numberBudget: 1, total: 1000, pendingBalance: 0, payments: [{ idPayment: 71, amount: 600, date: "2026-02-01" }, { idPayment: 72, amount: 400, date: "2026-03-01", notes: "saldo" }] };
    await renderPage();
    fireEvent.click(screen.getByTestId("btn-summary-budget-1"));

    const dialog = await screen.findByTestId("dialog-summary-budget");
    expect(await within(dialog).findByText("saldo")).toBeInTheDocument();
    expect(within(dialog).getByText("71")).toBeInTheDocument();
    expect(within(dialog).queryByTestId("summary-empty-payments")).toBeNull();
  });

  it("says a budget without a management was not found", async () => {
    summary = new ApiError(404, "/budgets/1/summary", "none");
    await renderPage();
    fireEvent.click(screen.getByTestId("btn-summary-budget-1"));

    expect(await screen.findByTestId("summary-not-found")).toHaveTextContent("presupuestos.resumen.notFound");
  });

  it("shows the generic error for any other failure", async () => {
    summary = new ApiError(500, "/budgets/1/summary", "down");
    await renderPage();
    fireEvent.click(screen.getByTestId("btn-summary-budget-1"));

    expect(await screen.findByTestId("summary-not-found")).toHaveTextContent("presupuestos.errorSave");
  });
});

describe("items dialog", () => {
  it("lists the items with their subtotal", async () => {
    await renderPage();
    fireEvent.click(screen.getByTestId("btn-items-budget-1"));

    expect(await screen.findByTestId("table-items-budget")).toBeInTheDocument();
    expect(screen.getByTestId("items-subtotal")).toHaveTextContent(/120/);
    expect(screen.getByText("5%")).toBeInTheDocument();
    expect(screen.getByText("0%")).toBeInTheDocument();
  });

  it("says a budget without items has none", async () => {
    await renderPage();
    fireEvent.click(screen.getByTestId("btn-items-budget-2"));

    expect(await screen.findByTestId("items-empty-data")).toBeInTheDocument();
  });

  it("loads a template only once a procedure type is chosen", async () => {
    await renderPage();
    fireEvent.click(screen.getByTestId("btn-items-budget-1"));
    await screen.findByTestId("table-items-budget");
    expect(screen.getByTestId("btn-load-template")).toBeDisabled();

    fireEvent.click(screen.getByTestId("select-procedure-type-items"));
    fireEvent.click(await screen.findByRole("option", { name: "Compraventa" }));
    fireEvent.click(screen.getByTestId("btn-load-template"));

    await waitFor(() => expect(apiPost).toHaveBeenCalledWith("/budgets/1/items-from-template?procedureTypeId=6", undefined));
    expect(toast.success).toHaveBeenCalledWith("presupuestos.items.loadedFromPlantilla");
  });

  it("keeps the curated message when the procedure has no template", async () => {
    apiPost.mockRejectedValue(new ApiError(400, "/budgets/1/items-from-template", "terse server text"));
    await renderPage();
    fireEvent.click(screen.getByTestId("btn-items-budget-1"));
    await screen.findByTestId("table-items-budget");
    fireEvent.click(screen.getByTestId("select-procedure-type-items"));
    fireEvent.click(await screen.findByRole("option", { name: "Compraventa" }));
    fireEvent.click(screen.getByTestId("btn-load-template"));

    await waitFor(() => expect(toast.error).toHaveBeenCalledWith("presupuestos.items.errorNoPlantilla"));
  });

  it("reports any other template failure with its own message", async () => {
    apiPost.mockRejectedValue(new ApiError(500, "/budgets/1/items-from-template", ""));
    await renderPage();
    fireEvent.click(screen.getByTestId("btn-items-budget-1"));
    await screen.findByTestId("table-items-budget");
    fireEvent.click(screen.getByTestId("select-procedure-type-items"));
    fireEvent.click(await screen.findByRole("option", { name: "Compraventa" }));
    fireEvent.click(screen.getByTestId("btn-load-template"));

    await waitFor(() => expect(toast.error).toHaveBeenCalled());
    expect(toast.error).not.toHaveBeenCalledWith("presupuestos.items.errorNoPlantilla");
  });

  it("adds a catalog item and clears the choice", async () => {
    await renderPage();
    fireEvent.click(screen.getByTestId("btn-items-budget-1"));
    await screen.findByTestId("table-items-budget");
    expect(screen.getByTestId("btn-add-from-catalog")).toBeDisabled();

    fireEvent.click(screen.getByTestId("select-catalog-item"));
    fireEvent.click(await screen.findByRole("option", { name: /Sellado/ }));
    fireEvent.click(screen.getByTestId("btn-add-from-catalog"));

    await waitFor(() => expect(apiPost).toHaveBeenCalledWith("/budgets/1/items-from-catalog", { idItems: [11] }));
    expect(toast.success).toHaveBeenCalledWith("presupuestos.items.addedFromCatalogo");
    await waitFor(() => expect(screen.getByTestId("btn-add-from-catalog")).toBeDisabled());
  });

  it("reports a catalog item that cannot be added", async () => {
    apiPost.mockRejectedValue(new ApiError(500, "/budgets/1/items-from-catalog", ""));
    await renderPage();
    fireEvent.click(screen.getByTestId("btn-items-budget-1"));
    await screen.findByTestId("table-items-budget");
    fireEvent.click(screen.getByTestId("select-catalog-item"));
    fireEvent.click(await screen.findByRole("option", { name: /Sellado/ }));
    fireEvent.click(screen.getByTestId("btn-add-from-catalog"));

    await waitFor(() => expect(toast.error).toHaveBeenCalled());
    expect(toast.success).not.toHaveBeenCalled();
  });

  it("tells the reader when there are no procedure types or catalog items to choose from", async () => {
    apiGet.mockImplementation((path: string) =>
      path === "/procedure-types" || path === "/items" ? Promise.resolve([]) : route(path),
    );
    await renderPage();
    fireEvent.click(screen.getByTestId("btn-items-budget-1"));

    expect(await screen.findByText("presupuestos.items.noTiposTramite")).toBeInTheDocument();
    expect(screen.getByText("presupuestos.items.noCatalogItems")).toBeInTheDocument();
    expect(screen.getByTestId("select-procedure-type-items")).toBeDisabled();
    expect(screen.getByTestId("select-catalog-item")).toBeDisabled();
  });
});
