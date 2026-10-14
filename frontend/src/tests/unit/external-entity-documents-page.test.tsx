/**
 * Documents held by external entities (CU44, #1246): a management's documents open from its row, a
 * document's movement is edited from its saved values and sent to that document's endpoint.
 */
import { describe, it, expect, vi, beforeEach } from "vitest";
import { fireEvent, render, screen, waitFor } from "@testing-library/react";
import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import type { ReactNode } from "react";

const apiGet = vi.fn();
const apiGetPage = vi.fn();
const apiPut = vi.fn();
const toast = vi.hoisted(() => ({ success: vi.fn(), error: vi.fn(), warning: vi.fn() }));

vi.mock("@/lib/api-client", async (orig) => ({
  ...(await orig<typeof import("@/lib/api-client")>()),
  apiGet: (path: string) => apiGet(path),
  apiGetPage: (path: string, request: unknown) => apiGetPage(path, request),
  apiPut: (path: string, body: unknown) => apiPut(path, body),
}));
vi.mock("sonner", () => ({ toast }));
vi.mock("next/navigation", () => ({
  useRouter: () => ({ push: vi.fn(), replace: vi.fn() }),
  usePathname: () => "/dashboard/external-entity-documents",
  useSearchParams: () => new URLSearchParams(),
}));
vi.mock("next-intl", () => ({
  useTranslations: (ns: string) => (key: string) => `${ns}.${key}`,
  useLocale: () => "es",
}));

import { ApiError } from "@/lib/api-client";
import ExternalEntityDocumentsPage from "@/app/dashboard/external-entity-documents/page";

const MANAGEMENTS = [{ idManagement: 7, number: 5007, heading: "Compraventa", currentStatus: "Iniciada" }];
const DETAIL = {
  number: 5007,
  notary: "Pérez",
  heading: "Compraventa",
  cadastralDesignation: "12-34",
  documents: [
    {
      idSubmittedDocument: 21,
      name: "Plano",
      delivered: false,
      prepared: true,
      cardNumber: 88,
      dateEntry: "2026-02-01",
      notes: "ok",
    },
    { idSubmittedDocument: 22, name: "Escritura", delivered: true, flagged: true },
  ],
};

function wrapper({ children }: { children: ReactNode }) {
  const client = new QueryClient({ defaultOptions: { queries: { retry: false } } });
  return <QueryClientProvider client={client}>{children}</QueryClientProvider>;
}

async function openDocuments() {
  render(<ExternalEntityDocumentsPage />, { wrapper });
  fireEvent.click(await screen.findByTestId("btn-view-documents-7"));
  await screen.findByText("Plano");
}

beforeEach(() => {
  apiGet.mockReset().mockImplementation((path: string) =>
    path === "/managements/7/external-entity-documents" ? Promise.resolve(DETAIL) : Promise.resolve([]),
  );
  apiGetPage.mockReset().mockResolvedValue({ content: MANAGEMENTS, number: 0, size: 20, totalElements: 1, totalPages: 1 });
  apiPut.mockReset().mockResolvedValue({});
  Object.values(toast).forEach((fn) => fn.mockReset());
});

describe("external entity documents", () => {
  it("lists the managements and loads nothing until one is opened", async () => {
    render(<ExternalEntityDocumentsPage />, { wrapper });

    expect(await screen.findByText("5007")).toBeInTheDocument();
    expect(apiGet).not.toHaveBeenCalledWith(expect.stringContaining("/external-entity-documents"));
  });

  it("shows the management summary and its documents", async () => {
    await openDocuments();

    expect(screen.getByText("Pérez")).toBeInTheDocument();
    expect(screen.getByText("12-34")).toBeInTheDocument();
    expect(screen.getByText("common.no")).toBeInTheDocument();
    expect(screen.getByText("common.yes")).toBeInTheDocument();
  });

  it("opens a document's movement with its saved values", async () => {
    await openDocuments();

    fireEvent.click(screen.getByTestId("btn-edit-document-21"));

    expect(await screen.findByTestId("input-card-number")).toHaveValue(88);
    expect(screen.getByTestId("input-entry-date")).toHaveValue("2026-02-01");
    expect(screen.getByTestId("input-notes")).toHaveValue("ok");
    expect(screen.getByTestId("checkbox-prepared")).toBeChecked();
    expect(screen.getByTestId("checkbox-delivered")).not.toBeChecked();
  });

  it("opens a delivered, flagged document with both boxes ticked", async () => {
    await openDocuments();

    fireEvent.click(screen.getByTestId("btn-edit-document-22"));

    expect(await screen.findByTestId("checkbox-delivered")).toBeChecked();
    expect(screen.getByTestId("checkbox-flagged")).toBeChecked();
    expect(screen.getByTestId("checkbox-prepared")).not.toBeChecked();
  });

  it("sends the edited movement to that document's endpoint", async () => {
    await openDocuments();
    fireEvent.click(screen.getByTestId("btn-edit-document-21"));
    fireEvent.change(await screen.findByTestId("input-amount-to-pay"), { target: { value: "1200" } });
    fireEvent.click(screen.getByTestId("checkbox-delivered"));

    fireEvent.click(screen.getByTestId("btn-save-movement"));

    await waitFor(() =>
      expect(apiPut).toHaveBeenCalledWith(
        "/managements/7/external-entity-documents/21",
        expect.objectContaining({ cardNumber: 88, amountToPay: 1200, delivered: true, prepared: true }),
      ),
    );
    expect(toast.success).toHaveBeenCalledWith("documentosEntidadesExternas.movimientoRegistrado");
  });

  it("shows the server's reason when the movement is rejected and keeps the dialog", async () => {
    apiPut.mockRejectedValue(new ApiError(409, "/managements/7/external-entity-documents/21", '{"message":"Ya entregado"}'));
    await openDocuments();
    fireEvent.click(screen.getByTestId("btn-edit-document-21"));

    fireEvent.click(await screen.findByTestId("btn-save-movement"));

    await waitFor(() => expect(toast.error).toHaveBeenCalledWith("Ya entregado"));
    expect(screen.getByTestId("dialog-movement")).toBeInTheDocument();
  });
});
