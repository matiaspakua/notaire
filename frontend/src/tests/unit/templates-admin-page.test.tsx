/**
 * Budget template administration (CU38, #1246): a template is keyed by procedure type and concept,
 * is edited and deleted through that composite key, and needs both before it is created.
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
  usePathname: () => "/dashboard/administration/templates",
  useSearchParams: () => new URLSearchParams(),
}));
vi.mock("next-intl", () => ({
  useTranslations: (ns: string) => (key: string) => `${ns}.${key}`,
  useLocale: () => "es",
}));

import TemplatesPage from "@/app/dashboard/administration/templates/page";

const TEMPLATES = [
  {
    budgetTemplatePK: { fkIdProcedureType: 3, fkIdConcept: 9 },
    procedureType: { idProcedureType: 3, name: "Compraventa" },
    concept: { idConcept: 9, name: "Sellado" },
    notes: "nota",
  },
];

function route(path: string) {
  if (path.startsWith("/budget-templates")) return Promise.resolve(TEMPLATES);
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

beforeEach(() => {
  apiGet.mockReset().mockImplementation(route);
  apiPost.mockReset().mockResolvedValue({});
  apiPut.mockReset().mockResolvedValue({});
  apiDelete.mockReset().mockResolvedValue(undefined);
  Object.values(toast).forEach((fn) => fn.mockReset());
});

describe("budget templates", () => {
  it("lists a template with its procedure type, concept and notes", async () => {
    await renderPage();

    const row = screen.getAllByRole("row").find((r) => within(r).queryByText("Sellado"))!;
    expect(within(row).getByText("Compraventa")).toBeInTheDocument();
    expect(within(row).getByText("nota")).toBeInTheDocument();
  });

  it("refuses to create a template without a procedure type and a concept", async () => {
    await renderPage();
    fireEvent.click(screen.getByTestId("btn-new-template"));

    const dialog = await screen.findByRole("dialog");
    fireEvent.click(within(dialog).getByRole("button", { name: /common\.save/ }));

    expect(toast.error).toHaveBeenCalledWith("administracion.plantillas.selectionRequired");
    expect(apiPost).not.toHaveBeenCalled();
  });

  it("edits through the composite key and keeps the key fields locked", async () => {
    await renderPage();
    fireEvent.click(screen.getByTestId("btn-edit-template"));

    const notes = await screen.findByTestId("input-notes-template");
    expect(notes).toHaveValue("nota");
    expect(screen.getByTestId("select-procedure-type")).toBeDisabled();
    expect(screen.getByTestId("select-concept")).toBeDisabled();
    fireEvent.change(notes, { target: { value: "otra" } });
    fireEvent.click(within(await screen.findByRole("dialog")).getByRole("button", { name: /common\.update/ }));

    await waitFor(() =>
      expect(apiPut).toHaveBeenCalledWith("/budget-templates/procedure-types/3/concept/9", {
        budgetTemplatePK: { fkIdProcedureType: 3, fkIdConcept: 9 },
        procedureType: { idProcedureType: 3 },
        concept: { idConcept: 9 },
        notes: "otra",
      }),
    );
    expect(toast.success).toHaveBeenCalled();
  });

  it("deletes through the composite key after confirmation", async () => {
    await renderPage();
    fireEvent.click(screen.getByTestId("btn-delete-template"));

    fireEvent.click(await screen.findByTestId("confirm-dialog-confirm"));

    await waitFor(() => expect(apiDelete).toHaveBeenCalledWith("/budget-templates/procedure-types/3/concept/9"));
  });

  it("shows the delete failure instead of closing silently", async () => {
    apiDelete.mockRejectedValue(new Error("boom"));
    await renderPage();
    fireEvent.click(screen.getByTestId("btn-delete-template"));

    fireEvent.click(await screen.findByTestId("confirm-dialog-confirm"));

    await waitFor(() => expect(toast.error).toHaveBeenCalled());
  });
});

describe("document cost templates", () => {
  it("offers a new cost only once a procedure type is chosen, and loads none before", async () => {
    await renderPage();

    expect(screen.queryByTestId("btn-new-cost-document")).toBeNull();
    expect(apiGet).not.toHaveBeenCalledWith(expect.stringContaining("/document-cost-templates"));
  });
});
