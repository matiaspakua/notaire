/**
 * Page chrome primitives (#1942 / RNF-09)
 */
import { describe, it, expect } from "vitest";
import { render, screen } from "@testing-library/react";
import { PageHeader } from "@/components/shared/PageHeader";
import { EmptyState } from "@/components/shared/EmptyState";
import { PageSection } from "@/components/shared/PageSection";

describe("PageHeader (#1942)", () => {
  it("renders title and actions", () => {
    render(
      <PageHeader
        title="Personas"
        description="Clientes y comparecientes"
        actions={<button type="button">Nueva</button>}
      />,
    );
    expect(screen.getByRole("heading", { name: "Personas" })).toBeInTheDocument();
    expect(screen.getByText("Clientes y comparecientes")).toBeInTheDocument();
    expect(screen.getByRole("button", { name: "Nueva" })).toBeInTheDocument();
  });
});

describe("EmptyState (#1942)", () => {
  it("shows title and action when provided", () => {
    render(
      <EmptyState
        title="Sin personas"
        description="Crea la primera ficha para empezar."
        action={<button type="button">Nueva persona</button>}
      />,
    );
    expect(screen.getByText("Sin personas")).toBeInTheDocument();
    expect(screen.getByText("Crea la primera ficha para empezar.")).toBeInTheDocument();
    expect(screen.getByRole("button", { name: "Nueva persona" })).toBeInTheDocument();
  });
});

describe("PageSection (#1942)", () => {
  it("renders title and children", () => {
    render(
      <PageSection title="Módulos disponibles">
        <p>Contenido de sección</p>
      </PageSection>,
    );
    expect(screen.getByRole("heading", { name: "Módulos disponibles" })).toBeInTheDocument();
    expect(screen.getByText("Contenido de sección")).toBeInTheDocument();
  });
});
