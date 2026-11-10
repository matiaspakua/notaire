import { render, screen, within } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { describe, expect, it } from "vitest";
import { PreferencesProvider } from "@/components/site/Preferences";
import { SiteHeader } from "@/components/site/SiteHeader";
import { setPathname } from "./setup";

describe("header language (#2201)", () => {
  it("shows the Spanish primary label after the visitor chooses Spanish", async () => {
    setPathname("/");
    localStorage.clear();
    render(
      <PreferencesProvider>
        <SiteHeader />
      </PreferencesProvider>,
    );
    const language = screen.getByRole("button", { name: /language|idioma/i });
    await userEvent.click(language);
    await userEvent.click(language);
    const header = screen.getByRole("banner");
    expect(within(header).getByRole("link", { name: "Documentación técnica" })).toHaveAttribute("href", "/docs/");
  });
});
