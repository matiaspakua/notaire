/**
 * Management status catalog (CU18, #1246): a status needs a name, is edited with its optimistic-lock
 * version, is deleted only when no management uses it, and is searched by name.
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
  usePathname: () => "/dashboard/administration/management-statuses",
  useSearchParams: () => new URLSearchParams(),
}));
vi.mock("next-intl", () => ({
  useTranslations: (ns: string) => (key: string) => `${ns}.${key}`,
  useLocale: () => "es",
}));

import StatusesManagementPage from "@/app/dashboard/administration/management-statuses/page";

const STATUSES = [
  { idManagementStatus: 4, name: "Iniciada", notes: "alta", version: 6 },
  { idManagementStatus: 5, name: "En registro", notes: null, version: 1 },
];

let inUse = false;
function route(path: string) {
  if (path.endsWith("/in-use")) return Promise.resolve({ inUse });
  if (path.startsWith("/management-statuses/search")) return Promise.resolve([STATUSES[1]]);
  if (path.startsWith("/management-statuses")) return Promise.resolve(STATUSES);
  return Promise.resolve([]);
}

function wrapper({ children }: { children: ReactNode }) {
  const client = new QueryClient({ defaultOptions: { queries: { retry: false } } });
  return <QueryClientProvider client={client}>{children}</QueryClientProvider>;
}

async function renderPage() {
  render(<StatusesManagementPage />, { wrapper });
  await screen.findByText("Iniciada");
}

function row(name: string) {
  return screen.getAllByRole("row").find((r) => within(r).queryByText(name))!;
}

beforeEach(() => {
  inUse = false;
  apiGet.mockReset().mockImplementation(route);
  apiPost.mockReset().mockResolvedValue({});
  apiPut.mockReset().mockResolvedValue({});
  apiDelete.mockReset().mockResolvedValue(undefined);
  Object.values(toast).forEach((fn) => fn.mockReset());
});

describe("management statuses", () => {
  it("needs a name", async () => {
    await renderPage();
    fireEvent.click(screen.getByTestId("btn-new-status"));

    fireEvent.click(within(await screen.findByRole("dialog")).getByRole("button", { name: /common\.save/ }));

    expect(toast.error).toHaveBeenCalledWith("administracion.estadosGestion.nameRequired");
    expect(apiPost).not.toHaveBeenCalled();
  });

  it("creates a status with a trimmed name", async () => {
    await renderPage();
    fireEvent.click(screen.getByTestId("btn-new-status"));

    fireEvent.change(await screen.findByTestId("input-name-status"), { target: { value: "  Observada  " } });
    fireEvent.click(within(screen.getByRole("dialog")).getByRole("button", { name: /common\.save/ }));

    await waitFor(() =>
      expect(apiPost).toHaveBeenCalledWith("/management-statuses", expect.objectContaining({ name: "Observada" })),
    );
  });

  it("edits a status and sends back its version", async () => {
    await renderPage();
    fireEvent.click(within(row("Iniciada")).getByTestId("btn-edit-status"));

    const name = await screen.findByTestId("input-name-status");
    expect(name).toHaveValue("Iniciada");
    fireEvent.change(name, { target: { value: "Iniciada B" } });
    fireEvent.click(within(screen.getByRole("dialog")).getByRole("button", { name: /common\.update/ }));

    await waitFor(() =>
      expect(apiPut).toHaveBeenCalledWith(
        "/management-statuses/4",
        expect.objectContaining({ name: "Iniciada B", version: 6 }),
      ),
    );
  });

  it("deletes a status no management uses after confirmation", async () => {
    await renderPage();
    fireEvent.click(within(row("Iniciada")).getByTestId("btn-delete-status"));

    fireEvent.click(await screen.findByTestId("confirm-dialog-confirm"));

    await waitFor(() => expect(apiDelete).toHaveBeenCalledWith("/management-statuses/4"));
  });

  it("warns instead of asking to delete a status in use", async () => {
    inUse = true;
    await renderPage();

    fireEvent.click(within(row("Iniciada")).getByTestId("btn-delete-status"));

    await waitFor(() => expect(toast.warning).toHaveBeenCalledWith("administracion.estadosGestion.inUseCannotDelete"));
    expect(screen.queryByTestId("confirm-dialog-confirm")).toBeNull();
    expect(apiDelete).not.toHaveBeenCalled();
  });

  it("searches statuses by name on the server", async () => {
    await renderPage();

    fireEvent.change(screen.getByTestId("search-statuses"), { target: { value: "regis" } });

    await waitFor(() => expect(apiGet).toHaveBeenCalledWith("/management-statuses/search?name=regis"));
    await waitFor(() => expect(screen.queryByText("Iniciada")).toBeNull());
    expect(screen.getByText("En registro")).toBeInTheDocument();
  });
});
