/**
 * Required-documents checklist per procedure type (#1362, CU55): on Administración →
 * Trámites an ADMIN lists, adds, edits and removes the document types a procedure needs.
 * The API returns flat rows (fkIdProcedureType, fkIdDocumentType, notes), so names come
 * from the document-type catalog.
 */
import { describe, it, expect, vi, beforeEach } from "vitest";
import { render, screen, waitFor, within, fireEvent } from "@testing-library/react";
import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import type { ReactNode } from "react";

const mockFetch = vi.fn();
vi.stubGlobal("fetch", mockFetch);

const toastError = vi.fn();
const toastSuccess = vi.fn();
vi.mock("sonner", () => ({ toast: { error: (m: string) => toastError(m), success: (m: string) => toastSuccess(m), warning: vi.fn() } }));
vi.mock("next-intl", () => ({
  useTranslations: (ns: string) => (key: string) => `${ns}.${key}`,
}));

import { RequiredDocumentsDialog } from "@/app/dashboard/administration/procedures/RequiredDocumentsDialog";

const DOC_TYPES = [
  { idDocumentType: 1, name: "Libre Deuda Municipal", expires: true, dueDays: 30, deliveredBy: "Entidad" },
  { idDocumentType: 2, name: "Informe de Dominio", expires: true, dueDays: 60, deliveredBy: "Entidad" },
  { idDocumentType: 3, name: "Título de propiedad", expires: false, deliveredBy: "Cliente" },
];
let rows = [{ fkIdProcedureType: 7, fkIdDocumentType: 1, notes: "Original", version: 0 }];
let postStatus = 201;

function res(body: unknown, status = 200) {
  const json = body === undefined ? "" : JSON.stringify(body);
  return Promise.resolve({ ok: status < 400, status, statusText: String(status), text: () => Promise.resolve(json) } as Response);
}

function routeFetch(input: RequestInfo | URL, init?: RequestInit) {
  const url = String(input);
  const method = init?.method ?? "GET";
  if (method === "POST") return postStatus === 409 ? res({ status: 409, message: "The submitted data duplicates an existing record" }, 409) : res({ ...JSON.parse(String(init?.body)), version: 0 }, 201);
  if (method === "PUT") return res({ ...JSON.parse(String(init?.body)), version: 1 }, 200);
  if (method === "DELETE") return res(undefined, 204);
  if (url.includes("/procedure-templates/procedure-types/7")) return res(rows);
  if (url.includes("/document-types")) return res(DOC_TYPES);
  return res([]);
}

function renderDialog() {
  const client = new QueryClient({ defaultOptions: { queries: { retry: false } } });
  const wrapper = ({ children }: { children: ReactNode }) => <QueryClientProvider client={client}>{children}</QueryClientProvider>;
  return render(<RequiredDocumentsDialog procedureType={{ idProcedureType: 7, name: "Compraventa" }} onClose={vi.fn()} />, { wrapper });
}

function calls(method: string) {
  return mockFetch.mock.calls.filter(([, init]) => (init?.method ?? "GET") === method);
}

describe("RequiredDocumentsDialog (#1362)", () => {
  beforeEach(() => {
    vi.clearAllMocks();
    rows = [{ fkIdProcedureType: 7, fkIdDocumentType: 1, notes: "Original", version: 0 }];
    postStatus = 201;
    mockFetch.mockImplementation(routeFetch);
  });

  it("lists the required documents by name, with their notes", async () => {
    renderDialog();
    const row = await screen.findByTestId("required-doc-row-1");
    expect(within(row).getByText("Libre Deuda Municipal")).toBeTruthy();
    expect(within(row).getByText("Original")).toBeTruthy();
  });

  it("offers only document types that aren't in the checklist yet", async () => {
    renderDialog();
    await screen.findByTestId("required-doc-row-1");
    const select = screen.getByTestId("select-required-doc-type") as HTMLSelectElement;
    const values = Array.from(select.options).map((o) => o.value).filter(Boolean);
    expect(values).toEqual(["2", "3"]);
  });

  it("adds a document with POST /procedure-templates", async () => {
    renderDialog();
    await screen.findByTestId("required-doc-row-1");
    fireEvent.change(screen.getByTestId("select-required-doc-type"), { target: { value: "3" } });
    fireEvent.change(screen.getByTestId("input-required-doc-notes"), { target: { value: "Copia certificada" } });
    fireEvent.click(screen.getByTestId("btn-add-required-doc"));
    await waitFor(() => expect(calls("POST")).toHaveLength(1));
    const [url, init] = calls("POST")[0];
    expect(String(url)).toContain("/procedure-templates");
    expect(JSON.parse(String(init.body))).toEqual({ fkIdProcedureType: 7, fkIdDocumentType: 3, notes: "Copia certificada" });
  });

  it("shows a duplicate message when the API answers 409", async () => {
    postStatus = 409;
    renderDialog();
    await screen.findByTestId("required-doc-row-1");
    fireEvent.change(screen.getByTestId("select-required-doc-type"), { target: { value: "2" } });
    fireEvent.click(screen.getByTestId("btn-add-required-doc"));
    await waitFor(() => expect(toastError).toHaveBeenCalledWith("administracion.tramites.requiredDocs.duplicate"));
  });

  it("edits the notes with PUT /procedure-templates/{type}/{document}", async () => {
    renderDialog();
    const row = await screen.findByTestId("required-doc-row-1");
    fireEvent.click(within(row).getByTestId("btn-edit-required-doc"));
    const input = within(row).getByTestId("input-edit-required-doc-notes");
    fireEvent.change(input, { target: { value: "Original y copia" } });
    fireEvent.click(within(row).getByTestId("btn-save-required-doc"));
    await waitFor(() => expect(calls("PUT")).toHaveLength(1));
    const [url, init] = calls("PUT")[0];
    expect(String(url)).toContain("/procedure-templates/7/1");
    expect(JSON.parse(String(init.body))).toEqual({ notes: "Original y copia" });
  });

  it("removes a document after confirmation with DELETE /procedure-templates/{type}/{document}", async () => {
    renderDialog();
    const row = await screen.findByTestId("required-doc-row-1");
    fireEvent.click(within(row).getByTestId("btn-delete-required-doc"));
    expect(calls("DELETE")).toHaveLength(0);
    fireEvent.click(await screen.findByRole("button", { name: "confirmDialog.confirm" }));
    await waitFor(() => expect(calls("DELETE")).toHaveLength(1));
    expect(String(calls("DELETE")[0][0])).toContain("/procedure-templates/7/1");
  });
});
