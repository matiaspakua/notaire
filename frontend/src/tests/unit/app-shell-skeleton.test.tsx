/**
 * #1359 — no blank first paint. Until the client has mounted and the stored
 * session is read, the dashboard layout rendered `null`, so every hard load
 * showed a white page and then the shell popped in. It now renders a static
 * app-shell skeleton (same sidebar width, header bar, content blocks), and a
 * shared Skeleton block that only pulses when the user allows motion.
 */
import { describe, it, expect, vi, beforeEach } from "vitest";
import { readFileSync, readdirSync, statSync } from "node:fs";
import { join } from "node:path";
import { render, screen } from "@testing-library/react";
import { renderToString } from "react-dom/server";

const auth = { isAuthenticated: true };
const replace = vi.fn();

vi.mock("next/navigation", () => ({
  usePathname: () => "/dashboard/people",
  useRouter: () => ({ replace, push: vi.fn() }),
}));

vi.mock("next-intl", () => ({
  useTranslations: (ns?: string) => (key: string) => (ns ? `${ns}.${key}` : key),
  useLocale: () => "es",
}));

vi.mock("@/components/layout/AppSidebar", () => ({
  MOBILE_SIDEBAR_ID: "mobile-sidebar",
  AppSidebar: () => <nav aria-label="sidebar" />,
}));

vi.mock("@/components/layout/Breadcrumb", () => ({ Breadcrumb: () => null }));

vi.mock("@/store/auth-store", () => ({
  useAuthStore: () => auth,
}));

import DashboardLayout from "@/app/dashboard/layout";
import { AppShellSkeleton } from "@/components/layout/AppShellSkeleton";
import { Skeleton } from "@/components/ui/skeleton";
import { DataTable } from "@/components/shared/DataTable";

beforeEach(() => {
  auth.isAuthenticated = true;
  replace.mockReset();
  window.matchMedia = vi.fn().mockImplementation((query: string) => ({
    matches: false,
    media: query,
    addEventListener: vi.fn(),
    removeEventListener: vi.fn(),
  }));
});

describe("dashboard first paint (#1359)", () => {
  it("server-renders the app-shell skeleton, not an empty page, before mount", () => {
    const html = renderToString(
      <DashboardLayout>
        <p>page content</p>
      </DashboardLayout>,
    );
    expect(html).toContain('data-testid="app-shell-skeleton"');
    expect(html).not.toContain("page content");
  });

  it("keeps the skeleton and redirects to /login when there is no session", () => {
    auth.isAuthenticated = false;
    render(
      <DashboardLayout>
        <p>page content</p>
      </DashboardLayout>,
    );
    expect(screen.getByTestId("app-shell-skeleton")).toBeInTheDocument();
    expect(screen.queryByText("page content")).toBeNull();
    expect(replace).toHaveBeenCalledWith("/login");
  });

  it("swaps the skeleton for the real shell once mounted with a session", async () => {
    render(
      <DashboardLayout>
        <p>page content</p>
      </DashboardLayout>,
    );
    expect(await screen.findByText("page content")).toBeInTheDocument();
    expect(screen.queryByTestId("app-shell-skeleton")).toBeNull();
  });
});

describe("<AppShellSkeleton> (#1359)", () => {
  it("is a busy, labelled region with a sidebar as wide as the real one", () => {
    render(<AppShellSkeleton />);
    const shell = screen.getByTestId("app-shell-skeleton");
    expect(shell).toHaveAttribute("aria-busy", "true");
    expect(shell).toHaveAttribute("aria-label", "common.loading");
    const sidebar = screen.getByTestId("app-shell-skeleton-sidebar");
    expect(sidebar.className).toMatch(/\bw-72\b/);
    expect(sidebar.className).toMatch(/\bhidden\b/);
    expect(sidebar.className).toMatch(/\bmd:flex\b/);
  });
});

describe("<Skeleton> (#1359)", () => {
  it("pulses only when motion is allowed, and is hidden from assistive technology", () => {
    render(<Skeleton data-testid="sk" className="h-4 w-10" />);
    const sk = screen.getByTestId("sk");
    expect(sk.className).toMatch(/motion-safe:animate-pulse/);
    expect(sk.className).not.toMatch(/(^|\s)animate-pulse/);
    expect(sk).toHaveAttribute("aria-hidden", "true");
    expect(sk).toHaveAttribute("data-slot", "skeleton");
    expect(sk.className).toMatch(/\bh-4\b/);
  });

  it("DataTable's loading rows are Skeleton blocks on a busy table", () => {
    const { container } = render(
      <DataTable data={[]} columns={[{ key: "a", header: "A", render: () => null }]} isLoading keyExtractor={() => 1} />,
    );
    expect(container.querySelectorAll('[data-slot="skeleton"]').length).toBe(5);
    expect(screen.getByRole("table")).toHaveAttribute("aria-busy", "true");
  });

  it("no component pulses outside the shared Skeleton", () => {
    const root = join(__dirname, "..", "..");
    const offenders: string[] = [];
    const walk = (dir: string) => {
      for (const name of readdirSync(dir)) {
        const p = join(dir, name);
        if (statSync(p).isDirectory()) {
          if (name !== "tests" && name !== "node_modules") walk(p);
        } else if (/\.tsx?$/.test(name) && !p.endsWith(join("ui", "skeleton.tsx"))) {
          if (/(^|[\s"'`])animate-pulse\b/.test(readFileSync(p, "utf8"))) offenders.push(p.slice(root.length + 1));
        }
      }
    };
    walk(root);
    expect(offenders).toEqual([]);
  });
});
