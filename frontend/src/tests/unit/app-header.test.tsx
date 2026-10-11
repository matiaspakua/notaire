import { describe, it, expect } from "vitest";
import { render, screen } from "@testing-library/react";

import { AppHeader } from "@/components/layout/AppHeader";

describe("AppHeader", () => {
  it("renders the title", () => {
    render(<AppHeader title="People" />);
    expect(screen.getByRole("heading", { name: "People" })).toBeInTheDocument();
  });

  it("renders the description when provided", () => {
    render(<AppHeader title="People" description="Client management" />);
    expect(screen.getByText("Client management")).toBeInTheDocument();
  });

  it("omits the description when not provided", () => {
    render(<AppHeader title="People" />);
    expect(screen.queryByText("Client management")).not.toBeInTheDocument();
  });

  it("renders action content when provided", () => {
    render(<AppHeader title="People" actions={<button>New person</button>} />);
    expect(screen.getByRole("button", { name: "New person" })).toBeInTheDocument();
  });

  it("renders as a header landmark", () => {
    render(<AppHeader title="People" />);
    expect(screen.getByRole("banner")).toBeInTheDocument();
  });
});
