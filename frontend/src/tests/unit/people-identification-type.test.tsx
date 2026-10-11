/**
 * The person form picks the identification type (#1361, RF-37). Owner decision:
 * required when creating a person, DNI preselected; a person stored without a type
 * asks for one on save. The list shows the type next to the number ("CUIT 20…").
 */
import { describe, it, expect, vi, beforeEach } from "vitest";
import { render, screen, waitFor, within, fireEvent } from "@testing-library/react";
import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import type { ReactNode } from "react";

const mockFetch = vi.fn();
vi.stubGlobal("fetch", mockFetch);

vi.mock("next/navigation", () => ({
  useRouter: () => ({ push: vi.fn(), replace: vi.fn() }),
  usePathname: () => "/dashboard/people",
  useSearchParams: () => new URLSearchParams(),
}));
vi.mock("next-intl", () => ({
  useTranslations: (ns: string) => (key: string) => `${ns}.${key}`,
}));
vi.mock("sonner", () => ({ toast: { error: vi.fn(), success: vi.fn() } }));

import PersonasPage from "@/app/dashboard/people/page";

const TYPES = [
  { idIdentificationType: 1, name: "DNI", characters: "8", version: 0 },
  { idIdentificationType: 4, name: "Pasaporte", characters: "12", version: 0 },
  { idIdentificationType: 5, name: "CUIT", characters: "11", version: 0 },
];
const CUIT_PERSON = {
  personId: 7, firstName: "Empresa", lastName: "SA", identificationNumber: "30712345678",
  identificationTypeId: 5, isClient: true, version: 0,
};
const UNTYPED_PERSON = {
  personId: 8, firstName: "Sin", lastName: "Tipo", identificationNumber: "123",
  identificationTypeId: null, isClient: false, version: 0,
};

function ok(body: unknown, status = 200) {
  const json = JSON.stringify(body);
  return Promise.resolve({ ok: true, status, text: () => Promise.resolve(json) } as Response);
}

function wrapper({ children }: { children: ReactNode }) {
  const client = new QueryClient({ defaultOptions: { queries: { retry: false } } });
  return <QueryClientProvider client={client}>{children}</QueryClientProvider>;
}

function routeFetch(input: RequestInfo | URL, init?: RequestInit) {
  const url = String(input);
  const method = init?.method ?? "GET";
  if (url.includes("/identification-types")) return ok(TYPES);
  if (method === "POST" && url.includes("/people")) return ok({ ...CUIT_PERSON, personId: 9 }, 201);
  if (method === "PUT" && url.includes("/people/")) return ok(CUIT_PERSON);
  return ok({ content: [CUIT_PERSON, UNTYPED_PERSON], number: 0, size: 20, totalElements: 2, totalPages: 1 });
}

function sentBodies(method: string) {
  return mockFetch.mock.calls
    .filter(([, init]) => (init as RequestInit | undefined)?.method === method)
    .map(([, init]) => JSON.parse(String((init as RequestInit).body)));
}

describe("PersonasPage identification type (#1361)", () => {
  beforeEach(() => {
    mockFetch.mockReset();
    mockFetch.mockImplementation(routeFetch);
  });

  it("lists the type next to the number", async () => {
    render(<PersonasPage />, { wrapper });
    expect(await screen.findByText("CUIT 30712345678")).toBeInTheDocument();
  });

  it("creates a person with DNI preselected and sends identificationTypeId", async () => {
    render(<PersonasPage />, { wrapper });
    await screen.findByText("CUIT 30712345678");
    fireEvent.click(screen.getByTestId("btn-new-person"));
    const dialog = await screen.findByRole("dialog");
    const typeSelect = within(dialog).getByRole("combobox", { name: /personas\.fields\.tipoIdentificacion/ });
    await waitFor(() => expect(typeSelect).toHaveTextContent("DNI"));
    const number = within(dialog).getByTestId("input-dni");
    fireEvent.change(within(dialog).getByTestId("input-firstName"), { target: { value: "Ana" } });
    fireEvent.change(within(dialog).getByTestId("input-lastName"), { target: { value: "Gomez" } });
    fireEvent.change(number, { target: { value: "20123456" } });
    fireEvent.click(within(dialog).getByRole("button", { name: "common.create" }));
    await waitFor(() => expect(sentBodies("POST")).toHaveLength(1));
    expect(sentBodies("POST")[0].identificationTypeId).toBe(1);
  });

  it("asks for a type before saving a person stored without one", async () => {
    render(<PersonasPage />, { wrapper });
    await screen.findByText("Sin Tipo");
    const rows = screen.getAllByRole("button", { name: "common.edit" });
    fireEvent.click(rows[1]);
    const dialog = await screen.findByRole("dialog");
    const typeSelect = within(dialog).getByRole("combobox", { name: /personas\.fields\.tipoIdentificacion/ });
    expect(typeSelect).not.toHaveTextContent("DNI");
    fireEvent.click(within(dialog).getByRole("button", { name: "common.update" }));
    expect(await within(dialog).findByText("personas.validation.tipoIdentificacionRequired")).toBeInTheDocument();
    expect(sentBodies("PUT")).toHaveLength(0);
  });
});
