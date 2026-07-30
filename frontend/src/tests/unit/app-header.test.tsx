import { describe, it, expect } from "vitest";
import { render, screen } from "@testing-library/react";

import { AppHeader } from "@/components/layout/AppHeader";

describe("AppHeader", () => {
  it("renders the title", () => {
    render(<AppHeader title="Personas" />);
    expect(screen.getByRole("heading", { name: "Personas" })).toBeInTheDocument();
  });

  it("renders the description when provided", () => {
    render(<AppHeader title="Personas" description="Gestión de clientes" />);
    expect(screen.getByText("Gestión de clientes")).toBeInTheDocument();
  });

  it("omits the description when not provided", () => {
    render(<AppHeader title="Personas" />);
    expect(screen.queryByText("Gestión de clientes")).not.toBeInTheDocument();
  });

  it("renders action content when provided", () => {
    render(<AppHeader title="Personas" actions={<button>Nueva persona</button>} />);
    expect(screen.getByRole("button", { name: "Nueva persona" })).toBeInTheDocument();
  });

  it("renders as a header landmark", () => {
    render(<AppHeader title="Personas" />);
    expect(screen.getByRole("banner")).toBeInTheDocument();
  });
});
