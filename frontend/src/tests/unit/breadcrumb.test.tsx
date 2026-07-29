import { describe, it, expect, vi } from "vitest";
import { render, screen } from "@testing-library/react";

const mockUsePathname = vi.fn();

vi.mock("next/navigation", () => ({
  usePathname: () => mockUsePathname(),
}));

import { Breadcrumb } from "@/components/layout/Breadcrumb";

describe("Breadcrumb", () => {
  it("renders nothing at the root path", () => {
    mockUsePathname.mockReturnValue("/");
    const { container } = render(<Breadcrumb />);
    expect(container).toBeEmptyDOMElement();
  });

  it("renders nothing for a single top-level segment", () => {
    mockUsePathname.mockReturnValue("/dashboard");
    const { container } = render(<Breadcrumb />);
    expect(container).toBeEmptyDOMElement();
  });

  it("renders a crumb per path segment with known Spanish labels", () => {
    mockUsePathname.mockReturnValue("/dashboard/personas");
    render(<Breadcrumb />);

    expect(screen.getByTestId("breadcrumb")).toBeInTheDocument();
    expect(screen.getByText("Dashboard")).toBeInTheDocument();
    expect(screen.getByText("Personas")).toBeInTheDocument();
  });

  it("renders the last segment as plain text, not a link", () => {
    mockUsePathname.mockReturnValue("/dashboard/personas");
    render(<Breadcrumb />);

    const last = screen.getByText("Personas");
    expect(last.tagName).not.toBe("A");
  });

  it("renders intermediate segments as links to their partial path", () => {
    mockUsePathname.mockReturnValue("/dashboard/personas");
    render(<Breadcrumb />);

    const link = screen.getByText("Dashboard").closest("a");
    expect(link).toHaveAttribute("href", "/dashboard");
  });

  it("falls back to a humanized label for unknown segments", () => {
    mockUsePathname.mockReturnValue("/dashboard/some-unknown-segment");
    render(<Breadcrumb />);

    expect(screen.getByText("Some Unknown Segment")).toBeInTheDocument();
  });
});
