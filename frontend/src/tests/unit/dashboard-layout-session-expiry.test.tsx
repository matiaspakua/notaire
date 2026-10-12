/**
 * #1548: while a session-expiry redirect to /login?expired=1 is in flight, the
 * dashboard auth guard must not race it with its own plain router.replace("/login").
 * That race dropped the expired message (TS-0093 red on main).
 */
import { describe, it, expect, vi, beforeEach } from "vitest";
import { render, waitFor } from "@testing-library/react";

const replace = vi.fn();
const expiryInProgress = vi.fn(() => false);

vi.mock("next/navigation", () => ({
  usePathname: () => "/dashboard/managements",
  useRouter: () => ({ replace, push: vi.fn() }),
}));
vi.mock("next-intl", () => ({
  useTranslations: (ns: string) => (key: string) => `${ns}.${key}`,
  useLocale: () => "es",
}));
vi.mock("@/components/layout/AppSidebar", () => ({
  MOBILE_SIDEBAR_ID: "mobile-sidebar",
  AppSidebar: () => null,
}));
vi.mock("@/components/layout/Breadcrumb", () => ({ Breadcrumb: () => null }));
vi.mock("@/store/auth-store", () => ({
  useAuthStore: () => ({ isAuthenticated: false }),
}));
vi.mock("@/lib/session-expiry", () => ({
  isSessionExpiryInProgress: () => expiryInProgress(),
}));

import DashboardLayout from "@/app/dashboard/layout";

beforeEach(() => {
  replace.mockClear();
  window.matchMedia = vi.fn().mockImplementation((query: string) => ({
    matches: false,
    media: query,
    addEventListener: vi.fn(),
    removeEventListener: vi.fn(),
  }));
});

describe("DashboardLayout auth guard vs session expiry (#1548)", () => {
  it("redirects an unauthenticated user to /login", async () => {
    expiryInProgress.mockReturnValue(false);
    render(<DashboardLayout><p>x</p></DashboardLayout>);
    await waitFor(() => expect(replace).toHaveBeenCalledWith("/login"));
  });

  it("does not race an in-flight expired-session redirect", async () => {
    expiryInProgress.mockReturnValue(true);
    render(<DashboardLayout><p>x</p></DashboardLayout>);
    await new Promise((r) => setTimeout(r, 20));
    expect(replace).not.toHaveBeenCalled();
  });
});
