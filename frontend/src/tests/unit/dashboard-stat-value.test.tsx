/**
 * Dashboard stat cards (#1358): a skeleton while the count loads (not a fake 0),
 * a dash when it failed, and the exact total formatted for the locale.
 */
import { describe, it, expect } from "vitest";
import { render, screen } from "@testing-library/react";
import { NextIntlClientProvider } from "next-intl";
import es from "../../../messages/es.json";
import en from "../../../messages/en.json";
import { StatValue } from "@/components/dashboard/StatValue";

function renderStat(props: Parameters<typeof StatValue>[0], locale: "es" | "en" = "es") {
  return render(
    <NextIntlClientProvider locale={locale} messages={locale === "es" ? es : en}>
      <StatValue {...props} />
    </NextIntlClientProvider>,
  );
}

describe("StatValue (#1358)", () => {
  it("shows a skeleton, not 0, while the count is loading", () => {
    const { container } = renderStat({ value: undefined, isLoading: true });
    expect(screen.queryByText("0")).toBeNull();
    expect(container.querySelector(".animate-pulse")).not.toBeNull();
    expect(screen.getByTestId("stat-value")).toHaveAttribute("aria-busy", "true");
    expect(screen.getByText("Cargando...")).toBeInTheDocument();
  });

  it("shows a dash when the count could not be loaded", () => {
    renderStat({ value: undefined, isLoading: false, isError: true });
    expect(screen.getByTestId("stat-value")).toHaveTextContent("—");
  });

  it("formats the exact total for the Spanish locale", () => {
    renderStat({ value: 1434, isLoading: false });
    expect(screen.getByTestId("stat-value")).toHaveTextContent("1.434");
    expect(screen.getByTestId("stat-value")).toHaveAttribute("aria-busy", "false");
  });

  it("formats the exact total for the English locale", () => {
    renderStat({ value: 1434, isLoading: false }, "en");
    expect(screen.getByTestId("stat-value")).toHaveTextContent("1,434");
  });

  it("shows a real zero once loaded", () => {
    renderStat({ value: 0, isLoading: false });
    expect(screen.getByTestId("stat-value")).toHaveTextContent("0");
  });
});
