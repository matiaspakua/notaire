/**
 * LoadingState / ErrorState / DataTable error branch (#1947)
 */
import { describe, it, expect, vi } from "vitest";
import { render, screen } from "@testing-library/react";
import { NextIntlClientProvider } from "next-intl";
import { LoadingState } from "@/components/shared/LoadingState";
import { ErrorState } from "@/components/shared/ErrorState";
import { DataTable } from "@/components/shared/DataTable";

const messages = {
  common: { loading: "Cargando...", noData: "Sin datos", retry: "Reintentar" },
};

function withIntl(ui: React.ReactNode) {
  return (
    <NextIntlClientProvider locale="es" messages={messages}>
      {ui}
    </NextIntlClientProvider>
  );
}

describe("LoadingState (#1947)", () => {
  it("is busy and exposes a loading label", () => {
    render(withIntl(<LoadingState />));
    const panel = screen.getByTestId("loading-state");
    expect(panel).toHaveAttribute("aria-busy", "true");
    expect(screen.getByText("Cargando...")).toBeInTheDocument();
  });
});

describe("ErrorState (#1947)", () => {
  it("shows title and retry action", () => {
    render(
      withIntl(
        <ErrorState
          title="No se pudo cargar"
          description="Intenta de nuevo."
          action={<button type="button">Reintentar</button>}
        />,
      ),
    );
    expect(screen.getByText("No se pudo cargar")).toBeInTheDocument();
    expect(screen.getByRole("button", { name: "Reintentar" })).toBeInTheDocument();
  });
});

describe("DataTable error branch (#1947)", () => {
  it("renders ErrorState instead of empty data", () => {
    const onRetry = vi.fn();
    render(
      withIntl(
        <DataTable
          data={[]}
          columns={[{ key: "id", header: "ID", render: () => null }]}
          keyExtractor={() => 1}
          error="No se pudo cargar la lista"
          onRetry={onRetry}
        />,
      ),
    );
    expect(screen.getByTestId("error-state")).toBeInTheDocument();
    expect(screen.getByText("No se pudo cargar la lista")).toBeInTheDocument();
    expect(screen.getByRole("button", { name: "Reintentar" })).toBeInTheDocument();
  });
});
