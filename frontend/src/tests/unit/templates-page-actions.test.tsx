/**
 * Templates administration page (CU38, CU39, #1246): a budget template needs a procedure type and a concept,
 * keeps its key locked while editing and trims its notes; a document cost needs a document type and a
 * non-zero value and is stored as a fixed amount or a percentage; load, save and delete failures are reported.
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
  usePathname: () => "/dashboard/administration/templates",
  useSearchParams: () => new URLSearchParams(),
}));
vi.mock("next-intl", () => ({
  useTranslations: (ns: string) => (key: string, values?: Record<string, unknown>) =>
    values ? `${ns}.${key}:${JSON.stringify(values)}` : `${ns}.${key}`,
  useLocale: () => "es",
}));
vi.mock("@/app/dashboard/administration/templates/AllProcedureTemplates", () => ({
  AllProcedureTemplates: () => null,
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
import TemplatesPage from "@/app/dashboard/administration/templates/page";

const P = "administracion.plantillas";

const TEMPLATES = [
  {
    budgetTemplatePK: { fkIdProcedureType: 3, fkIdConcept: 9 },
    procedureType: { idProcedureType: 3, name: "Compraventa" },
    concept: { idConcept: 9, name: "Sellado" },
    notes: "nota",
  },
  { budgetTemplatePK: { fkIdProcedureType: 4, fkIdConcept: 10 } },
];

let costRows: unknown[] = [];

function route(path: string): Promise<unknown> {
  if (path === "/budget-templates") return Promise.resolve(TEMPLATES);
  if (path === "/procedure-types") {
    return Promise.resolve([{ idProcedureType: 3, name: "Compraventa" }, { idProcedureType: 4, name: "Poder" }]);
  }
  if (path === "/concepts") {
    return Promise.resolve([{ idConcept: 9, name: "Sellado", value: 50 }, { idConcept: 10, name: "Honorarios" }]);
  }
  if (path === "/document-types") {
    return Promise.resolve([{ idDocumentType: 20, name: "DNI", expires: false }]);
  }
  if (path.startsWith("/document-cost-templates/procedure-types/")) return Promise.resolve(costRows);
  return Promise.resolve([]);
}

function wrapper({ children }: { children: ReactNode }) {
  const client = new QueryClient({ defaultOptions: { queries: { retry: false } } });
  return <QueryClientProvider client={client}>{children}</QueryClientProvider>;
}

async function renderPage() {
  render(<TemplatesPage />, { wrapper });
  await screen.findByText("Sellado");
}

async function pick(trigger: string, option: string) {
  fireEvent.click(screen.getByTestId(trigger));
  fireEvent.click(await screen.findByRole("option", { name: option }));
}

const saveButton = () => screen.getByRole("button", { name: /common\.save/ });

beforeEach(() => {
  costRows = [];
  apiGet.mockReset().mockImplementation(route);
  apiPost.mockReset().mockResolvedValue({});
  apiPut.mockReset().mockResolvedValue({});
  apiDelete.mockReset().mockResolvedValue(undefined);
  Object.values(toast).forEach((fn) => fn.mockReset());
});

describe("budget template list", () => {
  it("falls back to the catalog names, a dash for missing notes and shows the concept price", async () => {
    await renderPage();

    const row = screen.getAllByRole("row").find((r) => within(r).queryByText("Poder"))!;
    expect(within(row).getByText("Honorarios")).toBeInTheDocument();
    expect(within(row).getByText("—")).toBeInTheDocument();

    fireEvent.click(screen.getByTestId("btn-new-template"));
    fireEvent.click(await screen.findByTestId("select-concept"));
    expect(await screen.findByRole("option", { name: "Sellado ($50)" })).toBeInTheDocument();
    expect(screen.getByRole("option", { name: "Honorarios" })).toBeInTheDocument();
  });

  it("reports a failed load and reloads on retry", async () => {
    apiGet.mockImplementation((path: string) =>
      path === "/budget-templates" ? Promise.reject(new Error("down")) : route(path),
    );
    render(<TemplatesPage />, { wrapper });

    fireEvent.click(await screen.findByTestId("btn-retry"));

    await waitFor(() => expect(apiGet.mock.calls.filter(([p]) => p === "/budget-templates").length).toBeGreaterThan(1));
  });
});

describe("budget template create", () => {
  it("refuses to save without a concept", async () => {
    await renderPage();
    fireEvent.click(screen.getByTestId("btn-new-template"));
    await pick("select-procedure-type", "Compraventa");

    fireEvent.click(saveButton());

    expect(within(screen.getByRole("dialog")).getAllByRole("alert").length).toBeGreaterThan(0);
    expect(screen.getByTestId("select-concept")).toHaveAttribute("aria-invalid", "true");
    expect(apiPost).not.toHaveBeenCalled();
  });

  it("creates with trimmed notes", async () => {
    await renderPage();
    fireEvent.click(screen.getByTestId("btn-new-template"));
    await pick("select-procedure-type", "Compraventa");
    await pick("select-concept", "Sellado ($50)");
    fireEvent.change(screen.getByTestId("input-notes-template"), { target: { value: "  urgente  " } });

    fireEvent.click(saveButton());

    await waitFor(() =>
      expect(apiPost).toHaveBeenCalledWith("/budget-templates", expect.objectContaining({ notes: "urgente" })),
    );
    expect(toast.success).toHaveBeenCalledWith(`${P}.created`);
    await waitFor(() => expect(screen.queryByTestId("input-notes-template")).toBeNull());
  });

  it("sends no notes when they are blank", async () => {
    await renderPage();
    fireEvent.click(screen.getByTestId("btn-new-template"));
    await pick("select-procedure-type", "Poder");
    await pick("select-concept", "Honorarios");
    fireEvent.change(screen.getByTestId("input-notes-template"), { target: { value: "   " } });

    fireEvent.click(saveButton());

    await waitFor(() => expect(apiPost).toHaveBeenCalledWith("/budget-templates", expect.objectContaining({ notes: undefined })));
  });

  it("reports a failed save and keeps the dialog open", async () => {
    apiPost.mockRejectedValue(new ApiError(500, "/budget-templates", "boom"));
    await renderPage();
    fireEvent.click(screen.getByTestId("btn-new-template"));
    await pick("select-procedure-type", "Poder");
    await pick("select-concept", "Honorarios");

    fireEvent.click(saveButton());

    await waitFor(() => expect(toast.error).toHaveBeenCalled());
    expect(toast.success).not.toHaveBeenCalled();
    expect(screen.getByTestId("input-notes-template")).toBeInTheDocument();
  });

  it("starts empty after an edit and cancels without saving", async () => {
    await renderPage();
    fireEvent.click(screen.getAllByTestId("btn-edit-template")[0]);
    expect(await screen.findByTestId("input-notes-template")).toHaveValue("nota");
    fireEvent.click(screen.getByRole("button", { name: /common\.cancel/ }));
    await waitFor(() => expect(screen.queryByTestId("input-notes-template")).toBeNull());

    fireEvent.click(screen.getByTestId("btn-new-template"));

    expect(await screen.findByTestId("input-notes-template")).toHaveValue("");
    expect(screen.getByTestId("select-procedure-type")).not.toBeDisabled();
    expect(apiPut).not.toHaveBeenCalled();
  });
});

describe("budget template edit and delete", () => {
  it("edits a template without notes and reports the update", async () => {
    await renderPage();
    fireEvent.click(screen.getAllByTestId("btn-edit-template")[1]);
    expect(await screen.findByTestId("input-notes-template")).toHaveValue("");

    fireEvent.click(screen.getByRole("button", { name: /common\.update/ }));

    await waitFor(() => expect(apiPut).toHaveBeenCalledWith("/budget-templates/procedure-types/4/concept/10", expect.anything()));
    expect(toast.success).toHaveBeenCalledWith(`${P}.updated`);
  });

  it("deletes after confirmation and reports it", async () => {
    await renderPage();
    fireEvent.click(screen.getAllByTestId("btn-delete-template")[0]);
    fireEvent.click(await screen.findByTestId("confirm-dialog-confirm"));

    await waitFor(() => expect(apiDelete).toHaveBeenCalledWith("/budget-templates/procedure-types/3/concept/9"));
    expect(toast.success).toHaveBeenCalledWith(`${P}.deleted`);
  });

  it("warns that a template in use cannot be deleted", async () => {
    apiDelete.mockRejectedValue(new ApiError(409, "/budget-templates", "in use"));
    await renderPage();
    fireEvent.click(screen.getAllByTestId("btn-delete-template")[0]);
    fireEvent.click(await screen.findByTestId("confirm-dialog-confirm"));

    await waitFor(() => expect(toast.warning).toHaveBeenCalledWith("common.errors.inUse", expect.anything()));
    expect(toast.success).not.toHaveBeenCalled();
  });

  it("does not delete when the confirmation is cancelled", async () => {
    await renderPage();
    fireEvent.click(screen.getAllByTestId("btn-delete-template")[0]);
    fireEvent.click(await screen.findByRole("button", { name: "confirmDialog.cancel" }));

    expect(apiDelete).not.toHaveBeenCalled();
  });
});

describe("document cost templates", () => {
  async function openCosts() {
    await renderPage();
    await pick("select-procedure-type-costs", "Compraventa");
  }

  it("lists the costs of the chosen procedure type, as amount, percentage or unknown document", async () => {
    costRows = [
      { documentCostTemplatePK: { fkIdProcedureType: 3, fkIdDocumentType: 20 }, fixedAmount: 80 },
      { documentCostTemplatePK: { fkIdProcedureType: 3, fkIdDocumentType: 21 }, variablePercentage: 2.5 },
    ];
    await openCosts();

    await waitFor(() => expect(apiGet).toHaveBeenCalledWith("/document-cost-templates/procedure-types/3"));
    const fixed = (await screen.findByText("$80")).closest("tr")!;
    expect(within(fixed).getByText("DNI")).toBeInTheDocument();
    expect(within(fixed).getByText("—")).toBeInTheDocument();
    const variable = screen.getByText("2.5%").closest("tr")!;
    expect(within(variable).getByText("#21")).toBeInTheDocument();
  });

  it("refuses to save a cost without a document type or a value", async () => {
    await openCosts();
    fireEvent.click(await screen.findByTestId("btn-new-cost-document"));
    fireEvent.change(await screen.findByTestId("input-value-cost-document"), { target: { value: "10" } });
    fireEvent.click(screen.getByTestId("btn-save-cost-document"));
    expect(within(screen.getByRole("dialog")).getAllByRole("alert").length).toBeGreaterThan(0);
    expect(screen.getByTestId("select-document-type-cost")).toHaveAttribute("aria-invalid", "true");

    await pick("select-document-type-cost", "DNI");
    fireEvent.change(screen.getByTestId("input-value-cost-document"), { target: { value: "0" } });
    fireEvent.click(screen.getByTestId("btn-save-cost-document"));

    expect(within(screen.getByRole("dialog")).getAllByRole("alert").length).toBeGreaterThan(0);
    expect(screen.getByTestId("input-value-cost-document")).toHaveAttribute("aria-invalid", "true");
    expect(apiPost).not.toHaveBeenCalled();
  });

  it("stores a fixed amount by default", async () => {
    await openCosts();
    fireEvent.click(await screen.findByTestId("btn-new-cost-document"));
    await pick("select-document-type-cost", "DNI");
    fireEvent.change(screen.getByTestId("input-value-cost-document"), { target: { value: "80" } });

    fireEvent.click(screen.getByTestId("btn-save-cost-document"));

    await waitFor(() =>
      expect(apiPost).toHaveBeenCalledWith("/document-cost-templates", {
        idProcedureType: 3, idDocumentType: 20, fixedAmount: 80, variablePercentage: null,
      }),
    );
    expect(toast.success).toHaveBeenCalledWith(`${P}.costosDocumento.created`);
  });

  it("stores a percentage when the variable kind is chosen", async () => {
    await openCosts();
    fireEvent.click(await screen.findByTestId("btn-new-cost-document"));
    await pick("select-document-type-cost", "DNI");
    fireEvent.click(screen.getByLabelText(`${P}.costosDocumento.fields.porcentajeVariable`));
    fireEvent.change(screen.getByTestId("input-value-cost-document"), { target: { value: "2.5" } });

    fireEvent.click(screen.getByTestId("btn-save-cost-document"));

    await waitFor(() =>
      expect(apiPost).toHaveBeenCalledWith("/document-cost-templates", {
        idProcedureType: 3, idDocumentType: 20, fixedAmount: null, variablePercentage: 2.5,
      }),
    );
  });

  it("reports a failed cost save and keeps the dialog open", async () => {
    apiPost.mockRejectedValue(new ApiError(500, "/document-cost-templates", "boom"));
    await openCosts();
    fireEvent.click(await screen.findByTestId("btn-new-cost-document"));
    await pick("select-document-type-cost", "DNI");
    fireEvent.change(screen.getByTestId("input-value-cost-document"), { target: { value: "80" } });

    fireEvent.click(screen.getByTestId("btn-save-cost-document"));

    await waitFor(() => expect(toast.error).toHaveBeenCalled());
    expect(toast.success).not.toHaveBeenCalled();
    expect(screen.getByTestId("btn-save-cost-document")).toBeInTheDocument();
  });

  it("resets the cost form each time it opens", async () => {
    await openCosts();
    fireEvent.click(await screen.findByTestId("btn-new-cost-document"));
    fireEvent.change(await screen.findByTestId("input-value-cost-document"), { target: { value: "55" } });
    fireEvent.click(screen.getAllByRole("button", { name: /common\.cancel/ })[0]);
    await waitFor(() => expect(screen.queryByTestId("input-value-cost-document")).toBeNull());

    fireEvent.click(screen.getByTestId("btn-new-cost-document"));

    expect(await screen.findByTestId("input-value-cost-document")).toHaveValue(null);
  });
});
