/**
 * Document re-entry page (CU43, #1246): a management opens its procedures and the documents each one
 * requires, a document is re-entered for its procedure (success and failure reported), expiry and missing
 * values render as dashes, an empty detail says so and a page past the end moves to the last page.
 */
import { describe, it, expect, vi, beforeEach } from "vitest";
import { fireEvent, render, screen, waitFor, within } from "@testing-library/react";
import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import type { ReactNode } from "react";

const apiGet = vi.fn();
const apiGetPage = vi.fn();
const apiPost = vi.fn();
const toast = vi.hoisted(() => ({ success: vi.fn(), error: vi.fn(), warning: vi.fn(), info: vi.fn() }));
const replace = vi.hoisted(() => vi.fn());
let searchParams = new URLSearchParams();

vi.mock("@/lib/api-client", async (orig) => ({
  ...(await orig<typeof import("@/lib/api-client")>()),
  apiGet: (path: string) => apiGet(path),
  apiGetPage: (path: string, request: unknown) => apiGetPage(path, request),
  apiPost: (path: string, body: unknown) => apiPost(path, body),
}));
vi.mock("sonner", () => ({ toast }));
vi.mock("next/navigation", () => ({
  useRouter: () => ({ push: vi.fn(), replace }),
  usePathname: () => "/dashboard/document-reentry",
  useSearchParams: () => searchParams,
}));
vi.mock("next-intl", () => ({
  useTranslations: (ns: string) => (key: string) => `${ns}.${key}`,
  useLocale: () => "es",
}));

import { ApiError } from "@/lib/api-client";
import DocumentReentryPage from "@/app/dashboard/document-reentry/page";

const MANAGEMENTS = [
  { idManagement: 1, number: 501, heading: "Compraventa", currentStatus: "Iniciada" },
  { idManagement: 2, number: 502 },
];
const page = (totalPages = 1) => ({ content: MANAGEMENTS, number: 0, size: 20, totalElements: 2, totalPages });
const DETAIL = {
  idManagement: 1,
  procedures: [
    {
      idProcedure: 10,
      typeProcedureName: "Inscripción",
      requiredDocuments: [
        { idDocumentType: 100, name: "DNI", expires: true, dueDays: 30, deliveredBy: "Cliente" },
        { idDocumentType: 101, expires: true },
        { idDocumentType: 102, name: "Poder", expires: false, dueDays: 9 },
      ],
    },
    { idProcedure: 11, requiredDocuments: [] },
  ],
};

function wrapper({ children }: { children: ReactNode }) {
  const client = new QueryClient({ defaultOptions: { queries: { retry: false } } });
  return <QueryClientProvider client={client}>{children}</QueryClientProvider>;
}

async function openDetail() {
  render(<DocumentReentryPage />, { wrapper });
  fireEvent.click(await screen.findByTestId("btn-view-procedures-1"));
  await screen.findByTestId("procedure-10");
}

beforeEach(() => {
  searchParams = new URLSearchParams();
  replace.mockReset();
  apiGet.mockReset().mockResolvedValue(DETAIL);
  apiGetPage.mockReset().mockResolvedValue(page());
  apiPost.mockReset().mockResolvedValue({ idSubmittedDocument: 1, reentered: true });
  Object.values(toast).forEach((fn) => fn.mockReset());
});

describe("management list", () => {
  it("shows a dash for a management without heading or status", async () => {
    render(<DocumentReentryPage />, { wrapper });

    const row = (await screen.findByText("502")).closest("tr")!;
    expect(within(row).getAllByText("—")).toHaveLength(2);
    expect(screen.getByText("Compraventa")).toBeInTheDocument();
  });

  it("asks the server for the page in the address and falls back to the first for an invalid size", async () => {
    searchParams = new URLSearchParams("page=0&size=7");
    render(<DocumentReentryPage />, { wrapper });
    await screen.findByText("501");

    expect(apiGetPage).toHaveBeenCalledWith("/managements", expect.objectContaining({ page: 0, size: 20 }));
  });

  it("moves a page past the end to the last page", async () => {
    searchParams = new URLSearchParams("page=5");
    apiGetPage.mockResolvedValue(page(2));
    render(<DocumentReentryPage />, { wrapper });

    await waitFor(() => expect(replace).toHaveBeenCalledWith("/dashboard/document-reentry?page=1", { scroll: false }));
  });

  it("keeps a page inside the range", async () => {
    searchParams = new URLSearchParams("page=1");
    apiGetPage.mockResolvedValue(page(2));
    render(<DocumentReentryPage />, { wrapper });
    await screen.findByText("501");

    expect(replace).not.toHaveBeenCalled();
  });

  it("shows the empty message when there are no managements", async () => {
    apiGetPage.mockResolvedValue({ content: [], number: 0, size: 20, totalElements: 0, totalPages: 0 });
    render(<DocumentReentryPage />, { wrapper });

    expect(await screen.findByText("reingresoDocumentacion.noData")).toBeInTheDocument();
  });
});

