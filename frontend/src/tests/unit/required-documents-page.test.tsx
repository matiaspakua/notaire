/**
 * Required documents per procedure type (CU39, #1246): nothing is loaded until a procedure type is
 * chosen, its document types are listed with their expiry, and an empty template says so.
 */
import { describe, it, expect, vi, beforeEach } from "vitest";
import { fireEvent, render, screen, waitFor, within } from "@testing-library/react";
import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import type { ReactNode } from "react";

const apiGet = vi.fn();

vi.mock("@/lib/api-client", async (orig) => ({
  ...(await orig<typeof import("@/lib/api-client")>()),
  apiGet: (path: string) => apiGet(path),
}));
vi.mock("next/navigation", () => ({
  useRouter: () => ({ push: vi.fn(), replace: vi.fn() }),
  usePathname: () => "/dashboard/required-documents",
  useSearchParams: () => new URLSearchParams(),
}));
vi.mock("next-intl", () => ({
  useTranslations: (ns: string) => (key: string, values?: Record<string, string>) =>
    values ? `${ns}.${key}:${values.tramite}` : `${ns}.${key}`,
  useLocale: () => "es",
}));

import DocumentsRequiredPage from "@/app/dashboard/required-documents/page";

const PROCEDURES = [
  { idProcedureType: 3, name: "Compraventa" },
  { idProcedureType: 4, name: "Poder" },
];
const TEMPLATE_3 = [
  { fkIdProcedureType: 3, fkIdDocumentType: 1 },
  { fkIdProcedureType: 3, fkIdDocumentType: 2 },
];
const DOCUMENT_TYPES = [
  { idDocumentType: 1, name: "DNI", expires: false, deliveredBy: "Comprador" },
  { idDocumentType: 2, name: "Certificado", expires: true, dueDays: 30 },
];

function wrapper({ children }: { children: ReactNode }) {
  const client = new QueryClient({ defaultOptions: { queries: { retry: false } } });
  return <QueryClientProvider client={client}>{children}</QueryClientProvider>;
}

async function choose(name: string) {
  fireEvent.click(screen.getByTestId("select-procedure"));
  fireEvent.click(await screen.findByRole("option", { name }));
}

beforeEach(() => {
  Element.prototype.hasPointerCapture = vi.fn(() => false);
  Element.prototype.setPointerCapture = vi.fn();
  Element.prototype.releasePointerCapture = vi.fn();
  Element.prototype.scrollIntoView = vi.fn();
  apiGet.mockReset().mockImplementation((path: string) => {
    if (path === "/procedure-types") return Promise.resolve(PROCEDURES);
    if (path === "/document-types") return Promise.resolve(DOCUMENT_TYPES);
    if (path === "/procedure-templates/procedure-types/3") return Promise.resolve(TEMPLATE_3);
    return Promise.resolve([]);
  });
});

describe("required documents", () => {
  it("loads no template and offers no print before a procedure type is chosen", async () => {
    render(<DocumentsRequiredPage />, { wrapper });

    await waitFor(() => expect(apiGet).toHaveBeenCalledWith("/procedure-types"));
    expect(screen.queryByTestId("btn-print")).toBeNull();
    expect(apiGet).not.toHaveBeenCalledWith(expect.stringContaining("/procedure-templates"));
  });

  it("lists the document types of the chosen procedure with their expiry and offers to print", async () => {
    render(<DocumentsRequiredPage />, { wrapper });

    await choose("Compraventa");

    const dni = (await screen.findByText("DNI")).closest("tr")!;
    expect(within(dni).getByText("Comprador")).toBeInTheDocument();
    expect(within(dni).getByText("common.no")).toBeInTheDocument();
    const certificate = screen.getByText("Certificado").closest("tr")!;
    expect(within(certificate).getByText("common.yes")).toBeInTheDocument();
    expect(within(certificate).getByText("30")).toBeInTheDocument();
    expect(screen.getByTestId("btn-print")).toBeInTheDocument();
  });

  it("says so when the chosen procedure has no required documents", async () => {
    render(<DocumentsRequiredPage />, { wrapper });

    await choose("Poder");

    expect(await screen.findByTestId("empty-state")).toBeInTheDocument();
    expect(screen.queryByTestId("btn-print")).toBeNull();
  });

  it("prints the page when asked", async () => {
    const print = vi.spyOn(window, "print").mockImplementation(() => undefined);
    render(<DocumentsRequiredPage />, { wrapper });
    await choose("Compraventa");

    fireEvent.click(await screen.findByTestId("btn-print"));

    expect(print).toHaveBeenCalledTimes(1);
  });
});
