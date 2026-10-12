/**
 * #1554 / CU79: "Todas las plantillas" overview of every required-document template from
 * GET /procedure-templates, filterable client-side, each row linking to that procedure's checklist editor.
 */
import { describe, it, expect, vi, beforeEach } from "vitest";
import { render, screen, renderHook, waitFor, fireEvent, within } from "@testing-library/react";
import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import type { ReactNode } from "react";

const apiGet = vi.fn();
vi.mock("@/lib/api-client", async (orig) => ({
  ...(await orig<typeof import("@/lib/api-client")>()),
  apiGet: (path: string) => apiGet(path),
}));
vi.mock("next-intl", () => ({
  useTranslations: (ns: string) => (key: string) => `${ns}.${key}`,
  useLocale: () => "es",
}));

import { useAllProcedureTemplates } from "@/hooks/useProcedureTemplate";
import { AllProcedureTemplates } from "@/app/dashboard/administration/templates/AllProcedureTemplates";

let qc: QueryClient;
function wrapper({ children }: { children: ReactNode }) {
  return <QueryClientProvider client={qc}>{children}</QueryClientProvider>;
}

const ROUTES: Record<string, unknown> = {
  "/procedure-templates": [
    { fkIdProcedureType: 1, fkIdDocumentType: 10, notes: "Original", version: 0 },
    { fkIdProcedureType: 2, fkIdDocumentType: 11, notes: "", version: 0 },
  ],
  "/procedure-types": [
    { idProcedureType: 1, name: "Compraventa" },
    { idProcedureType: 2, name: "Donación" },
  ],
  "/document-types": [
    { idDocumentType: 10, name: "DNI" },
    { idDocumentType: 11, name: "Escritura anterior" },
  ],
};

beforeEach(() => {
  apiGet.mockReset();
  apiGet.mockImplementation((path: string) => Promise.resolve(ROUTES[path] ?? []));
  qc = new QueryClient({ defaultOptions: { queries: { retry: false } } });
});

describe("useAllProcedureTemplates (#1554)", () => {
  it("calls GET /procedure-templates", async () => {
    const { result } = renderHook(() => useAllProcedureTemplates(), { wrapper });
    await waitFor(() => expect(result.current.isSuccess).toBe(true));
    expect(apiGet).toHaveBeenCalledWith("/procedure-templates");
    expect(result.current.data).toHaveLength(2);
  });
});

describe("AllProcedureTemplates (#1554)", () => {
  it("lists every template with procedure and document names, linking to the editor", async () => {
    render(<AllProcedureTemplates />, { wrapper });
    const table = await screen.findByTestId("all-templates-table");
    await within(table).findByText("Compraventa");
    expect(within(table).getByText("DNI")).toBeInTheDocument();
    expect(within(table).getByText("Donación")).toBeInTheDocument();
    expect(within(table).getByText("Escritura anterior")).toBeInTheDocument();
    expect(screen.getByTestId("all-templates-open-1-10")).toHaveAttribute(
      "href",
      "/dashboard/administration/procedures?checklist=1",
    );
  });

  it("filters by procedure type and by document type", async () => {
    render(<AllProcedureTemplates />, { wrapper });
    const table = await screen.findByTestId("all-templates-table");
    await within(table).findByText("Compraventa");
    fireEvent.change(screen.getByTestId("all-templates-filter-procedure"), { target: { value: "dona" } });
    expect(within(table).queryByText("Compraventa")).not.toBeInTheDocument();
    expect(within(table).getByText("Donación")).toBeInTheDocument();
    fireEvent.change(screen.getByTestId("all-templates-filter-procedure"), { target: { value: "" } });
    fireEvent.change(screen.getByTestId("all-templates-filter-document"), { target: { value: "dni" } });
    expect(within(table).getByText("Compraventa")).toBeInTheDocument();
    expect(within(table).queryByText("Donación")).not.toBeInTheDocument();
  });

  it("shows an empty message when no template matches", async () => {
    render(<AllProcedureTemplates />, { wrapper });
    await screen.findByText("Compraventa");
    fireEvent.change(screen.getByTestId("all-templates-filter-document"), { target: { value: "zzz" } });
    expect(screen.getByTestId("all-templates-empty")).toBeInTheDocument();
  });
});
