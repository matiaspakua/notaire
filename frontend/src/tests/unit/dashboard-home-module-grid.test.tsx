/**
 * #2072 / RNF-05: home module grid keeps token chrome, drops decorative icon
 * scale, and limits San Francisco Blue wells so the catalog does not tint every
 * destination like the sidebar. Mist wells + keyboard-reachable links.
 */
import { describe, it, expect, vi, beforeEach } from "vitest";
import { readFileSync } from "node:fs";
import { resolve } from "node:path";
import { render, screen } from "@testing-library/react";
import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import type { ReactNode } from "react";

vi.mock("next-intl", () => ({
  useTranslations: (ns: string) => (key: string) => `${ns}.${key}`,
  useLocale: () => "es",
}));
vi.mock("next/navigation", () => ({
  useRouter: () => ({ push: vi.fn(), replace: vi.fn() }),
  usePathname: () => "/dashboard",
  useSearchParams: () => new URLSearchParams(),
}));
vi.mock("next/link", () => ({
  default: ({ href, children, ...rest }: { href: string; children: ReactNode }) => (
    <a href={href} {...rest}>
      {children}
    </a>
  ),
}));
vi.mock("@/hooks/useUpcomingExpirations", () => ({
  UPCOMING_EXPIRATIONS_DEFAULT_DAYS: 30,
  useUpcomingExpirations: () => ({ data: [], isError: false }),
}));
vi.mock("@/hooks/useManagements", () => ({
  useManagementsPage: () => ({
    data: { totalElements: 0 },
    isLoading: false,
    isError: false,
    refetch: vi.fn(),
  }),
}));
vi.mock("@/hooks/usePeople", () => ({
  usePeoplePage: () => ({
    data: { totalElements: 0 },
    isLoading: false,
    isError: false,
    refetch: vi.fn(),
  }),
}));
vi.mock("@/hooks/useBudgets", () => ({
  useBudgetsPage: () => ({
    data: { totalElements: 0 },
    isLoading: false,
    isError: false,
    refetch: vi.fn(),
  }),
}));
vi.mock("@/components/workflow/WorkflowHero", () => ({
  WorkflowHero: () => <div data-testid="workflow-hero-stub" />,
}));
vi.mock("@/store/auth-store", () => ({
  useAuthStore: () => ({
    user: { name: "Ada Clerk" },
    isAdmin: () => true,
  }),
}));

import DashboardPage from "@/app/dashboard/page";

const PAGE = resolve(__dirname, "../../app/dashboard/page.tsx");

/** Module-grid JSX block only (avoids metric-card primary tints). */
function moduleGridSource(): string {
  const src = readFileSync(PAGE, "utf8");
  const start = src.indexOf('data-testid="dashboard-modules"');
  expect(start).toBeGreaterThan(-1);
  return src.slice(start);
}

function wrapper({ children }: { children: ReactNode }) {
  const client = new QueryClient({ defaultOptions: { queries: { retry: false } } });
  return <QueryClientProvider client={client}>{children}</QueryClientProvider>;
}

describe("dashboard home module grid (#2072)", () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  it("module icon wells have no scale-110", () => {
    const grid = moduleGridSource();
    expect(grid).not.toMatch(/scale-110/);
  });

  it("at most one SF Blue module well (zero when expirations alert is the accent)", () => {
    const grid = moduleGridSource();
    const primaryWells = grid.match(/bg-primary\/10[^"']*text-primary|text-primary[^"']*bg-primary\/10/g) ?? [];
    // Decision: expirations alert is the accent → zero SF Blue module wells.
    expect(primaryWells.length).toBeLessThanOrEqual(1);
    expect(primaryWells.length).toBe(0);
  });

  it("non-accent wells use mist (bg-secondary)", () => {
    const grid = moduleGridSource();
    expect(grid).toMatch(/bg-secondary/);
    expect(grid).not.toMatch(/bg-primary\/10/);
  });

  it("module links remain keyboard-reachable", () => {
    render(<DashboardPage />, { wrapper });
    const grid = screen.getByTestId("dashboard-modules");
    const links = grid.querySelectorAll("a[href]");
    expect(links.length).toBeGreaterThan(0);
    for (const link of Array.from(links)) {
      expect(link.getAttribute("href")).toMatch(/^\/dashboard/);
      // Native anchors are in the tab order unless tabindex="-1".
      expect(link.getAttribute("tabindex")).not.toBe("-1");
    }
  });
});
