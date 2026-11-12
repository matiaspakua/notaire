/**
 * CU22 / #2259 forms audit: the API requires the start and end dates of a substitution
 * (400 "dateStart: must not be null"), so the dialog marks them required, shows the error on the
 * field, and sends nothing until both are filled.
 */
import { describe, it, expect, vi, beforeEach } from "vitest";
import { fireEvent, render, screen, waitFor, within } from "@testing-library/react";
import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import type { ReactNode } from "react";

const mockFetch = vi.fn();
vi.stubGlobal("fetch", mockFetch);
vi.mock("next/navigation", () => ({
  useRouter: () => ({ push: vi.fn(), replace: vi.fn() }),
  usePathname: () => "/dashboard/substitutions",
  useSearchParams: () => new URLSearchParams(),
}));
vi.mock("next-intl", () => ({
  useTranslations: (ns: string) => (key: string, v?: Record<string, unknown>) =>
    v ? `${ns}.${key}:${JSON.stringify(v)}` : `${ns}.${key}`,
  useLocale: () => "es",
}));
vi.mock("sonner", () => ({ toast: { error: vi.fn(), success: vi.fn(), warning: vi.fn() } }));

import SubstitutionsPage from "@/app/dashboard/substitutions/page";

function reply(body: unknown, status = 200) {
  const text = JSON.stringify(body);
  return Promise.resolve({ ok: status < 400, status, text: () => Promise.resolve(text) } as Response);
}

function wrapper({ children }: { children: ReactNode }) {
  const client = new QueryClient({ defaultOptions: { queries: { retry: false } } });
  return <QueryClientProvider client={client}>{children}</QueryClientProvider>;
}

const posts = () => mockFetch.mock.calls.filter(([, init]) => (init as RequestInit | undefined)?.method === "POST");
const saveButton = () => screen.getByTestId("btn-save-substitution");
const startDate = () => screen.getByTestId("input-start-date");
const endDate = () => screen.getByTestId("input-end-date");

async function openCreate() {
  render(<SubstitutionsPage />, { wrapper });
  fireEvent.click(await screen.findByTestId("btn-new-substitution"));
  await screen.findByTestId("input-start-date");
}

function errorOf(input: HTMLElement): string | undefined {
  const id = input.getAttribute("aria-describedby")?.split(" ")[0];
  return id ? document.getElementById(id)?.textContent ?? undefined : undefined;
}

beforeEach(() => {
  mockFetch.mockReset();
  mockFetch.mockImplementation((_url: string, init?: RequestInit) =>
    reply(init?.method === "POST" ? { idSubstitution: 9 } : []),
  );
});

describe("substitution dialog dates", () => {
  it("marks both dates invalid with a described error and sends nothing when they are missing", async () => {
    await openCreate();

    fireEvent.click(saveButton());

    for (const input of [startDate(), endDate()]) {
      await waitFor(() => expect(input).toHaveAttribute("aria-invalid", "true"));
      expect(errorOf(input)).toBe("common.fieldRequired");
      expect(input).toHaveAttribute("aria-required", "true");
    }
    expect(posts()).toHaveLength(0);
  });

  it("clears only the error of the date that was entered", async () => {
    await openCreate();
    fireEvent.click(saveButton());
    await waitFor(() => expect(startDate()).toHaveAttribute("aria-invalid", "true"));

    fireEvent.change(startDate(), { target: { value: "2026-03-01" } });

    expect(startDate()).not.toHaveAttribute("aria-invalid");
    expect(endDate()).toHaveAttribute("aria-invalid", "true");
  });

  it("sends the substitution once both dates are filled", async () => {
    await openCreate();
    fireEvent.change(startDate(), { target: { value: "2026-03-01" } });
    fireEvent.change(endDate(), { target: { value: "2026-03-05" } });

    fireEvent.click(saveButton());

    await waitFor(() => expect(posts()).toHaveLength(1));
    const [, init] = posts()[0];
    expect(JSON.parse(String((init as RequestInit).body))).toMatchObject({
      dateStart: "2026-03-01",
      dateEnd: "2026-03-05",
    });
    expect(within(document.body).queryByText("common.fieldRequired")).toBeNull();
  });
});
