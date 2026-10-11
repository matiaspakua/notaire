/**
 * Identification-type admin catalog (#1361 slice 2, RF-53): ADMIN lists, creates,
 * edits and deletes the types the person form offers (DNI, CUIT, Pasaporte, …).
 * The API answers 409 with a reason for a duplicate name or a type people use.
 */
import { describe, it, expect, vi, beforeEach } from "vitest";
import { render, screen, waitFor, within, fireEvent } from "@testing-library/react";
import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import type { ReactNode } from "react";

const mockFetch = vi.fn();
vi.stubGlobal("fetch", mockFetch);

const toastError = vi.fn();
const toastSuccess = vi.fn();
const toastWarning = vi.fn();
vi.mock("sonner", () => ({ toast: { error: (m: string) => toastError(m), success: (m: string) => toastSuccess(m), warning: (m: string) => toastWarning(m) } }));
vi.mock("next/navigation", () => ({
  useRouter: () => ({ push: vi.fn(), replace: vi.fn() }),
  usePathname: () => "/dashboard/administration/identification-types",
  useSearchParams: () => new URLSearchParams(),
}));
vi.mock("next-intl", () => ({
  useTranslations: (ns: string) => (key: string) => `${ns}.${key}`,
}));

import IdentificationTypesPage from "@/app/dashboard/administration/identification-types/page";

const TYPES = [
  { idIdentificationType: 1, name: "DNI", characters: "8", version: 0 },
  { idIdentificationType: 5, name: "CUIT", characters: "11", version: 0 },
];

function res(body: unknown, status = 200) {
  const json = body === undefined ? "" : JSON.stringify(body);
  return Promise.resolve({ ok: status < 400, status, statusText: String(status), text: () => Promise.resolve(json) } as Response);
}

function wrapper({ children }: { children: ReactNode }) {
  const client = new QueryClient({ defaultOptions: { queries: { retry: false } } });
  return <QueryClientProvider client={client}>{children}</QueryClientProvider>;
}

let deleteStatus = 204;
function routeFetch(input: RequestInfo | URL, init?: RequestInit) {
  const url = String(input);
  const method = init?.method ?? "GET";
  if (method === "POST") return res({ idIdentificationType: 9, name: "Identity card", characters: "10", version: 0 }, 201);
  if (method === "PUT") return res(undefined, 200);
  if (method === "DELETE") {
    return deleteStatus === 409
      ? res({ status: 409, message: "Cannot delete: the identification type is in use by people" }, 409)
      : res(undefined, 204);
  }
  if (url.includes("/identification-types")) return res(TYPES);
  return res([]);
}

function calls(method: string) {
  return mockFetch.mock.calls.filter(([, init]) => ((init as RequestInit | undefined)?.method ?? "GET") === method);
}

