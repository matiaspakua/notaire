/**
 * Procedure type catalog (CU37, #1246): a procedure type needs a name, is deleted only when no
 * procedure uses it, and is searched by name; its workflow is assigned through its own endpoint.
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
  usePathname: () => "/dashboard/administration/procedures",
  useSearchParams: () => new URLSearchParams(),
}));
vi.mock("next-intl", () => ({
  useTranslations: (ns: string) => (key: string) => `${ns}.${key}`,
  useLocale: () => "es",
}));

import ProceduresPage from "@/app/dashboard/administration/procedures/page";

const PROCEDURES = [
  { idProcedureType: 3, name: "Compraventa", notes: "n", workflowDefinitionId: null },
  { idProcedureType: 4, name: "Poder", notes: null, workflowDefinitionId: null },
];

let inUse = false;
function route(path: string) {
  if (path.endsWith("/in-use")) return Promise.resolve({ inUse });
  if (path.startsWith("/procedure-types/search")) return Promise.resolve([PROCEDURES[1]]);
  if (path === "/procedure-types") return Promise.resolve(PROCEDURES);
  return Promise.resolve([]);
}

function wrapper({ children }: { children: ReactNode }) {
  const client = new QueryClient({ defaultOptions: { queries: { retry: false } } });
  return <QueryClientProvider client={client}>{children}</QueryClientProvider>;
}

async function renderPage() {
  render(<ProceduresPage />, { wrapper });
  await screen.findByText("Compraventa");
}

function row(name: string) {
  return screen.getAllByRole("row").find((r) => within(r).queryByText(name))!;
}

beforeEach(() => {
  inUse = false;
  apiGet.mockReset().mockImplementation(route);
  apiPost.mockReset().mockResolvedValue({ idProcedureType: 9 });
  apiPut.mockReset().mockResolvedValue({});
  apiDelete.mockReset().mockResolvedValue(undefined);
  Object.values(toast).forEach((fn) => fn.mockReset());
});

describe("procedure types", () => {
  it("needs a name", async () => {
    await renderPage();
    fireEvent.click(screen.getByTestId("btn-new-procedure-type"));

    fireEvent.click(within(await screen.findByRole("dialog")).getByRole("button", { name: /common\.save/ }));

    expect(toast.error).toHaveBeenCalledWith("administracion.tramites.nameRequired");
    expect(apiPost).not.toHaveBeenCalled();
  });

  it("creates a procedure type", async () => {
    await renderPage();
    fireEvent.click(screen.getByTestId("btn-new-procedure-type"));

    fireEvent.change(await screen.findByTestId("input-name-procedure"), { target: { value: "Donación" } });
    fireEvent.click(within(screen.getByRole("dialog")).getByRole("button", { name: /common\.save/ }));

    await waitFor(() =>
      expect(apiPost).toHaveBeenCalledWith("/procedure-types", expect.objectContaining({ name: "Donación" })),
    );
    expect(toast.success).toHaveBeenCalledWith("administracion.tramites.created");
  });

  it("edits a procedure type", async () => {
    await renderPage();
    fireEvent.click(within(row("Compraventa")).getByTestId("btn-edit-procedure"));

    const name = await screen.findByTestId("input-name-procedure");
    expect(name).toHaveValue("Compraventa");
    fireEvent.change(name, { target: { value: "Compraventa B" } });
    fireEvent.click(within(screen.getByRole("dialog")).getByRole("button", { name: /common\.update/ }));

    await waitFor(() =>
      expect(apiPut).toHaveBeenCalledWith("/procedure-types/3", expect.objectContaining({ name: "Compraventa B" })),
    );
    expect(apiPut).not.toHaveBeenCalledWith("/procedure-types/3/workflow", expect.anything());
  });

  it("deletes an unused procedure type after confirmation", async () => {
    await renderPage();
    fireEvent.click(within(row("Compraventa")).getByTestId("btn-delete-procedure"));

    fireEvent.click(await screen.findByTestId("confirm-dialog-confirm"));

    await waitFor(() => expect(apiDelete).toHaveBeenCalledWith("/procedure-types/3"));
  });

  it("warns instead of asking to delete a procedure type in use", async () => {
    inUse = true;
    await renderPage();

    fireEvent.click(within(row("Compraventa")).getByTestId("btn-delete-procedure"));

    await waitFor(() => expect(toast.warning).toHaveBeenCalledWith("administracion.tramites.inUseCannotDelete"));
    expect(apiDelete).not.toHaveBeenCalled();
  });

  it("searches procedure types by name on the server", async () => {
    await renderPage();

    fireEvent.change(screen.getByTestId("input-search-procedure"), { target: { value: "pod" } });

    await waitFor(() => expect(apiGet).toHaveBeenCalledWith("/procedure-types/search?name=pod"));
    await waitFor(() => expect(screen.queryByText("Compraventa")).toBeNull());
  });
});
