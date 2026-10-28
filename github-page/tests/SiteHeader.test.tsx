import { render, screen, within } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { describe, expect, it } from "vitest";
import { setPathname } from "./setup";
import { SiteHeader } from "@/components/site/SiteHeader";

describe("SiteHeader mobile menu (#1970)", () => {
  it("exposes a labelled menu button that reports its state", async () => {
    setPathname("/docs/testing/");
    render(<SiteHeader />);
    const button = screen.getByRole("button", { name: /open menu/i });
    expect(button).toHaveAttribute("aria-expanded", "false");
    expect(button).toHaveAttribute("aria-controls");
    await userEvent.click(button);
    expect(screen.getByRole("button", { name: /close menu/i, expanded: true })).toBeInTheDocument();
  });

  it("opens a modal drawer with every top-level entry and the current section's pages", async () => {
    setPathname("/ai-sdlc/lessons/");
    render(<SiteHeader />);
    await userEvent.click(screen.getByRole("button", { name: /open menu/i }));
    const drawer = screen.getByRole("dialog", { name: /site menu/i });
    expect(drawer).toHaveAttribute("aria-modal", "true");
    for (const label of ["AI Factory", "Product", "Technical docs", "Releases"]) {
      expect(within(drawer).getAllByRole("link", { name: new RegExp(label, "i") }).length).toBeGreaterThan(0);
    }
    expect(within(drawer).getByRole("link", { name: /^Lessons/ })).toHaveAttribute("aria-current", "page");
  });

  it("moves focus into the drawer and traps Tab inside it", async () => {
    setPathname("/");
    render(<SiteHeader />);
    await userEvent.click(screen.getByRole("button", { name: /open menu/i }));
    const drawer = screen.getByRole("dialog", { name: /site menu/i });
    expect(drawer.contains(document.activeElement)).toBe(true);
    for (let i = 0; i < 60; i += 1) {
      await userEvent.tab();
      expect(drawer.contains(document.activeElement)).toBe(true);
    }
    await userEvent.tab({ shift: true });
    expect(drawer.contains(document.activeElement)).toBe(true);
  });

  it("closes on Escape and gives focus back to the menu button", async () => {
    setPathname("/");
    render(<SiteHeader />);
    const button = screen.getByRole("button", { name: /open menu/i });
    await userEvent.click(button);
    await userEvent.keyboard("{Escape}");
    expect(screen.queryByRole("dialog", { name: /site menu/i })).not.toBeInTheDocument();
    expect(button).toHaveAttribute("aria-expanded", "false");
    expect(button).toHaveFocus();
  });

  it("puts Technical docs one click away in the desktop bar", () => {
    setPathname("/ai-sdlc/");
    render(<SiteHeader />);
    const bar = screen.getByRole("navigation", { name: /primary/i });
    expect(within(bar).getByRole("link", { name: "Technical docs" })).toHaveAttribute("href", "/docs/");
    expect(within(bar).getByRole("link", { name: "AI Factory" })).toHaveAttribute("aria-current", "true");
  });
});
