/**
 * #1367 — AppSidebar renders collapsible task-based sections; the section with
 * the current route is always expanded; collapsed sections persist.
 */
import { describe, it, expect, vi, beforeEach } from "vitest";
import { render, screen, fireEvent, within } from "@testing-library/react";

const mockPathname = vi.fn(() => "/dashboard/copias");
let admin = true;

vi.mock("next/navigation", () => ({
  usePathname: () => mockPathname(),
  useRouter: () => ({ replace: vi.fn(), push: vi.fn() }),
}));
vi.mock("next-intl", () => ({
  useTranslations: (ns: string) => (key: string) => `${ns}.${key}`,
  useLocale: () => "es",
}));
vi.mock("@/lib/api-client", () => ({ apiLogout: vi.fn() }));
vi.mock("@/components/shared/LanguageSwitcher", () => ({ LanguageSwitcher: () => null }));
vi.mock("@/store/auth-store", () => ({
  useAuthStore: () => ({ user: { nombre: "Ana", tipo: "ESCRIBANO" }, logout: vi.fn(), isAdmin: () => admin }),
}));

import { AppSidebar, SIDEBAR_COLLAPSED_KEY } from "@/components/layout/AppSidebar";

function desktop() {
  return within(screen.getAllByTestId("sidebar")[0]);
}

beforeEach(() => {
  admin = true;
  mockPathname.mockReturnValue("/dashboard/copias");
  window.localStorage.removeItem(SIDEBAR_COLLAPSED_KEY);
});

describe("AppSidebar sections (#1367)", () => {
  it("renders six labelled sections for an admin, five without Administración otherwise", () => {
    const { unmount } = render(<AppSidebar />);
    expect(desktop().getAllByRole("heading", { level: 2 })).toHaveLength(6);
    unmount();

    admin = false;
    render(<AppSidebar />);
    const headings = desktop().getAllByRole("heading", { level: 2 }).map((h) => h.textContent);
    expect(headings).toHaveLength(5);
    expect(headings).not.toContain("navGroups.administracion");
    expect(desktop().queryByTestId("nav-auditoria")).toBeNull();
  });

  it("keeps Inicio outside the sections", () => {
    render(<AppSidebar />);
    const home = desktop().getByTestId("nav-home");
    expect(home.closest("[data-nav-group]")).toBeNull();
  });

  it("expands the section holding the current route and marks the link current", () => {
    render(<AppSidebar />);
    const toggle = desktop().getByRole("button", { name: "navGroups.escriturasProtocolo" });
    expect(toggle).toHaveAttribute("aria-expanded", "true");
    expect(desktop().getByTestId("nav-copias")).toHaveAttribute("aria-current", "page");
  });

  it("collapses a section on click and persists it", () => {
    render(<AppSidebar />);
    const toggle = desktop().getByRole("button", { name: "navGroups.clientes" });
    expect(toggle).toHaveAttribute("aria-expanded", "true");
    expect(desktop().getByTestId("nav-personas")).toBeVisible();

    fireEvent.click(toggle);
    expect(toggle).toHaveAttribute("aria-expanded", "false");
    expect(desktop().queryByTestId("nav-personas")).toBeNull();
    expect(JSON.parse(window.localStorage.getItem(SIDEBAR_COLLAPSED_KEY) ?? "[]")).toContain("clientes");
  });

  it("restores collapsed sections, but never hides the current route's section", () => {
    window.localStorage.setItem(SIDEBAR_COLLAPSED_KEY, JSON.stringify(["clientes", "escriturasProtocolo"]));
    render(<AppSidebar />);
    expect(desktop().getByRole("button", { name: "navGroups.clientes" })).toHaveAttribute("aria-expanded", "false");
    expect(desktop().getByRole("button", { name: "navGroups.escriturasProtocolo" })).toHaveAttribute("aria-expanded", "true");
    expect(desktop().getByTestId("nav-copias")).toBeVisible();
  });
});
