/**
 * Items page (CU39, CU45, #1246): an item is created or edited with an optional budget, a discount or surcharge
 * needs a reason, the item type defaults to normal, deletion needs confirmation and reports a conflict, and the
 * discounts-and-surcharges report is consulted per budget.
 */
import { describe, it, expect, vi, beforeEach } from "vitest";
import { fireEvent, render, screen, waitFor, within } from "@testing-library/react";
import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import type { ReactNode } from "react";

const apiGet = vi.fn();
const apiPost = vi.fn();
const apiPut = vi.fn();
const apiDelete = vi.fn();
const toast = vi.hoisted(() => ({ success: vi.fn(), error: vi.fn(), warning: vi.fn(), info: vi.fn() }));

vi.mock("@/lib/api-client", async (orig) => ({
  ...(await orig<typeof import("@/lib/api-client")>()),
  apiGet: (path: string) => apiGet(path),
  apiPost: (path: string, body: unknown) => apiPost(path, body),
  apiPut: (path: string, body: unknown) => apiPut(path, body),
  apiDelete: (path: string) => apiDelete(path),
}));
vi.mock("sonner", () => ({ toast }));
vi.mock("next/navigation", () => ({
  useRouter: () => ({ push: vi.fn(), replace: vi.fn() }),
  usePathname: () => "/dashboard/items",
  useSearchParams: () => new URLSearchParams(),
}));
vi.mock("next-intl", () => ({
  useTranslations: (ns: string) => (key: string, values?: Record<string, unknown>) =>
    values ? `${ns}.${key}:${JSON.stringify(values)}` : `${ns}.${key}`,
  useLocale: () => "es",
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
import ItemsPage from "@/app/dashboard/items/page";

const I = "items";

const ROWS = [
  { idItem: 1, name: "Sellado", value: 50, type: "NORMAL", fkIdBudget: { idBudget: 12 } },
  { idItem: 2, name: "Promo", value: 10, type: "DESCUENTO", reason: "cliente frecuente" },
  { idItem: 3, value: 5 },
];

const REPORT = [
  { idItem: 5, name: "Descuento", type: "DESCUENTO", reason: "amigo", value: 20 },
  { idItem: 6, type: "RECARGO", value: 8 },
];

function wrapper({ children }: { children: ReactNode }) {
  const client = new QueryClient({ defaultOptions: { queries: { retry: false } } });
  return <QueryClientProvider client={client}>{children}</QueryClientProvider>;
}

function route(path: string): Promise<unknown> {
  if (path === "/items") return Promise.resolve(ROWS);
  if (path === "/items/budget/12/discounts-surcharges") return Promise.resolve(REPORT);
  if (path === "/items/budget/99/discounts-surcharges") return Promise.resolve([]);
  return Promise.resolve([]);
}

async function renderPage() {
  render(<ItemsPage />, { wrapper });
  await screen.findByText("Sellado");
}

const rowOf = (text: string) => screen.getByText(text).closest("tr")!;
const editOf = (text: string) => within(rowOf(text)).getByRole("button", { name: "common.edit" });
const deleteOf = (text: string) => within(rowOf(text)).getByRole("button", { name: "common.delete" });
const saveButton = () => screen.getByTestId("btn-save-item");

async function pickType(name: string) {
  fireEvent.click(screen.getByTestId("select-item-type"));
  fireEvent.click(await screen.findByRole("option", { name }));
}

beforeEach(() => {
  apiGet.mockReset().mockImplementation(route);
  apiPost.mockReset().mockResolvedValue(undefined);
  apiPut.mockReset().mockResolvedValue(undefined);
  apiDelete.mockReset().mockResolvedValue(undefined);
  Object.values(toast).forEach((fn) => fn.mockReset());
});

describe("list", () => {
  it("shows the budget number or a dash, the type label and a dash for a missing name", async () => {
    await renderPage();

    expect(within(rowOf("Sellado")).getByText("#12")).toBeInTheDocument();
    expect(within(rowOf("Sellado")).getByText(`${I}.types.NORMAL`)).toBeInTheDocument();
    expect(within(rowOf("Promo")).getByText(`${I}.types.DESCUENTO`)).toBeInTheDocument();
    const unnamed = screen.getAllByRole("row").find((r) => within(r).queryByText("3"))!;
    expect(within(unnamed).getAllByText("—")).toHaveLength(2);
    expect(within(unnamed).getByText(`${I}.types.NORMAL`)).toBeInTheDocument();
  });

  it("reports a failed load and reloads on retry", async () => {
    apiGet.mockRejectedValue(new Error("down"));
    render(<ItemsPage />, { wrapper });

    fireEvent.click(await screen.findByTestId("btn-retry"));

    await waitFor(() => expect(apiGet.mock.calls.length).toBeGreaterThan(1));
  });
});

describe("create", () => {
  it("creates a normal item without a budget by default", async () => {
    await renderPage();
    fireEvent.click(screen.getByTestId("btn-new-item"));
    fireEvent.change(await screen.findByTestId("input-quantity"), { target: { value: "Honorarios" } });
    fireEvent.change(screen.getByTestId("input-price"), { target: { value: "70.5" } });

    fireEvent.click(saveButton());

    await waitFor(() =>
      expect(apiPost).toHaveBeenCalledWith("/items", { name: "Honorarios", value: 70.5, type: "NORMAL", fkIdBudget: undefined }),
    );
    expect(toast.success).toHaveBeenCalledWith(`${I}.created`);
    await waitFor(() => expect(screen.queryByTestId("btn-save-item")).toBeNull());
  });

  it("links the item to the typed budget", async () => {
    await renderPage();
    fireEvent.click(screen.getByTestId("btn-new-item"));
    fireEvent.change(await screen.findByTestId("input-budget-id"), { target: { value: "12" } });

    fireEvent.click(saveButton());

    await waitFor(() => expect(apiPost).toHaveBeenCalledWith("/items", expect.objectContaining({ fkIdBudget: { idBudget: 12 } })));
  });

  it("requires a reason for a discount or a surcharge and sends nothing without it", async () => {
    await renderPage();
    fireEvent.click(screen.getByTestId("btn-new-item"));
    await pickType(`${I}.types.RECARGO`);
    fireEvent.change(screen.getByTestId("input-reason"), { target: { value: "   " } });

    fireEvent.click(saveButton());

    const reason = screen.getByTestId("input-reason");
    expect(reason).toHaveAttribute("aria-invalid", "true");
    const describedBy = reason.getAttribute("aria-describedby");
    expect(describedBy).toBeTruthy();
    expect(document.getElementById(describedBy!.split(" ")[0])).toHaveTextContent(`${I}.reasonRequired`);
    expect(toast.error).not.toHaveBeenCalled();
    expect(apiPost).not.toHaveBeenCalled();
  });

  it("clears the reason error as soon as a reason is typed", async () => {
    await renderPage();
    fireEvent.click(screen.getByTestId("btn-new-item"));
    await pickType(`${I}.types.RECARGO`);
    fireEvent.click(saveButton());
    expect(screen.getByTestId("input-reason")).toHaveAttribute("aria-invalid", "true");

    fireEvent.change(screen.getByTestId("input-reason"), { target: { value: "urgente" } });

    expect(screen.getByTestId("input-reason")).not.toHaveAttribute("aria-invalid");
  });

  it("does not show a stale reason error when the dialog is opened again", async () => {
    await renderPage();
    fireEvent.click(screen.getByTestId("btn-new-item"));
    await pickType(`${I}.types.RECARGO`);
    fireEvent.click(saveButton());
    fireEvent.click(within(screen.getByRole("dialog")).getByRole("button", { name: /common\.cancel/ }));

    fireEvent.click(screen.getByTestId("btn-new-item"));
    await pickType(`${I}.types.RECARGO`);

    expect(screen.getByTestId("input-reason")).not.toHaveAttribute("aria-invalid");
  });

  it("sends a surcharge with its reason, and hides the reason again for a normal item", async () => {
    await renderPage();
    fireEvent.click(screen.getByTestId("btn-new-item"));
    expect(screen.queryByTestId("input-reason")).toBeNull();
    await pickType(`${I}.types.RECARGO`);
    fireEvent.change(screen.getByTestId("input-reason"), { target: { value: "urgente" } });

    fireEvent.click(saveButton());

    await waitFor(() => expect(apiPost).toHaveBeenCalledWith("/items", expect.objectContaining({ type: "RECARGO", reason: "urgente" })));

    fireEvent.click(screen.getByTestId("btn-new-item"));
    await pickType(`${I}.types.RECARGO`);
    await pickType(`${I}.types.NORMAL`);
    expect(screen.queryByTestId("input-reason")).toBeNull();
  });

  it("reports a failed save with the server message and keeps the dialog open", async () => {
    apiPost.mockRejectedValue(new ApiError(400, "/items", JSON.stringify({ message: "Budget does not exist" })));
    await renderPage();
    fireEvent.click(screen.getByTestId("btn-new-item"));
    fireEvent.change(await screen.findByTestId("input-budget-id"), { target: { value: "99" } });

    fireEvent.click(saveButton());

    await waitFor(() => expect(toast.error).toHaveBeenCalledWith("Budget does not exist"));
    expect(toast.success).not.toHaveBeenCalled();
    expect(saveButton()).toBeInTheDocument();
  });

  it("falls back to the generic message when the failure has no reason", async () => {
    apiPost.mockRejectedValue(new Error("boom"));
    await renderPage();
    fireEvent.click(screen.getByTestId("btn-new-item"));

    fireEvent.click(await screen.findByTestId("btn-save-item"));

    await waitFor(() => expect(toast.error).toHaveBeenCalledWith(`${I}.errorSave`));
  });

  it("starts empty after editing an item", async () => {
    await renderPage();
    fireEvent.click(editOf("Sellado"));
    expect(await screen.findByTestId("input-quantity")).toHaveValue("Sellado");
    fireEvent.click(screen.getByRole("button", { name: "common.cancel" }));
    await waitFor(() => expect(screen.queryByTestId("btn-save-item")).toBeNull());

    fireEvent.click(screen.getByTestId("btn-new-item"));

    expect(await screen.findByTestId("input-quantity")).toHaveValue("");
    expect(screen.getByTestId("input-budget-id")).toHaveValue(null);
    expect(screen.getByTestId("input-price")).toHaveValue(null);
  });
});

describe("edit", () => {
  it("prefills the item and its budget, and saves with PUT", async () => {
    await renderPage();
    fireEvent.click(editOf("Sellado"));
    expect(await screen.findByTestId("input-budget-id")).toHaveValue(12);
    expect(screen.getByTestId("input-price")).toHaveValue(50);
    fireEvent.change(screen.getByTestId("input-price"), { target: { value: "55" } });

    fireEvent.click(saveButton());

    await waitFor(() =>
      expect(apiPut).toHaveBeenCalledWith("/items/1", expect.objectContaining({ idItem: 1, value: 55, type: "NORMAL", fkIdBudget: { idBudget: 12 } })),
    );
    expect(toast.success).toHaveBeenCalledWith(`${I}.updated`);
    expect(apiPost).not.toHaveBeenCalled();
  });

  it("unlinks the budget when its field is cleared", async () => {
    await renderPage();
    fireEvent.click(editOf("Sellado"));
    fireEvent.change(await screen.findByTestId("input-budget-id"), { target: { value: "" } });

    fireEvent.click(saveButton());

    await waitFor(() => expect(apiPut).toHaveBeenCalledWith("/items/1", expect.objectContaining({ fkIdBudget: undefined })));
  });

  it("keeps the reason of a discount and treats an item without type as normal", async () => {
    await renderPage();
    fireEvent.click(editOf("Promo"));
    expect(await screen.findByTestId("input-reason")).toHaveValue("cliente frecuente");
    fireEvent.click(saveButton());
    await waitFor(() => expect(apiPut).toHaveBeenCalledWith("/items/2", expect.objectContaining({ type: "DESCUENTO", reason: "cliente frecuente" })));

    fireEvent.click(editOf("Sellado"));
    fireEvent.click(screen.getByRole("button", { name: "common.cancel" }));
    const untyped = screen.getAllByRole("row").find((r) => within(r).queryByText("3"))!;
    fireEvent.click(within(untyped).getByRole("button", { name: "common.edit" }));
    expect(screen.queryByTestId("input-reason")).toBeNull();
    fireEvent.click(await screen.findByTestId("btn-save-item"));

    await waitFor(() => expect(apiPut).toHaveBeenCalledWith("/items/3", expect.objectContaining({ type: "NORMAL" })));
  });
});

describe("delete", () => {
  it("deletes after confirmation", async () => {
    await renderPage();
    fireEvent.click(deleteOf("Sellado"));
    fireEvent.click(await screen.findByTestId("confirm-dialog-confirm"));

    await waitFor(() => expect(apiDelete).toHaveBeenCalledWith("/items/1"));
    expect(toast.success).toHaveBeenCalledWith(`${I}.deleted`);
  });

  it("warns that an item in use cannot be deleted and closes the question", async () => {
    apiDelete.mockRejectedValue(new ApiError(409, "/items/1", "in use"));
    await renderPage();
    fireEvent.click(deleteOf("Sellado"));
    fireEvent.click(await screen.findByTestId("confirm-dialog-confirm"));

    await waitFor(() => expect(toast.warning).toHaveBeenCalledWith("common.errors.inUse", expect.anything()));
    await waitFor(() => expect(screen.queryByTestId("confirm-dialog-confirm")).toBeNull());
    expect(toast.success).not.toHaveBeenCalled();
  });

  it("does not delete when the question is cancelled", async () => {
    await renderPage();
    fireEvent.click(deleteOf("Sellado"));
    fireEvent.click(await screen.findByRole("button", { name: "confirmDialog.cancel" }));

    expect(apiDelete).not.toHaveBeenCalled();
  });
});

describe("discounts and surcharges report", () => {
  it("is not requested until a budget is consulted", async () => {
    await renderPage();

    expect(apiGet).not.toHaveBeenCalledWith(expect.stringContaining("discounts-surcharges"));
  });

  it("lists the discounts and surcharges of the budget, with dashes for missing data", async () => {
    await renderPage();
    fireEvent.change(screen.getByTestId("input-report-budget-id"), { target: { value: "12" } });

    fireEvent.click(screen.getByTestId("btn-query-report"));

    const discount = (await screen.findByText("amigo")).closest("tr")!;
    expect(within(discount).getByText("Descuento")).toBeInTheDocument();
    expect(within(discount).getByText(`${I}.types.DESCUENTO`)).toBeInTheDocument();
    const surcharge = screen.getAllByRole("row").find((r) => within(r).queryByText(`${I}.types.RECARGO`))!;
    expect(within(surcharge).getAllByText("—")).toHaveLength(2);
  });

  it("says when the budget has none", async () => {
    await renderPage();
    fireEvent.change(screen.getByTestId("input-report-budget-id"), { target: { value: "99" } });

    fireEvent.click(screen.getByTestId("btn-query-report"));

    expect(await screen.findByText(`${I}.report.empty`)).toBeInTheDocument();
  });

  it("hides the report when the budget field is cleared and consulted again", async () => {
    await renderPage();
    fireEvent.change(screen.getByTestId("input-report-budget-id"), { target: { value: "12" } });
    fireEvent.click(screen.getByTestId("btn-query-report"));
    await screen.findByText("amigo");

    fireEvent.change(screen.getByTestId("input-report-budget-id"), { target: { value: "" } });
    fireEvent.click(screen.getByTestId("btn-query-report"));

    await waitFor(() => expect(screen.queryByText("amigo")).toBeNull());
  });
});
