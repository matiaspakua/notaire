/**
 * Folios administration page (CU87, #1246): the folio form needs a number, a type and a notary, a folio is
 * created, edited and deleted (failures keep the dialog), the list narrows by status, a deed linked to another
 * folio cannot be linked again, folio types are searched, saved, edited and deleted, and load errors retry.
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
  usePathname: () => "/dashboard/administration/folios",
  useSearchParams: () => new URLSearchParams(),
}));
vi.mock("next-intl", () => ({
  useTranslations: (ns: string) => (key: string) => `${ns}.${key}`,
  useLocale: () => "es",
}));
vi.mock("@/components/shared/DeedPicker", () => ({
  DeedPicker: ({ onChange, exclude }: {
    onChange: (deed: { idDeed: number } | undefined) => void;
    exclude?: (deed: { idDeed: number }) => boolean;
  }) => (
    <div>
      <button type="button" data-testid="pick-deed" onClick={() => onChange({ idDeed: 55 })} />
      <button type="button" data-testid="clear-deed" onClick={() => onChange(undefined)} />
      <span data-testid="excludes-20">{String(exclude?.({ idDeed: 20 }))}</span>
      <span data-testid="excludes-99">{String(exclude?.({ idDeed: 99 }))}</span>
    </div>
  ),
}));
vi.mock("@/components/ui/select", async () => {
  const React = await import("react");
  const Ctx = React.createContext<{
    value: string;
    onValueChange: (v: string) => void;
    open: boolean;
    setOpen: (o: boolean) => void;
  }>({ value: "", onValueChange: () => undefined, open: false, setOpen: () => undefined });
  return {
    Select: ({ value, onValueChange, children }: {
      value: string; onValueChange: (v: string) => void; children: ReactNode;
    }) => {
      const [open, setOpen] = React.useState(false);
      return <Ctx.Provider value={{ value, onValueChange, open, setOpen }}>{children}</Ctx.Provider>;
    },
    SelectTrigger: ({ children, ...rest }: { children: ReactNode; "data-testid"?: string }) => {
      const ctx = React.useContext(Ctx);
      return <button type="button" onClick={() => ctx.setOpen(!ctx.open)} {...rest}>{children}</button>;
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
import FoliosAdminPage from "@/app/dashboard/administration/folios/page";

const FOLIOS = [
  {
    idFolio: 1, number: 1001, year: 2026, status: "Nuevo", notes: "n1",
    fkIdFolioType: { idFolioType: 7, name: "Protocolo" }, fkIdNotaryPerson: { personId: 5 },
    fkIdDeed: { idDeed: 20, number: 300 },
  },
  { idFolio: 2, number: 1002, status: "Utilizado" },
  { idFolio: 3, number: 1003, year: 2025, fkIdDeed: { idDeed: 21 } },
];
const ERRORED = [{ idFolio: 4, number: 1004, status: "Errose" }];
const FOLIO_TYPES = [
  { idFolioType: 7, name: "Protocolo", isAuxiliary: false, notes: "n", enabled: true, version: 3 },
  { idFolioType: 8, name: "Auxiliar", isAuxiliary: true },
];
const NOTARIES = [{ personId: 5, firstName: "Ana", lastName: "Pérez" }];

let failFolios = false;
let typeInUse = false;
let typeInUseFails = false;
function route(path: string): Promise<unknown> {
  if (path === "/folios") return failFolios ? Promise.reject(new ApiError(500, path, "")) : Promise.resolve(FOLIOS);
  if (path === "/folios/search?status=Errose") return Promise.resolve(ERRORED);
  if (path === "/folio-types/search?name=aux") return Promise.resolve([FOLIO_TYPES[1]]);
  if (path.endsWith("/in-use")) {
    return typeInUseFails ? Promise.reject(new ApiError(500, path, "")) : Promise.resolve({ inUse: typeInUse });
  }
  if (path === "/folio-types") return Promise.resolve(FOLIO_TYPES);
  if (path === "/deeds/available-notaries") return Promise.resolve(NOTARIES);
  return Promise.resolve([]);
}

function wrapper({ children }: { children: ReactNode }) {
  const client = new QueryClient({ defaultOptions: { queries: { retry: false } } });
  return <QueryClientProvider client={client}>{children}</QueryClientProvider>;
}

async function renderPage() {
  render(<FoliosAdminPage />, { wrapper });
  await screen.findByText("1001");
  await screen.findByText("Auxiliar", { selector: "span.font-medium" });
}

beforeEach(() => {
  failFolios = false;
  typeInUse = false;
  typeInUseFails = false;
  apiGet.mockReset().mockImplementation(route);
  apiPost.mockReset().mockResolvedValue({});
  apiPut.mockReset().mockResolvedValue({});
  apiDelete.mockReset().mockResolvedValue(undefined);
  Object.values(toast).forEach((fn) => fn.mockReset());
});

const rowOf = (text: string) => screen.getAllByRole("row").find((row) => within(row).queryByText(text))!;
const typeRowOf = (name: string) =>
  screen.getAllByRole("row").find((row) => within(row).queryByText(name, { selector: "span.font-medium" })
    && within(row).queryByTestId("btn-edit-folio-type"))!;
const yearInput = () => screen.getAllByRole("dialog")[0].querySelectorAll<HTMLInputElement>('input[type="number"]')[1];
const saveButton = () => within(screen.getByRole("dialog")).getByRole("button", { name: /common\.(save|update)/ });

async function choose(trigger: string, option: string | RegExp) {
  fireEvent.click(screen.getByTestId(trigger));
  fireEvent.click(await screen.findByRole("option", { name: option }));
}

async function fillRequired() {
  fireEvent.change(screen.getByTestId("input-folio-number"), { target: { value: "2001" } });
  await choose("select-folio-type", "Protocolo");
  await choose("select-notary-folio", /Pérez/);
}

describe("folio form", () => {
  it("creates a folio with every chosen value", async () => {
    await renderPage();
    fireEvent.click(screen.getByTestId("btn-new-folio"));
    await screen.findByTestId("input-folio-number");

    await fillRequired();
    fireEvent.change(yearInput(), { target: { value: "2027" } });
    await choose("select-status-folio", "Errose");
    fireEvent.click(screen.getByTestId("pick-deed"));
    fireEvent.change(screen.getByLabelText("common.observations"), { target: { value: "obs" } });
    fireEvent.click(saveButton());

    await waitFor(() =>
      expect(apiPost).toHaveBeenCalledWith("/folios", {
        number: 2001, year: 2027, status: "Errose", notes: "obs", typeFolioId: 7, notaryId: 5, deedId: 55,
      }),
    );
    expect(toast.success).toHaveBeenCalledWith("administracion.folios.created");
    await waitFor(() => expect(screen.queryByTestId("input-folio-number")).toBeNull());
  });

  it("sends no year when it is cleared and no deed when the link is cleared", async () => {
    await renderPage();
    fireEvent.click(screen.getByTestId("btn-new-folio"));
    await screen.findByTestId("input-folio-number");
    await fillRequired();

    fireEvent.change(yearInput(), { target: { value: "" } });
    fireEvent.click(screen.getByTestId("pick-deed"));
    fireEvent.click(screen.getByTestId("clear-deed"));
    fireEvent.click(saveButton());

    await waitFor(() =>
      expect(apiPost).toHaveBeenCalledWith("/folios", expect.objectContaining({ year: undefined, deedId: undefined })),
    );
  });

  it("asks for a notary once number and type are set", async () => {
    await renderPage();
    fireEvent.click(screen.getByTestId("btn-new-folio"));
    await screen.findByTestId("input-folio-number");
    fireEvent.change(screen.getByTestId("input-folio-number"), { target: { value: "2001" } });
    await choose("select-folio-type", "Protocolo");

    fireEvent.click(saveButton());

    expect(toast.error).toHaveBeenLastCalledWith(expect.stringContaining("administracion.folios.fields.escribano"));
    expect(apiPost).not.toHaveBeenCalled();
  });

  it("empties the number when the field is cleared and asks for it again", async () => {
    await renderPage();
    fireEvent.click(screen.getByTestId("btn-new-folio"));
    const number = await screen.findByTestId("input-folio-number");
    fireEvent.change(number, { target: { value: "5" } });
    fireEvent.change(number, { target: { value: "" } });

    fireEvent.click(saveButton());

    expect(toast.error).toHaveBeenLastCalledWith("administracion.folios.numberRequired");
  });

  it("reports a failed save with the fallback message and keeps the dialog open", async () => {
    apiPost.mockRejectedValue(new ApiError(500, "/folios", ""));
    await renderPage();
    fireEvent.click(screen.getByTestId("btn-new-folio"));
    await screen.findByTestId("input-folio-number");
    await fillRequired();

    fireEvent.click(saveButton());

    await waitFor(() => expect(toast.error).toHaveBeenCalledWith("administracion.folios.errorCreate"));
    expect(toast.success).not.toHaveBeenCalled();
    expect(screen.getByTestId("input-folio-number")).toBeInTheDocument();
  });

  it("shows the server's reason when it gives one", async () => {
    apiPost.mockRejectedValue(new ApiError(409, "/folios", '{"message":"Folio repetido"}'));
    await renderPage();
    fireEvent.click(screen.getByTestId("btn-new-folio"));
    await screen.findByTestId("input-folio-number");
    await fillRequired();

    fireEvent.click(saveButton());

    await waitFor(() => expect(toast.error).toHaveBeenCalled());
    expect(toast.error).not.toHaveBeenCalledWith("administracion.folios.errorCreate");
  });

  it("edits a folio with its loaded values and saves with PUT", async () => {
    await renderPage();
    fireEvent.click(within(rowOf("1001")).getByTestId("btn-edit-folio"));

    expect(await screen.findByTestId("input-folio-number")).toHaveValue(1001);
    expect(yearInput()).toHaveValue(2026);
    expect(screen.getByLabelText("common.observations")).toHaveValue("n1");
    fireEvent.change(screen.getByTestId("input-folio-number"), { target: { value: "1010" } });
    fireEvent.click(saveButton());

    await waitFor(() =>
      expect(apiPut).toHaveBeenCalledWith("/folios/1", {
        number: 1010, year: 2026, status: "Nuevo", notes: "n1", typeFolioId: 7, notaryId: 5, deedId: 20,
      }),
    );
    expect(toast.success).toHaveBeenCalledWith("administracion.folios.updated");
    expect(apiPost).not.toHaveBeenCalled();
  });

  it("edits a folio that lacks status, notes, type and notary and asks for the type", async () => {
    await renderPage();
    fireEvent.click(within(rowOf("1003")).getByTestId("btn-edit-folio"));
    await screen.findByTestId("input-folio-number");
    expect(screen.getByLabelText("common.observations")).toHaveValue("");

    fireEvent.click(saveButton());

    expect(toast.error).toHaveBeenLastCalledWith(expect.stringContaining("administracion.folios.fields.tipo"));
    expect(apiPut).not.toHaveBeenCalled();
  });

  it("lets a folio keep its own deed but not take one linked to another folio", async () => {
    await renderPage();
    fireEvent.click(screen.getByTestId("btn-new-folio"));
    await screen.findByTestId("pick-deed");
    expect(screen.getByTestId("excludes-20")).toHaveTextContent("true");
    expect(screen.getByTestId("excludes-99")).toHaveTextContent("false");
    fireEvent.click(within(screen.getByRole("dialog")).getByRole("button", { name: /common\.cancel/ }));
    await waitFor(() => expect(screen.queryByTestId("pick-deed")).toBeNull());

    fireEvent.click(within(rowOf("1001")).getByTestId("btn-edit-folio"));

    await screen.findByTestId("pick-deed");
    expect(screen.getByTestId("excludes-20")).toHaveTextContent("false");
  });
});

describe("folio list", () => {
  it("shows year, type and deed, and a dash where they are missing", async () => {
    await renderPage();

    expect(within(rowOf("1001")).getByText("Nº 300")).toBeInTheDocument();
    expect(within(rowOf("1001")).getByText("Protocolo")).toBeInTheDocument();
    expect(within(rowOf("1002")).getAllByText("—")).toHaveLength(3);
    expect(within(rowOf("1003")).getByText("2025")).toBeInTheDocument();
    expect(within(rowOf("1003")).queryByText(/Nº/)).toBeNull();
  });

  it("narrows by status on the server and goes back to every folio", async () => {
    await renderPage();

    await choose("select-filter-status-folio", "Errose");
    expect(await screen.findByText("1004")).toBeInTheDocument();
    expect(apiGet).toHaveBeenCalledWith("/folios/search?status=Errose");
    expect(screen.queryByText("1001")).toBeNull();

    await choose("select-filter-status-folio", "common.all");
    expect(await screen.findByText("1001")).toBeInTheDocument();
  });

  it("deletes nothing when the confirmation is dismissed", async () => {
    await renderPage();
    fireEvent.click(within(rowOf("1001")).getByTestId("btn-delete-folio"));
    await screen.findByTestId("confirm-dialog-confirm");

    fireEvent.keyDown(screen.getByRole("alertdialog"), { key: "Escape" });

    await waitFor(() => expect(screen.queryByTestId("confirm-dialog-confirm")).toBeNull());
    expect(apiDelete).not.toHaveBeenCalled();
  });

  it("reports a folio that cannot be deleted", async () => {
    apiDelete.mockRejectedValue(new ApiError(409, "/folios/1", "in use"));
    await renderPage();
    fireEvent.click(within(rowOf("1001")).getByTestId("btn-delete-folio"));
    fireEvent.click(await screen.findByTestId("confirm-dialog-confirm"));

    await waitFor(() => expect(toast.warning.mock.calls.length + toast.error.mock.calls.length).toBeGreaterThan(0));
    expect(toast.success).not.toHaveBeenCalled();
  });

  it("shows the load error and retries", async () => {
    failFolios = true;
    render(<FoliosAdminPage />, { wrapper });

    expect(await screen.findByText("common.loadError")).toBeInTheDocument();
    failFolios = false;
    fireEvent.click(screen.getByRole("button", { name: /retry|reintentar/i }));

    expect(await screen.findByText("1001")).toBeInTheDocument();
  });
});

describe("folio types", () => {
  it("marks an auxiliary type and searches the catalog by name", async () => {
    await renderPage();
    expect(within(typeRowOf("Auxiliar")).getByText("administracion.folios.tiposDeFolio.esAuxiliar", { selector: "div,span" }))
      .toBeInTheDocument();

    fireEvent.change(screen.getByTestId("input-search-folio-type"), { target: { value: "aux" } });

    await waitFor(() => expect(apiGet).toHaveBeenCalledWith("/folio-types/search?name=aux"));
    await waitFor(() => expect(screen.queryByText("Protocolo", { selector: "span.font-medium" })).toBeNull());
  });

  it("creates a type without notes or version and closes the dialog", async () => {
    await renderPage();
    fireEvent.click(screen.getByTestId("btn-new-folio-type"));
    fireEvent.change(await screen.findByTestId("input-name-folio-type"), { target: { value: "Nuevo tipo" } });

    fireEvent.click(screen.getByTestId("btn-save-folio-type"));

    await waitFor(() => expect(apiPost).toHaveBeenCalledWith("/folio-types", { name: "Nuevo tipo", isAuxiliary: false }));
    expect(toast.success).toHaveBeenCalledWith("administracion.folios.tiposDeFolio.created");
    await waitFor(() => expect(screen.queryByTestId("input-name-folio-type")).toBeNull());
  });

  it("starts a new type empty even after editing another", async () => {
    await renderPage();
    fireEvent.click(within(typeRowOf("Auxiliar")).getByTestId("btn-edit-folio-type"));
    expect(await screen.findByTestId("input-name-folio-type")).toHaveValue("Auxiliar");
    expect(screen.getByTestId("checkbox-is-auxiliary-folio-type")).toBeChecked();
    fireEvent.click(within(screen.getByRole("dialog")).getByRole("button", { name: /common\.cancel/ }));
    await waitFor(() => expect(screen.queryByTestId("input-name-folio-type")).toBeNull());

    fireEvent.click(screen.getByTestId("btn-new-folio-type"));

    expect(await screen.findByTestId("input-name-folio-type")).toHaveValue("");
    expect(screen.getByTestId("checkbox-is-auxiliary-folio-type")).not.toBeChecked();
  });

  it("reports an edit that failed and keeps the dialog", async () => {
    apiPut.mockRejectedValue(new ApiError(500, "/folio-types/7", ""));
    await renderPage();
    fireEvent.click(within(typeRowOf("Protocolo")).getByTestId("btn-edit-folio-type"));
    await screen.findByTestId("input-name-folio-type");

    fireEvent.click(screen.getByTestId("btn-save-folio-type"));

    await waitFor(() => expect(toast.error).toHaveBeenCalledWith("administracion.folios.tiposDeFolio.errorSave"));
    expect(toast.success).not.toHaveBeenCalled();
    expect(screen.getByTestId("input-name-folio-type")).toBeInTheDocument();
  });

  it("confirms an edit with the updated message", async () => {
    await renderPage();
    fireEvent.click(within(typeRowOf("Protocolo")).getByTestId("btn-edit-folio-type"));
    await screen.findByTestId("input-name-folio-type");

    fireEvent.click(screen.getByTestId("btn-save-folio-type"));

    await waitFor(() => expect(toast.success).toHaveBeenCalledWith("administracion.folios.tiposDeFolio.updated"));
  });

  it("reports a failed delete check without opening the confirmation", async () => {
    typeInUseFails = true;
    await renderPage();

    fireEvent.click(within(typeRowOf("Protocolo")).getByTestId("btn-delete-folio-type"));

    await waitFor(() => expect(toast.warning.mock.calls.length + toast.error.mock.calls.length).toBeGreaterThan(0));
    expect(screen.queryByTestId("confirm-dialog-confirm")).toBeNull();
  });

  it("reports a type whose delete fails after the confirmation", async () => {
    apiDelete.mockRejectedValue(new ApiError(500, "/folio-types/7", ""));
    await renderPage();
    fireEvent.click(within(typeRowOf("Protocolo")).getByTestId("btn-delete-folio-type"));
    fireEvent.click(await screen.findByTestId("confirm-dialog-confirm"));

    await waitFor(() => expect(toast.warning.mock.calls.length + toast.error.mock.calls.length).toBeGreaterThan(0));
    expect(toast.success).not.toHaveBeenCalledWith("administracion.folios.tiposDeFolio.deleted");
  });

  it("confirms a deleted type", async () => {
    await renderPage();
    fireEvent.click(within(typeRowOf("Protocolo")).getByTestId("btn-delete-folio-type"));
    fireEvent.click(await screen.findByTestId("confirm-dialog-confirm"));

    await waitFor(() => expect(toast.success).toHaveBeenCalledWith("administracion.folios.tiposDeFolio.deleted"));
  });
});
