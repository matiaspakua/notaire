/**
 * Budget concept catalog (CU36, #1246): a concept needs a name, the server's conflict message is
 * shown as is, and a concept in use is not offered for deletion.
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
  usePathname: () => "/dashboard/administration/concepts",
  useSearchParams: () => new URLSearchParams(),
}));
vi.mock("next-intl", () => ({
  useTranslations: (ns: string) => (key: string) => `${ns}.${key}`,
  useLocale: () => "es",
}));

import { ApiError } from "@/lib/api-client";
import ConceptsPage from "@/app/dashboard/administration/concepts/page";

const CONCEPTS = [
  { idConcept: 9, name: "Sellado", value: 1500 },
  { idConcept: 10, name: "Honorarios", value: 0 },
];

let inUse = false;
function route(path: string) {
  if (path.endsWith("/in-use")) return Promise.resolve({ inUse });
  if (path.startsWith("/concepts/search")) return Promise.resolve([CONCEPTS[1]]);
  if (path.startsWith("/concepts")) return Promise.resolve(CONCEPTS);
  return Promise.resolve([]);
}

function wrapper({ children }: { children: ReactNode }) {
  const client = new QueryClient({ defaultOptions: { queries: { retry: false } } });
  return <QueryClientProvider client={client}>{children}</QueryClientProvider>;
}

async function renderPage() {
  render(<ConceptsPage />, { wrapper });
  await screen.findByText("Sellado");
}

function row(name: string) {
  return screen.getAllByRole("row").find((r) => within(r).queryByText(name))!;
}

function newConceptButton() {
  return screen.getByRole("button", { name: /newConcepto/ });
}

function saveButton() {
  return within(screen.getByRole("dialog")).getByRole("button", { name: /common\.(save|update)/ });
}

beforeEach(() => {
  inUse = false;
  apiGet.mockReset().mockImplementation(route);
  apiPost.mockReset().mockResolvedValue({});
  apiPut.mockReset().mockResolvedValue({});
  apiDelete.mockReset().mockResolvedValue(undefined);
  Object.values(toast).forEach((fn) => fn.mockReset());
});

describe("concepts", () => {
  it("needs a name", async () => {
    await renderPage();
    fireEvent.click(newConceptButton());
    await screen.findByRole("dialog");

    fireEvent.click(saveButton());

    expect(await screen.findByText("common.fieldRequired")).toBeTruthy();
    expect(apiPost).not.toHaveBeenCalled();
  });

  it("creates a concept", async () => {
    await renderPage();
    fireEvent.click(newConceptButton());

    fireEvent.change(await screen.findByTestId("input-name-concept"), { target: { value: "Timbrado" } });
    fireEvent.click(saveButton());

    await waitFor(() => expect(apiPost).toHaveBeenCalledWith("/concepts", expect.objectContaining({ name: "Timbrado" })));
    expect(toast.success).toHaveBeenCalledWith("administracion.conceptos.created");
  });

  it("shows the server's conflict message when the name already exists", async () => {
    apiPost.mockRejectedValue(new ApiError(409, "/concepts", '{"message":"El concepto ya existe"}'));
    await renderPage();
    fireEvent.click(newConceptButton());
    fireEvent.change(await screen.findByTestId("input-name-concept"), { target: { value: "Sellado" } });

    fireEvent.click(saveButton());

    await waitFor(() => expect(toast.error).toHaveBeenCalledWith("El concepto ya existe"));
  });

  it("falls back to a generic message when the failure has no server text", async () => {
    apiPost.mockRejectedValue(new Error("boom"));
    await renderPage();
    fireEvent.click(newConceptButton());
    fireEvent.change(await screen.findByTestId("input-name-concept"), { target: { value: "Otro" } });

    fireEvent.click(saveButton());

    await waitFor(() => expect(toast.error).toHaveBeenCalledWith("administracion.conceptos.errorSave"));
  });

  it("deletes an unused concept after confirmation", async () => {
    await renderPage();
    fireEvent.click(within(row("Sellado")).getByTestId("btn-delete-concept"));

    fireEvent.click(await screen.findByTestId("confirm-dialog-confirm"));

    await waitFor(() => expect(apiDelete).toHaveBeenCalledWith("/concepts/9"));
  });

  it("warns instead of asking to delete a concept in use", async () => {
    inUse = true;
    await renderPage();

    fireEvent.click(within(row("Sellado")).getByTestId("btn-delete-concept"));

    await waitFor(() => expect(toast.warning).toHaveBeenCalledWith("administracion.conceptos.inUseCannotDelete"));
    expect(apiDelete).not.toHaveBeenCalled();
  });

  it("searches concepts by name on the server", async () => {
    await renderPage();

    fireEvent.change(screen.getByTestId("input-search-concept"), { target: { value: "hon" } });

    await waitFor(() => expect(apiGet).toHaveBeenCalledWith("/concepts/search?name=hon"));
    await waitFor(() => expect(screen.queryByText("Sellado")).toBeNull());
  });
});
