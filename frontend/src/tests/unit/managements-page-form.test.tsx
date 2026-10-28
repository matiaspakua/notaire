/**
 * Managements create, edit, transition and client filter (CU13, CU53, CU83, #1246): create sends the
 * chosen values, edit prefills from the complete case and keeps the status locked, a note added by an
 * active substitution is announced, a failed save is reported, a transition offers only the valid
 * destinations of the current step, and the client filter replaces the paged list.
 */
import { describe, it, expect, vi, beforeEach } from "vitest";
import { fireEvent, render, screen, waitFor } from "@testing-library/react";
import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import type { ReactNode } from "react";

const apiGet = vi.fn();
const apiGetPage = vi.fn();
const apiPost = vi.fn();
const apiPut = vi.fn();
const toast = vi.hoisted(() => ({ success: vi.fn(), error: vi.fn(), warning: vi.fn(), info: vi.fn() }));

vi.mock("@/lib/api-client", async (orig) => ({
  ...(await orig<typeof import("@/lib/api-client")>()),
  apiGet: (path: string) => apiGet(path),
  apiGetPage: (path: string, request: unknown) => apiGetPage(path, request),
  apiPost: (path: string, body: unknown) => apiPost(path, body),
  apiPut: (path: string, body: unknown) => apiPut(path, body),
}));
vi.mock("sonner", () => ({ toast }));
vi.mock("next/navigation", () => ({
  useRouter: () => ({ push: vi.fn(), replace: vi.fn() }),
  usePathname: () => "/dashboard/managements",
  useSearchParams: () => new URLSearchParams(),
}));
vi.mock("next-intl", () => ({
  useTranslations: (ns: string) => (key: string, values?: Record<string, unknown>) =>
    values ? `${ns}.${key}:${JSON.stringify(values)}` : `${ns}.${key}`,
  useLocale: () => "es",
}));
vi.mock("@/components/shared/PersonPicker", () => ({
  PersonPicker: (props: {
    onChange: (p: { personId: number } | undefined) => void;
    "data-testid"?: string;
    value?: number;
  }) => (
    <button type="button" data-testid={props["data-testid"]} onClick={() => props.onChange({ personId: 31 })}>
      {`person:${props.value ?? "none"}`}
    </button>
  ),
}));
vi.mock("@/components/shared/BudgetPicker", () => ({
  BudgetPicker: (props: {
    onChange: (b: { idBudget: number } | undefined) => void;
    "data-testid"?: string;
    value?: number;
  }) => (
    <button type="button" data-testid={props["data-testid"]} onClick={() => props.onChange({ idBudget: 21 })}>
      {`budget:${props.value ?? "none"}`}
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
import ManagementsPage from "@/app/dashboard/managements/page";

const ROWS = [{ idManagement: 1, number: 5001, currentStatus: "Iniciada", procedureCount: 2 }];
const CLIENT_ROWS = [{ idManagement: 8, number: 7001, currentStatus: "Iniciada", procedureCount: 0 }];

let completeCase: Record<string, unknown> = {};

function wrapper({ children }: { children: ReactNode }) {
  const client = new QueryClient({ defaultOptions: { queries: { retry: false } } });
  return <QueryClientProvider client={client}>{children}</QueryClientProvider>;
}

function route(path: string) {
  if (path === "/management-statuses") {
    return Promise.resolve([{ idManagementStatus: 4, name: "Iniciada" }, { idManagementStatus: 5, name: "Archivada" }]);
  }
  if (path === "/procedure-types") return Promise.resolve([{ idProcedureType: 6, name: "Compraventa" }]);
  if (path === "/properties") return Promise.resolve([{ idProperty: 9, address: "Calle 1" }]);
  if (path === "/managements/1/complete-case") return Promise.resolve(completeCase);
  if (path === "/managements/1/workflow-trace") {
    return Promise.resolve({
      managementId: 1,
      currentStatus: "Iniciada",
      nodes: [
        { id: 1, statusManagementName: "Iniciada" },
        { id: 2, statusManagementName: "Documentación" },
        { id: 3, statusManagementName: null },
      ],
      transitions: [
        { id: 1, originNodeId: 1, destinationNodeId: 2 },
        { id: 2, originNodeId: 1, destinationNodeId: 3 },
        { id: 3, originNodeId: 2, destinationNodeId: 1 },
      ],
      history: [],
      nodeStatuses: {},
    });
  }
  if (path === "/managements/client/31") return Promise.resolve(CLIENT_ROWS);
  return Promise.resolve([]);
}

async function renderPage() {
  render(<ManagementsPage />, { wrapper });
  await screen.findByText("5001");
}

async function choose(testId: string, name: string) {
  fireEvent.click(screen.getByTestId(testId));
  fireEvent.click(await screen.findByRole("option", { name }));
}

beforeEach(() => {
  completeCase = {
    idManagement: 1,
    number: 5001,
    heading: "Encabezado",
    notes: "nota",
    budgetId: 21,
    notaryId: 31,
    statusManagementId: 4,
    typeProcedureId: 6,
    propertyId: 9,
  };
  apiGet.mockReset().mockImplementation(route);
  apiGetPage.mockReset().mockResolvedValue({ content: ROWS, number: 0, size: 20, totalElements: 1, totalPages: 1 });
  apiPost.mockReset().mockResolvedValue({});
  apiPut.mockReset().mockResolvedValue({});
  Object.values(toast).forEach((fn) => fn.mockReset());
});

async function fillCreateForm(withProperty: boolean) {
  fireEvent.click(screen.getByTestId("btn-new-management"));
  fireEvent.change(await screen.findByTestId("input-management-number"), { target: { value: "5100" } });
  fireEvent.click(screen.getByTestId("select-budget-management"));
  fireEvent.click(screen.getByTestId("select-notary-management"));
  await choose("select-status-management", "Iniciada");
  await choose("select-procedure-type-management", "Compraventa");
  if (withProperty) await choose("select-property-management", "Calle 1");
}

describe("create", () => {
  it("sends the chosen values to the complete-case endpoint and confirms", async () => {
    await renderPage();
    await fillCreateForm(true);

    fireEvent.click(screen.getByTestId("btn-save-management"));

    await waitFor(() =>
      expect(apiPost).toHaveBeenCalledWith("/managements/complete-case", {
        number: 5100,
        budgetId: 21,
        notaryId: 31,
        statusManagementId: 4,
        typeProcedureId: 6,
        propertyId: 9,
      }),
    );
    expect(toast.success).toHaveBeenCalledWith("gestiones.created");
    expect(toast.info).not.toHaveBeenCalled();
  });

  it("leaves the property out when none is chosen", async () => {
    await renderPage();
    await fillCreateForm(false);

    fireEvent.click(screen.getByTestId("btn-save-management"));

    await waitFor(() => expect(apiPost).toHaveBeenCalled());
    expect(apiPost.mock.calls[0][1]).toMatchObject({ propertyId: undefined });
  });

  it("announces a redirect by an active substitution", async () => {
    apiPost.mockResolvedValue({ notes: "Gestión redirigida por suplencia activa a Ana" });
    await renderPage();
    await fillCreateForm(false);

    fireEvent.click(screen.getByTestId("btn-save-management"));

    await waitFor(() =>
      expect(toast.info).toHaveBeenCalledWith("gestiones.suplenciaRedirected", {
        description: "Gestión redirigida por suplencia activa a Ana",
      }),
    );
  });

  it("reports a failed save and keeps the form open (#1951)", async () => {
    apiPost.mockRejectedValue(new Error("boom"));
    await renderPage();
    await fillCreateForm(false);

    fireEvent.click(screen.getByTestId("btn-save-management"));

    await waitFor(() => expect(toast.error).toHaveBeenCalledWith("gestiones.errorSave"));
    expect(toast.success).not.toHaveBeenCalled();
    expect(screen.getByTestId("btn-save-management")).toBeDefined();
  });

  it("starts empty every time the form opens", async () => {
    await renderPage();
    await fillCreateForm(false);
    fireEvent.click(screen.getByRole("button", { name: "common.cancel" }));
    await waitFor(() => expect(screen.queryByTestId("input-management-number")).toBeNull());

    fireEvent.click(screen.getByTestId("btn-new-management"));

    expect(await screen.findByTestId("input-management-number")).toHaveValue(null);
    expect(screen.getByTestId("select-budget-management")).toHaveTextContent("budget:none");
  });
});

describe("edit", () => {
  it("prefills from the complete case, locks the status and keeps heading and notes on save", async () => {
    await renderPage();

    fireEvent.click(screen.getByTestId("btn-edit-management-1"));

    await waitFor(() => expect(screen.getByTestId("select-budget-management")).toHaveTextContent("budget:21"));
    expect(screen.getByTestId("input-management-number")).toHaveValue(5001);
    expect(screen.getByTestId("select-notary-management")).toHaveTextContent("person:31");
    expect(screen.getByTestId("select-status-management")).toBeDisabled();
    expect(screen.getByText("gestiones.estadoViaTransition")).toBeInTheDocument();

    fireEvent.change(screen.getByTestId("input-management-number"), { target: { value: "5002" } });
    fireEvent.click(screen.getByTestId("btn-save-management"));

    await waitFor(() =>
      expect(apiPut).toHaveBeenCalledWith("/managements/1/complete-case", {
        number: 5002,
        heading: "Encabezado",
        notes: "nota",
        budgetId: 21,
        notaryId: 31,
        statusManagementId: 4,
        typeProcedureId: 6,
        propertyId: 9,
      }),
    );
    expect(toast.success).toHaveBeenCalledWith("gestiones.updated");
    expect(toast.info).not.toHaveBeenCalled();
  });

  it("keeps save disabled until the complete case has loaded", async () => {
    let release: (value: unknown) => void = () => undefined;
    apiGet.mockImplementation((path: string) =>
      path === "/managements/1/complete-case" ? new Promise((resolve) => { release = resolve; }) : route(path),
    );
    await renderPage();

    fireEvent.click(screen.getByTestId("btn-edit-management-1"));

    expect(await screen.findByTestId("btn-save-management")).toBeDisabled();
    release(completeCase);
    await waitFor(() => expect(screen.getByTestId("btn-save-management")).toBeEnabled());
  });

  it("announces only a redirect note the save added", async () => {
    completeCase = { ...completeCase, notes: "Gestión redirigida por suplencia activa a Ana" };
    apiPut.mockResolvedValue({ notes: "Gestión redirigida por suplencia activa a Ana" });
    await renderPage();
    fireEvent.click(screen.getByTestId("btn-edit-management-1"));
    await waitFor(() => expect(screen.getByTestId("select-budget-management")).toHaveTextContent("budget:21"));

    fireEvent.click(screen.getByTestId("btn-save-management"));

    await waitFor(() => expect(toast.success).toHaveBeenCalledWith("gestiones.updated"));
    expect(toast.info).not.toHaveBeenCalled();
  });

  it("announces a redirect note added by the save", async () => {
    apiPut.mockResolvedValue({ notes: "Gestión redirigida por suplencia activa a Ana" });
    await renderPage();
    fireEvent.click(screen.getByTestId("btn-edit-management-1"));
    await waitFor(() => expect(screen.getByTestId("select-budget-management")).toHaveTextContent("budget:21"));

    fireEvent.click(screen.getByTestId("btn-save-management"));

    await waitFor(() => expect(toast.info).toHaveBeenCalled());
  });
});

describe("transition", () => {
  it("offers only the named destinations of the current step and sends the chosen one", async () => {
    await renderPage();
    fireEvent.click(screen.getByTestId("btn-change-status-1"));
    expect(await screen.findByTestId("btn-confirm-transition")).toBeDisabled();

    fireEvent.click(await screen.findByTestId("select-new-status"));
    expect(await screen.findAllByRole("option")).toHaveLength(1);
    fireEvent.click(screen.getByRole("option", { name: "Documentación" }));
    fireEvent.click(screen.getByTestId("btn-confirm-transition"));

    await waitFor(() =>
      expect(apiPost).toHaveBeenCalledWith("/managements/1/transition", { statusDestination: "Documentación" }),
    );
    expect(toast.success).toHaveBeenCalledWith("gestiones.transitioned");
  });

  it("shows the server message when the transition is refused and keeps the dialog open", async () => {
    apiPost.mockRejectedValue(new ApiError(422, "refused", JSON.stringify({ message: "Falta documentación" })));
    await renderPage();
    fireEvent.click(screen.getByTestId("btn-change-status-1"));
    fireEvent.click(await screen.findByTestId("select-new-status"));
    fireEvent.click(await screen.findByRole("option", { name: "Documentación" }));

    fireEvent.click(screen.getByTestId("btn-confirm-transition"));

    await waitFor(() => expect(toast.error).toHaveBeenCalled());
    expect(toast.success).not.toHaveBeenCalled();
    expect(screen.getByTestId("btn-confirm-transition")).toBeInTheDocument();
  });

  it("closes the dialog and forgets the choice on cancel", async () => {
    await renderPage();
    fireEvent.click(screen.getByTestId("btn-change-status-1"));
    fireEvent.click(await screen.findByTestId("select-new-status"));
    fireEvent.click(await screen.findByRole("option", { name: "Documentación" }));

    fireEvent.click(screen.getByRole("button", { name: "common.cancel" }));
    await waitFor(() => expect(screen.queryByTestId("btn-confirm-transition")).toBeNull());
    fireEvent.click(screen.getByTestId("btn-change-status-1"));

    expect(await screen.findByTestId("btn-confirm-transition")).toBeDisabled();
  });
});

describe("client filter", () => {
  it("replaces the paged list by the client's managements and hides the pagination", async () => {
    await renderPage();

    fireEvent.click(screen.getByTestId("select-filter-client-management"));

    expect(await screen.findByText("7001")).toBeInTheDocument();
    expect(screen.queryByText("5001")).toBeNull();
    expect(apiGet).toHaveBeenCalledWith("/managements/client/31");
  });
});
