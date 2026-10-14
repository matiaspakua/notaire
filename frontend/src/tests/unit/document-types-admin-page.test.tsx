/**
 * Document type catalog (CU39, #1246): a document type needs a name, one that expires needs its due
 * days, clearing the expiry clears the days, and a type in use is not offered for deletion.
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
  usePathname: () => "/dashboard/administration/documents",
  useSearchParams: () => new URLSearchParams(),
}));
vi.mock("next-intl", () => ({
  useTranslations: (ns: string) => (key: string) => `${ns}.${key}`,
  useLocale: () => "es",
}));

import DocumentTypesPage from "@/app/dashboard/administration/documents/page";

const TYPES = [
  { idDocumentType: 3, name: "DNI", enabled: true, returned: false, expires: false, dueDays: null },
  { idDocumentType: 4, name: "Certificado", enabled: true, returned: true, expires: true, dueDays: 30 },
];

let inUse = false;
function route(path: string) {
  if (path.endsWith("/in-use")) return Promise.resolve({ inUse });
  if (path.startsWith("/document-types/search")) return Promise.resolve([TYPES[1]]);
  if (path.startsWith("/document-types")) return Promise.resolve(TYPES);
  return Promise.resolve([]);
}

function wrapper({ children }: { children: ReactNode }) {
  const client = new QueryClient({ defaultOptions: { queries: { retry: false } } });
  return <QueryClientProvider client={client}>{children}</QueryClientProvider>;
}

async function renderPage() {
  render(<DocumentTypesPage />, { wrapper });
  await screen.findByText("DNI");
}

function row(name: string) {
  return screen.getAllByRole("row").find((r) => within(r).queryByText(name))!;
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

describe("document types", () => {
  it("needs a name", async () => {
    await renderPage();
    fireEvent.click(screen.getByTestId("btn-new-document-type"));
    await screen.findByRole("dialog");

    fireEvent.click(saveButton());

    expect(toast.error).toHaveBeenCalledWith("administracion.documentos.nameRequired");
    expect(apiPost).not.toHaveBeenCalled();
  });

  it("asks for the due days of a document that expires", async () => {
    await renderPage();
    fireEvent.click(screen.getByTestId("btn-new-document-type"));
    fireEvent.change(await screen.findByTestId("input-name-document"), { target: { value: "Poder" } });
    fireEvent.click(screen.getByTestId("checkbox-expires-document"));

    fireEvent.click(saveButton());

    expect(toast.error).toHaveBeenCalledWith("administracion.documentos.diasVencimientoRequired");
    expect(apiPost).not.toHaveBeenCalled();
  });

  it("creates a document type that expires with its due days", async () => {
    await renderPage();
    fireEvent.click(screen.getByTestId("btn-new-document-type"));
    fireEvent.change(await screen.findByTestId("input-name-document"), { target: { value: "Poder" } });
    fireEvent.click(screen.getByTestId("checkbox-expires-document"));
    fireEvent.change(screen.getByTestId("input-due-days-document"), { target: { value: "90" } });

    fireEvent.click(saveButton());

    await waitFor(() =>
      expect(apiPost).toHaveBeenCalledWith(
        "/document-types",
        expect.objectContaining({ name: "Poder", expires: true, dueDays: 90 }),
      ),
    );
  });

  it("clears the due days when the expiry is switched off", async () => {
    await renderPage();
    fireEvent.click(within(row("Certificado")).getByRole("button", { name: "common.edit" }));
    const days = await screen.findByTestId("input-due-days-document");
    expect(days).toHaveValue(30);

    fireEvent.click(screen.getByTestId("checkbox-expires-document"));
    expect(screen.queryByTestId("input-due-days-document")).toBeNull();
    fireEvent.click(saveButton());

    await waitFor(() =>
      expect(apiPut).toHaveBeenCalledWith(
        "/document-types/4",
        expect.objectContaining({ expires: false, dueDays: null }),
      ),
    );
  });

  it("deletes an unused document type after confirmation", async () => {
    await renderPage();
    fireEvent.click(within(row("DNI")).getByTestId("btn-delete-document"));

    fireEvent.click(await screen.findByTestId("confirm-dialog-confirm"));

    await waitFor(() => expect(apiDelete).toHaveBeenCalledWith("/document-types/3"));
  });

  it("warns instead of asking to delete a document type in use", async () => {
    inUse = true;
    await renderPage();

    fireEvent.click(within(row("DNI")).getByTestId("btn-delete-document"));

    await waitFor(() => expect(toast.warning).toHaveBeenCalledWith("administracion.documentos.inUseCannotDelete"));
    expect(apiDelete).not.toHaveBeenCalled();
  });

  it("searches document types by name on the server", async () => {
    await renderPage();

    fireEvent.change(screen.getByTestId("input-search-document"), { target: { value: "cert" } });

    await waitFor(() => expect(apiGet).toHaveBeenCalledWith("/document-types/search?name=cert"));
    await waitFor(() => expect(screen.queryByText("DNI")).toBeNull());
  });
});
