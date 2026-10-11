/**
 * Testimony edit/delete and movement history (#1364, CU07/CU44): a mistaken testimony can be
 * corrected (number, notes) or deleted with confirmation; a testimony with registry movements
 * answers 409 and the page says why. "Ver movimientos" shows the testimony's movements, newest
 * first, from the list data (GET /testimonies already carries them).
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
  usePathname: () => "/dashboard/testimonies",
  useSearchParams: () => new URLSearchParams(),
}));
vi.mock("next-intl", () => ({
  useTranslations: (ns: string) => (key: string) => `${ns}.${key}`,
}));

import TestimoniesPage from "@/app/dashboard/testimonies/page";

const TESTIMONIES = [
  {
    idTestimony: 7, number: 1200, flagged: false, verified: true, notes: "Primera copia", version: 3,
    deed: { idDeed: 70, number: 500 },
    testimonyMovements: [
      { idTestimonyMovement: 11, dateEntry: "2026-10-01T03:00:00.000Z", registered: true, dateRegistration: "2026-10-03T03:00:00.000Z", cardNumber: 0 },
      { idTestimonyMovement: 12, dateEntry: "2026-10-06T03:00:00.000Z", dateExit: "2026-10-08T03:00:00.000Z", registered: true, cardNumber: 456 },
    ],
  },
  { idTestimony: 8, number: 1201, flagged: false, verified: false, notes: null, version: 0, deed: { idDeed: 71, number: 501 }, testimonyMovements: [] },
];

function res(body: unknown, status = 200) {
  const json = body === undefined ? "" : typeof body === "string" ? body : JSON.stringify(body);
  return Promise.resolve({ ok: status < 400, status, statusText: String(status), text: () => Promise.resolve(json) } as Response);
}

let deleteStatus = 204;
function routeFetch(input: RequestInfo | URL, init?: RequestInit) {
  const url = String(input);
  const method = init?.method ?? "GET";
  if (method === "PUT") return res(undefined, 200);
  if (method === "DELETE") return deleteStatus === 409 ? res("Cannot delete: the testimony has registry movements or issued copies", 409) : res(undefined, 204);
  if (url.includes("/testimonies")) return res(TESTIMONIES);
  return res([]);
}

function wrapper({ children }: { children: ReactNode }) {
  const client = new QueryClient({ defaultOptions: { queries: { retry: false } } });
  return <QueryClientProvider client={client}>{children}</QueryClientProvider>;
}

function calls(method: string) {
  return mockFetch.mock.calls.filter(([, init]) => (init?.method ?? "GET") === method);
}

async function row(id: number) {
  const cell = await screen.findByText(String(TESTIMONIES.find((t) => t.idTestimony === id)!.number));
  return cell.closest("tr") as HTMLElement;
}

describe("Testimonies edit/delete and movement history (#1364)", () => {
  beforeEach(() => {
    vi.clearAllMocks();
    deleteStatus = 204;
    mockFetch.mockImplementation(routeFetch);
  });

  it("edits number and notes with PUT, keeping flagged, verified and the version read", async () => {
    render(<TestimoniesPage />, { wrapper });
    fireEvent.click(within(await row(7)).getByTestId("btn-edit-testimony-7"));
    const dialog = await screen.findByRole("dialog");
    fireEvent.change(within(dialog).getByTestId("input-edit-testimony-number"), { target: { value: "1210" } });
    fireEvent.change(within(dialog).getByTestId("input-edit-testimony-notes"), { target: { value: "Corregido" } });
    fireEvent.click(within(dialog).getByTestId("btn-save-edit-testimony"));
    await waitFor(() => expect(calls("PUT")).toHaveLength(1));
    const [url, init] = calls("PUT")[0];
    expect(String(url)).toContain("/testimonies/7");
    expect(JSON.parse(String(init.body))).toEqual({ number: 1210, notes: "Corregido", flagged: false, verified: true, version: 3 });
  });

  it("does not send an edit without a positive whole number", async () => {
    render(<TestimoniesPage />, { wrapper });
    fireEvent.click(within(await row(7)).getByTestId("btn-edit-testimony-7"));
    const dialog = await screen.findByRole("dialog");
    fireEvent.change(within(dialog).getByTestId("input-edit-testimony-number"), { target: { value: "0" } });
    fireEvent.click(within(dialog).getByTestId("btn-save-edit-testimony"));
    await waitFor(() => expect(toastError).toHaveBeenCalledWith("testimonios.numberRequired"));
    expect(calls("PUT")).toHaveLength(0);
  });

  it("deletes after confirmation with DELETE /testimonies/{id}", async () => {
    render(<TestimoniesPage />, { wrapper });
    fireEvent.click(within(await row(8)).getByTestId("btn-delete-testimony-8"));
    expect(calls("DELETE")).toHaveLength(0);
    fireEvent.click(await screen.findByRole("button", { name: "confirmDialog.confirm" }));
    await waitFor(() => expect(calls("DELETE")).toHaveLength(1));
    expect(String(calls("DELETE")[0][0])).toContain("/testimonies/8");
    await waitFor(() => expect(toastSuccess).toHaveBeenCalledWith("testimonios.deleted"));
  });

  it("explains, in the UI language, why a testimony with movements can't be deleted", async () => {
    deleteStatus = 409;
    render(<TestimoniesPage />, { wrapper });
    fireEvent.click(within(await row(7)).getByTestId("btn-delete-testimony-7"));
    fireEvent.click(await screen.findByRole("button", { name: "confirmDialog.confirm" }));
    await waitFor(() => expect(toastWarning).toHaveBeenCalledWith("testimonios.deleteInUse"));
  });

  it("shows the movements newest first, with the card number of a withdrawal", async () => {
    render(<TestimoniesPage />, { wrapper });
    fireEvent.click(within(await row(7)).getByTestId("btn-movements-testimony-7"));
    const dialog = await screen.findByRole("dialog");
    const items = within(dialog).getAllByTestId(/^testimony-movement-/);
    expect(items.map((i) => i.getAttribute("data-testid"))).toEqual(["testimony-movement-12", "testimony-movement-11"]);
    expect(within(items[0]).getByText(/456/)).toBeTruthy();
  });

  it("says when a testimony has no movements yet", async () => {
    render(<TestimoniesPage />, { wrapper });
    fireEvent.click(within(await row(8)).getByTestId("btn-movements-testimony-8"));
    const dialog = await screen.findByRole("dialog");
    expect(within(dialog).getByText("testimonios.movements.empty")).toBeTruthy();
  });
});
