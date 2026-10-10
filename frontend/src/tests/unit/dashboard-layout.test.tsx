/**
 * #1352 — keyboard bypass (WCAG 2.4.1) and focus on route change (WCAG 2.4.3).
 * The dashboard layout starts with a "skip to content" link targeting
 * <main id="main-content" tabIndex={-1}>, and after a client-side navigation
 * focus moves to the new page's <h1> instead of staying on the nav link.
 */
import { describe, it, expect, vi, beforeEach } from "vitest";
import { render, screen, fireEvent, waitFor } from "@testing-library/react";
import es from "../../../messages/es.json";
import en from "../../../messages/en.json";

const mockPathname = vi.fn(() => "/dashboard/personas");

vi.mock("next/navigation", () => ({
  usePathname: () => mockPathname(),
  useRouter: () => ({ replace: vi.fn(), push: vi.fn() }),
}));

vi.mock("next-intl", () => ({
  useTranslations: (ns: string) => (key: string) => `${ns}.${key}`,
  useLocale: () => "es",
}));

vi.mock("@/components/layout/AppSidebar", () => ({
  MOBILE_SIDEBAR_ID: "mobile-sidebar",
  AppSidebar: () => (
    <nav aria-label="sidebar">
      <a href="/dashboard/gestiones">Gestiones</a>
    </nav>
  ),
}));

vi.mock("@/components/layout/Breadcrumb", () => ({ Breadcrumb: () => null }));

vi.mock("@/store/auth-store", () => ({
  useAuthStore: () => ({ isAuthenticated: true }),
}));

import DashboardLayout from "@/app/dashboard/layout";

function Page({ title }: { title: string }) {
  return (
    <header>
      <h1>{title}</h1>
    </header>
  );
}

beforeEach(() => {
  mockPathname.mockReturnValue("/dashboard/personas");
  // jsdom has no matchMedia; the layout listens for the desktop breakpoint.
  window.matchMedia = vi.fn().mockImplementation((query: string) => ({
    matches: false,
    media: query,
    addEventListener: vi.fn(),
    removeEventListener: vi.fn(),
  }));
});

describe("DashboardLayout skip link (#1352)", () => {
  it("renders the skip link as the first focusable element, targeting main", async () => {
    render(
      <DashboardLayout>
        <Page title="Personas" />
      </DashboardLayout>,
    );
    const skip = await screen.findByTestId("skip-link");
    expect(skip.tagName).toBe("A");
    expect(skip).toHaveAttribute("href", "#main-content");
    expect(skip).toHaveTextContent("navigation.skipToContent");

    const focusables = document.querySelectorAll<HTMLElement>("a[href], button");
    expect(focusables[0]).toBe(skip);

    const main = screen.getByRole("main");
    expect(main).toHaveAttribute("id", "main-content");
    expect(main).toHaveAttribute("tabindex", "-1");
  });

  it("moves focus to main when the skip link is activated", async () => {
    render(
      <DashboardLayout>
        <Page title="Personas" />
      </DashboardLayout>,
    );
    fireEvent.click(await screen.findByTestId("skip-link"));
    expect(document.activeElement).toBe(screen.getByRole("main"));
  });

  it("has the skipToContent label in both catalogs", () => {
    expect((es.navigation as Record<string, unknown>).skipToContent).toBe("Saltar al contenido");
    expect((en.navigation as Record<string, unknown>).skipToContent).toBe("Skip to content");
  });
});

describe("DashboardLayout route focus (#1352)", () => {
  it("does not move focus on the first load", async () => {
    render(
      <DashboardLayout>
        <Page title="Personas" />
      </DashboardLayout>,
    );
    await screen.findByRole("heading", { name: "Personas" });
    await new Promise((r) => setTimeout(r, 100));
    expect(document.activeElement).toBe(document.body);
  });

  it("focuses the new page h1 after a client-side navigation", async () => {
    const { rerender } = render(
      <DashboardLayout>
        <Page title="Personas" />
      </DashboardLayout>,
    );
    await screen.findByRole("heading", { name: "Personas" });
    screen.getByRole("link", { name: "Gestiones" }).focus();

    mockPathname.mockReturnValue("/dashboard/gestiones");
    rerender(
      <DashboardLayout>
        <Page title="Gestiones" />
      </DashboardLayout>,
    );

    await waitFor(() =>
      expect(document.activeElement).toBe(screen.getByRole("heading", { name: "Gestiones" })),
    );
    expect(document.activeElement).toHaveAttribute("tabindex", "-1");
  });
});