describe("procedures dialog", () => {
  it("loads the procedures of the chosen management", async () => {
    await openDetail();

    expect(apiGet).toHaveBeenCalledWith("/managements/1/document-reentry");
    expect(screen.getByTestId("procedure-10")).toHaveTextContent("Inscripción");
    expect(screen.getByTestId("procedure-11")).toHaveTextContent("—");
  });

  it("says a procedure needs no documents", async () => {
    await openDetail();

    expect(screen.getByTestId("empty-documents-11")).toHaveTextContent("reingresoDocumentacion.noDocumentosNecesarios");
    expect(screen.queryByTestId("empty-documents-10")).toBeNull();
  });

  it("shows the due days only for a document that expires and dashes for missing values", async () => {
    await openDetail();

    const dni = within(screen.getByTestId("procedure-10")).getByText("DNI").closest("tr")!;
    expect(within(dni).getByText("30")).toBeInTheDocument();
    expect(within(dni).getByText("Cliente")).toBeInTheDocument();
    const unnamed = within(screen.getByTestId("procedure-10")).getByTestId("btn-reenter-10-101").closest("tr")!;
    expect(within(unnamed).getAllByText("—")).toHaveLength(3);
    const noExpiry = within(screen.getByTestId("procedure-10")).getByText("Poder").closest("tr")!;
    expect(within(noExpiry).queryByText("9")).toBeNull();
  });

  it("says a management has no procedures", async () => {
    apiGet.mockResolvedValue({ idManagement: 1, procedures: [] });
    render(<DocumentReentryPage />, { wrapper });
    fireEvent.click(await screen.findByTestId("btn-view-procedures-1"));

    expect(await screen.findByTestId("empty-procedures")).toHaveTextContent("reingresoDocumentacion.noTramites");
  });

  it("shows the loading text while the detail loads", async () => {
    apiGet.mockReturnValue(new Promise(() => undefined));
    render(<DocumentReentryPage />, { wrapper });
    fireEvent.click(await screen.findByTestId("btn-view-procedures-1"));

    expect(await screen.findByText("common.loading")).toBeInTheDocument();
    expect(screen.queryByTestId("empty-procedures")).toBeNull();
  });

  it("closes the dialog with Escape", async () => {
    await openDetail();

    fireEvent.keyDown(screen.getByRole("dialog"), { key: "Escape" });

    await waitFor(() => expect(screen.queryByTestId("procedure-10")).toBeNull());
  });
});

describe("re-entering a document", () => {
  it("re-enters the document for its procedure", async () => {
    await openDetail();

    fireEvent.click(screen.getByTestId("btn-reenter-10-100"));

    await waitFor(() =>
      expect(apiPost).toHaveBeenCalledWith("/managements/1/document-reentry", { idProcedure: 10, idDocumentType: 100 }),
    );
    expect(toast.success).toHaveBeenCalledWith("reingresoDocumentacion.documentoReingresado");
  });

  it("reports the server's reason when the re-entry fails", async () => {
    apiPost.mockRejectedValue(new ApiError(409, "/managements/1/document-reentry", '{"message":"Ya vigente"}'));
    await openDetail();

    fireEvent.click(screen.getByTestId("btn-reenter-10-100"));

    await waitFor(() => expect(toast.error).toHaveBeenCalled());
    expect(toast.error).not.toHaveBeenCalledWith("reingresoDocumentacion.errorReingreso");
    expect(toast.success).not.toHaveBeenCalled();
  });

  it("falls back to the generic message when the server gives no reason", async () => {
    apiPost.mockRejectedValue(new ApiError(500, "/managements/1/document-reentry", ""));
    await openDetail();

    fireEvent.click(screen.getByTestId("btn-reenter-10-100"));

    await waitFor(() => expect(toast.error).toHaveBeenCalledWith("reingresoDocumentacion.errorReingreso"));
  });

  it("disables the re-entry buttons while one is in flight", async () => {
    apiPost.mockReturnValue(new Promise(() => undefined));
    await openDetail();

    fireEvent.click(screen.getByTestId("btn-reenter-10-100"));

    await waitFor(() => expect(screen.getByTestId("btn-reenter-10-102")).toBeDisabled());
  });
});
