/**
 * #1546 / CU59: substitutions can be filtered by year (date-range overlap) and by
 * notary (either side, accent/case-insensitive), kept in the URL.
 */
import { describe, it, expect, vi, beforeEach } from "vitest";
import { render, screen, fireEvent } from "@testing-library/react";
import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import type { ReactNode } from "react";

const mockFetch = vi.fn();
vi.stubGlobal("fetch", mockFetch);
let mockSearchParams = new URLSearchParams();
const mockReplace = vi.fn();
vi.mock("next/navigation", () => ({
  useRouter: () => ({ push: vi.fn(), replace: mockReplace }),
  usePathname: () => "/dashboard/substitutions",
  useSearchParams: () => mockSearchParams,
}));
vi.mock("next-intl", () => ({
  useTranslations: (ns: string) => (key: string, v?: Record<string, unknown>) =>
    v ? `${ns}.${key}:${JSON.stringify(v)}` : `${ns}.${key}`,
  useLocale: () => "es",
}));
vi.mock("sonner", () => ({ toast: { error: vi.fn(), success: vi.fn() } }));

import { overlapsYear, matchesNotary, substitutionYears, filterSubstitutions } from "@/lib/substitution-filter";
import SubstitutionsPage from "@/app/dashboard/substitutions/page";

const p = (personId: number, name: string, lastName: string) => ({ personId, name, lastName });
const S = [
  { idSubstitution: 1, dateStart: "2024-12-20T00:00:00", dateEnd: "2025-01-10T00:00:00", fkIdSubstituted: p(1, "José", "Pérez"), fkIdSubstitute: p(2, "Ana", "Gómez") },
  { idSubstitution: 2, dateStart: "2023-03-01T00:00:00", dateEnd: "2023-03-05T00:00:00", fkIdSubstituted: p(3, "Luis", "Martínez"), fkIdSubstitute: p(1, "José", "Pérez") },
  { idSubstitution: 3, dateStart: "2025-06-01T00:00:00", dateEnd: "2025-06-02T00:00:00", fkIdSubstituted: p(4, "Marta", "Ríos"), fkIdSubstitute: p(5, "Pablo", "Sosa") },
];

describe("substitution-filter (#1546)", () => {
  it("a Dec→Jan substitution overlaps both years", () => {
    expect(overlapsYear(S[0], 2024)).toBe(true);
    expect(overlapsYear(S[0], 2025)).toBe(true);
    expect(overlapsYear(S[0], 2023)).toBe(false);
  });
  it("matches the notary on either side, ignoring accents and case", () => {
    expect(matchesNotary(S[0], "jose perez")).toBe(true);
    expect(matchesNotary(S[1], "PÉREZ")).toBe(true);
    expect(matchesNotary(S[0], "gomez")).toBe(true);
    expect(matchesNotary(S[2], "perez")).toBe(false);
    expect(matchesNotary(S[2], "")).toBe(true);
  });
  it("years are those in the data plus the current year, newest first", () => {
    expect(substitutionYears(S, 2026)).toEqual([2026, 2025, 2024, 2023]);
  });
  it("combines year and notary", () => {
    expect(filterSubstitutions(S, { year: 2025, notary: "perez" }).map((s) => s.idSubstitution)).toEqual([1]);
    expect(filterSubstitutions(S, {}).length).toBe(3);
  });
});

function ok(body: unknown) {
  const json = JSON.stringify(body);
  return Promise.resolve({ ok: true, status: 200, text: () => Promise.resolve(json) } as Response);
}
function wrapper({ children }: { children: ReactNode }) {
  const client = new QueryClient({ defaultOptions: { queries: { retry: false } } });
  return <QueryClientProvider client={client}>{children}</QueryClientProvider>;
}

describe("SubstitutionsPage filters (#1546)", () => {
  beforeEach(() => {
    mockFetch.mockReset();
    mockFetch.mockImplementation(() => ok(S));
    mockReplace.mockReset();
    mockSearchParams = new URLSearchParams();
  });

  it("reads year and notary from the URL and shows the count", async () => {
    mockSearchParams = new URLSearchParams("year=2025&notary=perez");
    render(<SubstitutionsPage />, { wrapper });
    expect((await screen.findAllByText("Ana Gómez")).length).toBeGreaterThan(0);
    expect(screen.queryAllByText("Marta Ríos")).toHaveLength(0);
    expect(screen.getByTestId("substitutions-count").textContent).toContain('"count":1');
  });

  it("shows the year empty state", async () => {
    mockSearchParams = new URLSearchParams("year=2026");
    render(<SubstitutionsPage />, { wrapper });
    expect((await screen.findAllByText('suplencias.noDataYear:{"year":2026}')).length).toBeGreaterThan(0);
  });

  it("writes the filters to the URL", async () => {
    render(<SubstitutionsPage />, { wrapper });
    await screen.findAllByText("Ana Gómez");
    fireEvent.change(screen.getByTestId("select-substitutions-year"), { target: { value: "2024" } });
    expect(mockReplace).toHaveBeenLastCalledWith("/dashboard/substitutions?year=2024", { scroll: false });
    fireEvent.change(screen.getByTestId("input-substitutions-notary"), { target: { value: "ana" } });
    expect(mockReplace).toHaveBeenLastCalledWith("/dashboard/substitutions?notary=ana", { scroll: false });
  });
});
