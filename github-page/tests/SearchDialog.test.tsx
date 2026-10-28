import { render, screen, within } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { describe, expect, it } from "vitest";
import { setPathname } from "./setup";
import { SiteHeader } from "@/components/site/SiteHeader";

describe("site search (#1970)", () => {
  it("opens from the header, finds pages, and closes on Escape", async () => {
    setPathname("/");
    render(<SiteHeader />);
    const opener = screen.getAllByRole("button", { name: /search/i })[0];
    await userEvent.click(opener);
    const dialog = screen.getByRole("dialog", { name: /search the site/i });
    const input = within(dialog).getByRole("combobox");
    expect(input).toHaveFocus();
    await userEvent.type(input, "quality gates");
    const options = within(dialog).getAllByRole("option");
    expect(options[0]).toHaveTextContent("Quality gates");
    expect(within(options[0]).getByRole("link")).toHaveAttribute("href", "/ai-sdlc/quality-gates/");
    await userEvent.keyboard("{Escape}");
    expect(screen.queryByRole("dialog", { name: /search the site/i })).not.toBeInTheDocument();
  });

  it("opens with the / shortcut and says when nothing matches", async () => {
    setPathname("/docs/");
    render(<SiteHeader />);
    await userEvent.keyboard("/");
    const dialog = screen.getByRole("dialog", { name: /search the site/i });
    await userEvent.type(within(dialog).getByRole("combobox"), "zzzz");
    expect(within(dialog).getByText(/no page matches/i)).toBeInTheDocument();
  });
});
