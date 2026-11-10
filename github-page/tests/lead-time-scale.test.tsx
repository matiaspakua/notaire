import { render, screen } from "@testing-library/react";
import { describe, expect, it } from "vitest";
import { LeadTimeScale } from "@/components/LeadTimeScale";

describe("LeadTimeScale", () => {
  it("prints the snapshot median and p90 and does not pretend to be a histogram", () => {
    render(<LeadTimeScale n={691} median={0.4} p90={4.2} />);
    const text = screen.getByRole("figure").textContent ?? "";
    expect(text).toContain("0.4");
    expect(text).toContain("4.2");
    expect(text).toContain("691");
    expect(text).toMatch(/not a histogram/i);
  });

  it("says when the snapshot has no lead time", () => {
    render(<LeadTimeScale n={0} median={null} p90={null} />);
    expect(screen.getByText(/not measured/i)).toBeInTheDocument();
  });
});
