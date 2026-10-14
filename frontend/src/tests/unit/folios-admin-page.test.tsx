/**
 * Folio administration (CU87, #1246): folio types are created, edited with their optimistic-lock
 * version and deleted unless a folio uses them; a folio in use cannot be edited or deleted, and a
 * folio needs a number, a type and a notary before it is sent.
 */
import { describe, it, expect, vi, beforeEach } from "vitest";
import { fireEvent, render, screen, waitFor, within } from "@testing-library/react";
import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import type { ReactNode } from "react";

const apiGet = vi.fn();
const apiPost = vi.fn();
const apiPut = vi.fn();
const apiDelete = vi.fn();
const toast = vi.hoisted(() => ({ success: vi.fn(), error: vi.fn(), warning: vi.fn() }));

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

import FoliosAdminPage from "@/app/dashboard/administration/folios/page";

const FOLIOS = [
  { idFolio: 1, number: 1001, year: 2026, status: "Nuevo", fkIdFolioType: { idFolioType: 7, name: "Protocolo" } },
  { idFolio: 2, number: 1002, year: 2026, status: "Utilizado", fkIdFolioType: { idFolioType: 7, name: "Protocolo" } },
];
const FOLIO_TYPES = [{ idFolioType: 7, name: "Protocolo", isAuxiliary: false, notes: "n", enabled: true, version: 3 }];

let inUse = false;
function route(path: string) {
  if (path.endsWith("/in-use")) return Promise.resolve({ inUse });
  if (path.startsWith("/folios")) return Promise.resolve(FOLIOS);
  if (path.startsWith("/folio-types")) return Promise.resolve(FOLIO_TYPES);
  return Promise.resolve([]);
}

function wrapper({ children }: { children: ReactNode }) {
  const client = new QueryClient({ defaultOptions: { queries: { retry: false } } });
  return <QueryClientProvider client={client}>{children}</QueryClientProvider>;
}

async function renderPage() {
  render(<FoliosAdminPage />, { wrapper });
  await screen.findByText("1001");
  await screen.findByText("Protocolo", { selector: "span" });
}

beforeEach(() => {
  inUse = false;
  apiGet.mockReset().mockImplementation(route);
  apiPost.mockReset().mockResolvedValue({});
  apiPut.mockReset().mockResolvedValue({});
  apiDelete.mockReset().mockResolvedValue(undefined);
  Object.values(toast).forEach((fn) => fn.mockReset());
});

describe("folio types", () => {
  it("creates a type with its auxiliary flag", async () => {
    await renderPage();
    fireEvent.click(screen.getByTestId("btn-new-folio-type"));

    fireEvent.change(await screen.findByTestId("input-name-folio-type"), { target: { value: "Auxiliar" } });
    fireEvent.click(screen.getByTestId("checkbox-is-auxiliary-folio-type"));
    fireEvent.click(screen.getByTestId("btn-save-folio-type"));

    await waitFor(() => expect(apiPost).toHaveBeenCalledWith("/folio-types", { name: "Auxiliar", isAuxiliary: true }));
    expect(toast.success).toHaveBeenCalled();
  });

  it("refuses an empty name without calling the API", async () => {
    await renderPage();
    fireEvent.click(screen.getByTestId("btn-new-folio-type"));

    fireEvent.click(await screen.findByTestId("btn-save-folio-type"));

    expect(toast.error).toHaveBeenCalledWith("administracion.folios.tiposDeFolio.nameRequired");
    expect(apiPost).not.toHaveBeenCalled();
  });

  it("edits a type and sends back the loaded version", async () => {
    await renderPage();
    fireEvent.click(screen.getByTestId("btn-edit-folio-type"));

    const name = await screen.findByTestId("input-name-folio-type");
    expect(name).toHaveValue("Protocolo");
    fireEvent.change(name, { target: { value: "Protocolo B" } });
    fireEvent.click(screen.getByTestId("btn-save-folio-type"));

    await waitFor(() =>
      expect(apiPut).toHaveBeenCalledWith(
        "/folio-types/7",
        expect.objectContaining({ name: "Protocolo B", version: 3, notes: "n", enabled: true }),
      ),
    );
  });

  it("deletes an unused type after confirmation", async () => {
    await renderPage();
    fireEvent.click(screen.getByTestId("btn-delete-folio-type"));

    fireEvent.click(await screen.findByTestId("confirm-dialog-confirm"));

    await waitFor(() => expect(apiDelete).toHaveBeenCalledWith("/folio-types/7"));
  });

  it("warns instead of asking to delete a type a folio uses", async () => {
    inUse = true;
    await renderPage();

    fireEvent.click(screen.getByTestId("btn-delete-folio-type"));

    await waitFor(() => expect(toast.warning).toHaveBeenCalled());
    expect(screen.queryByTestId("confirm-dialog-confirm")).toBeNull();
    expect(apiDelete).not.toHaveBeenCalled();
  });
});

describe("folios", () => {
  it("blocks editing and deleting a folio that is in use", async () => {
    await renderPage();

    const rows = screen.getAllByRole("row");
    const used = rows.find((row) => within(row).queryByText("1002"))!;
    expect(within(used).getByTestId("btn-edit-folio")).toBeDisabled();
    expect(within(used).getByTestId("btn-delete-folio")).toBeDisabled();
    const fresh = rows.find((row) => within(row).queryByText("1001"))!;
    expect(within(fresh).getByTestId("btn-edit-folio")).toBeEnabled();
  });

  it("deletes a new folio after confirmation", async () => {
    await renderPage();
    const fresh = screen.getAllByRole("row").find((row) => within(row).queryByText("1001"))!;

    fireEvent.click(within(fresh).getByTestId("btn-delete-folio"));
    fireEvent.click(await screen.findByTestId("confirm-dialog-confirm"));

    await waitFor(() => expect(apiDelete).toHaveBeenCalledWith("/folios/1"));
    expect(toast.success).toHaveBeenCalled();
  });

  it("asks for the number, then the type, before creating a folio", async () => {
    await renderPage();
    fireEvent.click(screen.getByTestId("btn-new-folio"));
    const dialog = await screen.findByRole("dialog");
    const save = within(dialog).getByRole("button", { name: /common\.save/ });

    fireEvent.click(save);
    expect(toast.error).toHaveBeenLastCalledWith("administracion.folios.numberRequired");

    fireEvent.change(screen.getByTestId("input-folio-number"), { target: { value: "2001" } });
    fireEvent.click(save);
    expect(toast.error).toHaveBeenLastCalledWith(expect.stringContaining("administracion.folios.fields.tipo"));
    expect(apiPost).not.toHaveBeenCalled();
  });

  it("opens the edit dialog of a new folio with its number", async () => {
    await renderPage();
    const fresh = screen.getAllByRole("row").find((row) => within(row).queryByText("1001"))!;

    fireEvent.click(within(fresh).getByTestId("btn-edit-folio"));

    expect(await screen.findByTestId("input-folio-number")).toHaveValue(1001);
    expect(screen.getByText("administracion.folios.editFolio")).toBeInTheDocument();
  });
});
