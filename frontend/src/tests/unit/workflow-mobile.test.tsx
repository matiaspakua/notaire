/**
 * #1580: below md the editor opens in the list view and the graph is read-only; header actions
 * wrap instead of overflowing the screen.
 */
import { describe, it, expect } from "vitest";
import { render, screen } from "@testing-library/react";
import { resolveView, graphEditable } from "@/lib/workflow-view";
import { AppHeader } from "@/components/layout/AppHeader";

describe("workflow editor on a phone (#1580)", () => {
  it("defaults to the list view on a phone and the graph elsewhere", () => {
    expect(resolveView(null, true)).toBe("list");
    expect(resolveView(null, false)).toBe("graph");
  });

  it("keeps the user's explicit choice", () => {
    expect(resolveView("graph", true)).toBe("graph");
    expect(resolveView("list", false)).toBe("list");
  });

  it("makes the graph read-only on a phone", () => {
    expect(graphEditable(true, true)).toBe(false);
    expect(graphEditable(true, false)).toBe(true);
    expect(graphEditable(false, false)).toBe(false);
  });

  it("lets header actions wrap", () => {
    render(<AppHeader title="T" actions={<button>a</button>} />);
    const actions = screen.getByRole("button", { name: "a" }).parentElement!;
    expect(actions.className).toContain("flex-wrap");
    expect(actions.parentElement!.className).toContain("flex-wrap");
  });
});
