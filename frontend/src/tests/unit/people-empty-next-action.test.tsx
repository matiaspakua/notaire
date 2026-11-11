/**
 * People list empty state teaches the next create step (#2071 / RNF-05).
 */
import { describe, it, expect, vi, beforeEach } from "vitest";
import { render, screen, waitFor } from "@testing-library/react";
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

import PeoplePage from "@/app/dashboard/people/page";

function ok(body: unknown, status = 200) {
  const json = JSON.stringify(body);
  return Promise.resolve({ ok: true, status, text: () => Promise.resolve(json) } as Response);
}

function wrapper({ children }: { children: ReactNode }) {
  const client = new QueryClient({ defaultOptions: { queries: { retry: false } } });
  return <QueryClientProvider client={client}>{children}</QueryClientProvider>;
}

describe("PeoplePage empty next action (#2071)", () => {
  beforeEach(() => {
    mockFetch.mockReset();
    mockFetch.mockImplementation((input: RequestInfo | URL) => {
      const url = String(input);
      if (url.includes("/identification-types")) return ok([]);
      return ok({ content: [], number: 0, size: 20, totalElements: 0, totalPages: 0 });
    });
  });

  it("shows the empty description when the people list has no rows", async () => {
    render(<PeoplePage />, { wrapper });
    await waitFor(() => {
      expect(screen.getByTestId("empty-state")).toBeInTheDocument();
    });
    expect(screen.getByTestId("empty-state")).toHaveTextContent("personas.emptyDescription");
    expect(screen.getByTestId("btn-empty-new-person")).toBeInTheDocument();
  });
});
