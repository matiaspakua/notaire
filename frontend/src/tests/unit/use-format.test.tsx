/**
 * Dates, numbers and currency follow the active locale (#1355, RNF-05, ADR-015).
 * es → es-AR; en → en-GB (Owner decision: day first for an Argentine notary
 * office). Currency stays ARS in both.
 */
import { describe, it, expect } from "vitest";
import { render, screen } from "@testing-library/react";
import { NextIntlClientProvider } from "next-intl";
import { readFileSync, readdirSync, statSync } from "node:fs";
import { join } from "node:path";
import { useFormat } from "@/hooks/useFormat";

function Probe() {
  const f = useFormat();
  return (
    <ul>
      <li data-testid="date">{f.date("2026-07-08")}</li>
      <li data-testid="empty">{f.date(null)}</li>
      <li data-testid="currency">{f.currency(75000)}</li>
      <li data-testid="number">{f.number(1234567)}</li>
      <li data-testid="instant">{f.instant("2026-07-08T15:04:00Z")}</li>
    </ul>
  );
}

const text = (id: string) => (screen.getByTestId(id).textContent ?? "").replace(/\s/g, " ");

function renderIn(locale: "es" | "en") {
  render(
    <NextIntlClientProvider locale={locale} messages={{}}>
      <Probe />
    </NextIntlClientProvider>,
  );
}

describe("useFormat (#1355)", () => {
  it("formats day-first dates, ARS and numbers in es-AR", () => {
    renderIn("es");
    expect(text("date")).toBe("8/7/2026");
    expect(text("empty")).toBe("—");
    expect(text("currency")).toBe("$ 75.000,00");
    expect(text("number")).toBe("1.234.567");
    expect(text("instant")).toBe("8/7/2026, 12:04");
  });

  it("formats day-first dates, ARS and numbers in English", () => {
    renderIn("en");
    expect(text("date")).toBe("08/07/2026");
    expect(text("currency")).toBe("ARS 75,000.00");
    expect(text("number")).toBe("1,234,567");
    expect(text("instant")).toBe("08/07/2026, 12:04");
  });
});

describe("no hardcoded locale in screens (#1355)", () => {
  function files(dir: string): string[] {
    return readdirSync(dir).flatMap((f) => {
      const p = join(dir, f);
      return statSync(p).isDirectory() ? files(p) : /\.tsx?$/.test(f) ? [p] : [];
    });
  }
  it("src/app and src/components never name es-AR, en-US or en-GB", () => {
    const root = join(__dirname, "..", "..");
    const offenders = [...files(join(root, "app")), ...files(join(root, "components"))].filter((f) =>
      /["'](es-AR|en-US|en-GB)["']/.test(readFileSync(f, "utf8")),
    );
    expect(offenders.map((f) => f.slice(root.length + 1))).toEqual([]);
  });
  it("screens format through useFormat, not the es-AR-only helpers", () => {
    const root = join(__dirname, "..", "..");
    const offenders = [...files(join(root, "app")), ...files(join(root, "components"))].filter((f) =>
      /\b(formatDate|formatCurrency|formatCalendarDate|formatInstant)\(/.test(readFileSync(f, "utf8")),
    );
    expect(offenders.map((f) => f.slice(root.length + 1))).toEqual([]);
  });
});
