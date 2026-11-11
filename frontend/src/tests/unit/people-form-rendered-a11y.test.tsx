/**
 * People create dialog — rendered form-pattern contract (#2087 / RNF-07 / RNF-05).
 * Locks labels, required, errors, loading busy/disabled, and primary size token.
 */
import { describe, it, expect, vi, beforeEach } from "vitest";
import { render, screen, waitFor, within, fireEvent } from "@testing-library/react";
import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import type { ReactNode } from "react";
import { readFileSync } from "node:fs";
import { resolve } from "node:path";

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

import PeoplePage from "@/app/dashboard/people/page";

const TYPES = [{ idIdentificationType: 1, name: "DNI", characters: "8", version: 0 }];

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
  if (method === "POST" && url.includes("/people")) {
    return new Promise<Response>(() => {
      /* hang so isPending stays true */
    });
  }
  return ok({ content: [], number: 0, size: 20, totalElements: 0, totalPages: 0 });
}

describe("PeoplePage form rendered a11y (#2087)", () => {
  beforeEach(() => {
    mockFetch.mockReset();
    mockFetch.mockImplementation(routeFetch);
  });

  it("labels required fields with a visible * and aria-required", async () => {
    render(<PeoplePage />, { wrapper });
    fireEvent.click(await screen.findByTestId("btn-new-person"));
    const dialog = await screen.findByRole("dialog");
    const first = within(dialog).getByTestId("input-firstName");
    expect(first).toHaveAttribute("aria-required", "true");
    expect(within(dialog).getByText(/personas\.fields\.nombre/i).textContent).toMatch(/\*/);
    expect(within(dialog).getByRole("combobox", { name: /tipoIdentificacion/i })).toHaveAttribute(
      "aria-required",
      "true",
    );
  });

  it("shows linked alerts and aria-invalid when required fields are empty", async () => {
    render(<PeoplePage />, { wrapper });
    fireEvent.click(await screen.findByTestId("btn-new-person"));
    const dialog = await screen.findByRole("dialog");
    fireEvent.click(within(dialog).getByTestId("btn-save-person"));
    await waitFor(() => expect(within(dialog).getAllByRole("alert").length).toBeGreaterThan(0));
    expect(within(dialog).getByTestId("input-firstName")).toHaveAttribute("aria-invalid", "true");
    expect(within(dialog).getByTestId("input-lastName")).toHaveAttribute("aria-invalid", "true");
    expect(within(dialog).getByTestId("input-dni")).toHaveAttribute("aria-invalid", "true");
  });

  it("disables the primary save and marks it busy while create is pending", async () => {
    render(<PeoplePage />, { wrapper });
    fireEvent.click(await screen.findByTestId("btn-new-person"));
    const dialog = await screen.findByRole("dialog");
    fireEvent.change(within(dialog).getByTestId("input-firstName"), { target: { value: "Ana" } });
    fireEvent.change(within(dialog).getByTestId("input-lastName"), { target: { value: "Gomez" } });
    fireEvent.change(within(dialog).getByTestId("input-dni"), { target: { value: "20123456" } });
    const save = within(dialog).getByTestId("btn-save-person");
    fireEvent.click(save);
    await waitFor(() => expect(save).toBeDisabled());
    expect(save).toHaveAttribute("aria-busy", "true");
  });

  it("uses the default (h-11 / 44px) size token on the dialog primary action", () => {
    const src = readFileSync(resolve(__dirname, "../../app/dashboard/people/page.tsx"), "utf8");
    expect(src).toMatch(/data-testid="btn-save-person"/);
    // Primary save must not force size="sm" (32px); default button is h-11.
    const saveBlock = src.slice(src.indexOf("btn-save-person") - 120, src.indexOf("btn-save-person") + 80);
    expect(saveBlock).not.toMatch(/size="sm"/);
  });

  it("stacks people dialog field rows on narrow viewports", () => {
    const src = readFileSync(resolve(__dirname, "../../app/dashboard/people/page.tsx"), "utf8");
    expect(src).toMatch(/grid-cols-1\s+sm:grid-cols-2/);
  });
});