describe("Identification types admin page (#1361)", () => {
  beforeEach(() => {
    mockFetch.mockReset();
    mockFetch.mockImplementation(routeFetch);
    toastError.mockReset();
    toastSuccess.mockReset();
    toastWarning.mockReset();
    deleteStatus = 204;
  });

  it("lists every type with its number length", async () => {
    render(<IdentificationTypesPage />, { wrapper });
    const row = (await screen.findByText("CUIT")).closest("tr")!;
    expect(within(row).getByText("11")).toBeInTheDocument();
    expect(screen.getByText("DNI")).toBeInTheDocument();
  });

  it("creates a type with a trimmed name and its length", async () => {
    render(<IdentificationTypesPage />, { wrapper });
    await screen.findByText("DNI");
    fireEvent.click(screen.getByTestId("btn-new-identification-type"));
    const dialog = await screen.findByRole("dialog");
    fireEvent.change(within(dialog).getByTestId("input-identification-type-name"), { target: { value: "  Identity card " } });
    fireEvent.change(within(dialog).getByTestId("input-identification-type-characters"), { target: { value: "10" } });
    fireEvent.click(within(dialog).getByTestId("btn-save-identification-type"));
    await waitFor(() => expect(calls("POST")).toHaveLength(1));
    const [url, init] = calls("POST")[0];
    expect(String(url)).toContain("/identification-types");
    expect(JSON.parse(String((init as RequestInit).body))).toEqual({ name: "Identity card", characters: "10" });
  });

  it("does not send a type without a name or a length", async () => {
    render(<IdentificationTypesPage />, { wrapper });
    await screen.findByText("DNI");
    fireEvent.click(screen.getByTestId("btn-new-identification-type"));
    const dialog = await screen.findByRole("dialog");
    fireEvent.change(within(dialog).getByTestId("input-identification-type-name"), { target: { value: "Identity card" } });
    fireEvent.click(within(dialog).getByTestId("btn-save-identification-type"));
    await waitFor(() => expect(toastError).toHaveBeenCalled());
    expect(calls("POST")).toHaveLength(0);
  });

  it("explains a duplicate name in the UI language", async () => {
    mockFetch.mockImplementation((input: RequestInfo | URL, init?: RequestInit) =>
      (init?.method ?? "GET") === "POST"
        ? res({ status: 409, message: "An identification type with this name already exists" }, 409)
        : routeFetch(input, init),
    );
    render(<IdentificationTypesPage />, { wrapper });
    await screen.findByText("DNI");
    fireEvent.click(screen.getByTestId("btn-new-identification-type"));
    const dialog = await screen.findByRole("dialog");
    fireEvent.change(within(dialog).getByTestId("input-identification-type-name"), { target: { value: "dni" } });
    fireEvent.change(within(dialog).getByTestId("input-identification-type-characters"), { target: { value: "8" } });
    fireEvent.click(within(dialog).getByTestId("btn-save-identification-type"));
    await waitFor(() => expect(toastError).toHaveBeenCalledWith("administracion.tiposIdentificacion.duplicateName"));
    expect(screen.getByRole("dialog")).toBeInTheDocument();
  });

  it("edits a type with PUT on its id", async () => {
    render(<IdentificationTypesPage />, { wrapper });
    const row = (await screen.findByText("CUIT")).closest("tr")!;
    fireEvent.click(within(row).getByTestId("btn-edit-identification-type"));
    const dialog = await screen.findByRole("dialog");
    const name = within(dialog).getByTestId("input-identification-type-name") as HTMLInputElement;
    expect(name.value).toBe("CUIT");
    fireEvent.change(within(dialog).getByTestId("input-identification-type-characters"), { target: { value: "13" } });
    fireEvent.click(within(dialog).getByTestId("btn-save-identification-type"));
    await waitFor(() => expect(calls("PUT")).toHaveLength(1));
    const [url, init] = calls("PUT")[0];
    expect(String(url)).toContain("/identification-types/5");
    expect(JSON.parse(String((init as RequestInit).body))).toEqual({ name: "CUIT", characters: "13" });
  });

  it("deletes after confirmation", async () => {
    render(<IdentificationTypesPage />, { wrapper });
    const row = (await screen.findByText("CUIT")).closest("tr")!;
    fireEvent.click(within(row).getByTestId("btn-delete-identification-type"));
    fireEvent.click(await screen.findByRole("button", { name: "confirmDialog.confirm" }));
    await waitFor(() => expect(calls("DELETE")).toHaveLength(1));
    expect(String(calls("DELETE")[0][0])).toContain("/identification-types/5");
  });

  it("shows the in-use reason when people use the type", async () => {
    deleteStatus = 409;
    render(<IdentificationTypesPage />, { wrapper });
    const row = (await screen.findByText("DNI")).closest("tr")!;
    fireEvent.click(within(row).getByTestId("btn-delete-identification-type"));
    fireEvent.click(await screen.findByRole("button", { name: "confirmDialog.confirm" }));
    await waitFor(() => expect(toastWarning).toHaveBeenCalledWith("common.errors.inUse"));
    expect(toastSuccess).not.toHaveBeenCalled();
  });
});

describe("Administration index (#1361)", () => {
  it("links the identification types catalog", async () => {
    const { default: AdministrationPage } = await import("@/app/dashboard/administration/page");
    render(<AdministrationPage />, { wrapper });
    const link = screen.getByText("administracion.modules.tiposIdentificacion.label").closest("a")!;
    expect(link.getAttribute("href")).toBe("/dashboard/administration/identification-types");
  });
});
