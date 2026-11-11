/**
 * CheckboxField hit target meets WCAG 2.2 AA SC 2.5.8 (24 CSS px) — #2087.
 */
import { describe, it, expect } from "vitest";
import { render, screen } from "@testing-library/react";
import { CheckboxField } from "@/theme/form-patterns";

describe("CheckboxField touch target (#2087)", () => {
  it("renders a checkbox at least 24×24 CSS pixels", () => {
    render(<CheckboxField label="Is client" checked={false} onChange={() => undefined} data-testid="check" />);
    const box = screen.getByTestId("check");
    const style = box.getAttribute("style") ?? "";
    // Inline styles encode the contract; jsdom does not layout real pixels.
    // 28px declared → measured ≥24 CSS px in Chromium (native checkbox paint).
    expect(style).toMatch(/width:\s*28px/);
    expect(style).toMatch(/height:\s*28px/);
  });
});
