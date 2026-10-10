/**
 * The people list is read one server page at a time (#1340 slice 2, CU18/CU54/CU61).
 * It used to request size=1000, so every person after the 1000th was unreachable
 * and the duplicate-document toast could not link to an existing person outside them.
 */
import { describe, it, expect, vi, beforeEach } from "vitest";
import { render, screen, waitFor, renderHook } from "@testing-library/react";
import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import type { ReactNode } from "react";

const mockFetch = vi.fn();
vi.stubGlobal("fetch", mockFetch);

let mockSearchParams = new URLSearchParams();
const mockReplace = vi.fn();
vi.mock("next/navigation", () => ({
  useRouter: () => ({ push: vi.fn(), replace: mockReplace }),
  usePathname: () => "/dashboard/personas",
  useSearchParams: () => mockSearchParams,
}));
vi.mock("next-intl", () => ({
  useTranslations: (ns: string) => (key: string) => `${ns}.${key}`,
}));
vi.mock("sonner", () => ({ toast: { error: vi.fn(), success: vi.fn() } }));

import { ApiError } from "@/lib/api-client";
import { usePersonasPage } from "@/hooks/usePersonas";
import { presentPersonaSaveError } from "@/lib/persona-save-error";
import PersonasPage from "@/app/dashboard/personas/page";

function pageResponse(body: unknown) {
  const json = JSON.stringify(body);
  return Promise.resolve({ ok: true, status: 200, text: () => Promise.resolve(json) } as Response);
}

function wrapper({ children }: { children: ReactNode }) {
  const client = new QueryClient({ defaultOptions: { queries: { retry: false } } });
  return <QueryClientProvider client={client}>{children}</QueryClientProvider>;
}

const ROW = { personId: 1384, firstName: "Ana", lastName: "Nueva", identificationNumber: "1", isClient: false };

describe("usePersonasPage (#1340)", () => {
  beforeEach(() => mockFetch.mockReset());

  it("requests one page, newest first, and keeps the total", async () => {
    mockFetch.mockReturnValue(
      pageResponse({ content: [ROW], number: 2, size: 50, totalElements: 1384, totalPages: 28 }),
    );
    const { result } = renderHook(() => usePersonasPage({ page: 2, size: 50 }), { wrapper });
    await waitFor(() => expect(result.current.isSuccess).toBe(true));
    const url = String(mockFetch.mock.calls[0][0]);
    expect(url).toMatch(/\/people\?page=2&size=50&sort=idPerson%2Cdesc$/);
    expect(url).not.toContain("size=1000");
    expect(result.current.data?.totalElements).toBe(1384);
  });
});

describe("PersonasPage (#1340)", () => {
  beforeEach(() => {
    mockFetch.mockReset();
    mockReplace.mockReset();
    mockSearchParams = new URLSearchParams();
  });

  it("renders the server page with the pagination footer and never asks for size=1000", async () => {
    mockFetch.mockReturnValue(
      pageResponse({ content: [ROW], number: 0, size: 20, totalElements: 1384, totalPages: 70 }),
    );
    render(<PersonasPage />, { wrapper });
    expect(await screen.findByText("Ana Nueva")).toBeInTheDocument();
    expect(screen.getByRole("navigation")).toBeInTheDocument();
    const urls = mockFetch.mock.calls.map((c) => String(c[0]));
    expect(urls.some((u) => /\/people\?page=0&size=20&sort=idPerson%2Cdesc$/.test(u))).toBe(true);
    expect(urls.some((u) => u.includes("size=1000"))).toBe(false);
  });

  it("reads the page and size from the URL", async () => {
    mockSearchParams = new URLSearchParams("page=3&size=50");
    mockFetch.mockReturnValue(
      pageResponse({ content: [ROW], number: 3, size: 50, totalElements: 1384, totalPages: 28 }),
    );
    render(<PersonasPage />, { wrapper });
    await screen.findByText("Ana Nueva");
    const urls = mockFetch.mock.calls.map((c) => String(c[0]));
    expect(urls.some((u) => /\/people\?page=3&size=50&sort=idPerson%2Cdesc$/.test(u))).toBe(true);
  });
});

describe("PersonasPage out-of-range page (#1340)", () => {
  beforeEach(() => {
    mockFetch.mockReset();
    mockReplace.mockReset();
  });

  it("moves a page past the end (stale link, deleted rows) to the last page", async () => {
    mockSearchParams = new URLSearchParams("page=99");
    mockFetch.mockReturnValue(
      pageResponse({ content: [], number: 99, size: 20, totalElements: 1104, totalPages: 56 }),
    );
    render(<PersonasPage />, { wrapper });
    await waitFor(() => expect(mockReplace).toHaveBeenCalledWith("/dashboard/personas?page=55", { scroll: false }));
  });
});

describe("presentPersonaSaveError duplicate link outside the loaded page (#1340)", () => {
  it("offers the link and loads the existing person by id when it is not on the page", async () => {
    const { toast } = await import("sonner");
    vi.mocked(toast.error).mockClear();
    const onViewExisting = vi.fn();
    const existing = { ...ROW, personId: 7 };
    const loadPersona = vi.fn().mockResolvedValue(existing);
    const err = new ApiError(409, "/people", JSON.stringify({ existingPersonId: 7 }));

    presentPersonaSaveError(err, {
      fallback: "f",
      duplicateDocument: "dup",
      viewExistingLabel: "Ver persona existente",
      personas: [ROW],
      loadPersona,
      onViewExisting,
      setFieldErrors: vi.fn(),
    });

    const opts = vi.mocked(toast.error).mock.calls[0][1] as { action?: { label: string; onClick: () => void } };
    expect(opts.action?.label).toBe("Ver persona existente");
    opts.action?.onClick();
    await waitFor(() => expect(onViewExisting).toHaveBeenCalledWith(existing));
    expect(loadPersona).toHaveBeenCalledWith(7);
  });
});

describe("toast actions over an open dialog (#1340)", () => {
  it("keeps the toaster clickable while a modal sets pointer-events: none on body", async () => {
    const { readFileSync } = await import("node:fs");
    const { resolve } = await import("node:path");
    const css = readFileSync(resolve(__dirname, "../../app/globals.css"), "utf8");
    expect(css).toMatch(/\[data-sonner-toaster\][^{]*\{[^}]*pointer-events:\s*auto/);
  });
});
