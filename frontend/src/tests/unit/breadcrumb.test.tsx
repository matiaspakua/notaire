/**
 * Breadcrumb labels come from messages/*.json (#1354): no Spanish map, no
 * slug title-casing. Every dashboard route segment has a label in both locales.
 */
import { readdirSync, statSync } from "node:fs";
import { join } from "node:path";
import { describe, it, expect, vi } from "vitest";
import { render, screen } from "@testing-library/react";
import { NextIntlClientProvider } from "next-intl";
import es from "../../../messages/es.json";
import en from "../../../messages/en.json";

const mockUsePathname = vi.fn();

vi.mock("next/navigation", () => ({
  usePathname: () => mockUsePathname(),
}));

import { Breadcrumb, breadcrumbKey } from "@/components/layout/Breadcrumb";

function renderAt(path: string, locale: "es" | "en" = "es") {
  mockUsePathname.mockReturnValue(path);
  return render(
    <NextIntlClientProvider locale={locale} messages={locale === "es" ? es : en}>
      <Breadcrumb />
    </NextIntlClientProvider>,
  );
}

function routeSegments(dir: string, parents: string[] = []): string[][] {
  return readdirSync(dir).flatMap((name) => {
    const path = join(dir, name);
    if (!statSync(path).isDirectory() || name.startsWith("[")) return [];
    const segs = [...parents, name];
    return [segs, ...routeSegments(path, segs)];
  });
}

describe("Breadcrumb", () => {
  it("renders nothing at the root path", () => {
    const { container } = renderAt("/");
    expect(container).toBeEmptyDOMElement();
  });

  it("renders nothing for a single top-level segment", () => {
    const { container } = renderAt("/dashboard");
    expect(container).toBeEmptyDOMElement();
  });

  it("renders a crumb per path segment with Spanish labels", () => {
    renderAt("/dashboard/personas");
    expect(screen.getByRole("navigation", { name: "Ruta de navegación" })).toBeInTheDocument();
    expect(screen.getByText("Inicio")).toBeInTheDocument();
    expect(screen.getByText("Personas")).toBeInTheDocument();
  });

  it("renders English labels in the en locale", () => {
    renderAt("/dashboard/movimientos-testimonio", "en");
    expect(screen.getByRole("navigation", { name: "Breadcrumb" })).toBeInTheDocument();
    expect(screen.getByText("Home")).toBeInTheDocument();
    expect(screen.getByText("Testimonio Movements")).toBeInTheDocument();
  });

  it("labels administration sub-routes by their module, in both locales", () => {
    renderAt("/dashboard/administracion/documentos", "en");
    expect(screen.getByText("Administration")).toBeInTheDocument();
    expect(screen.getByText("Document Types")).toBeInTheDocument();
  });

  it("keeps the accent on Próximos Vencimientos", () => {
    renderAt("/dashboard/proximos-vencimientos");
    expect(screen.getByText("Próximos Vencimientos")).toBeInTheDocument();
  });

  it("renders the last segment as plain text, not a link", () => {
    renderAt("/dashboard/personas");
    expect(screen.getByText("Personas").tagName).not.toBe("A");
  });

  it("renders intermediate segments as links to their partial path", () => {
    renderAt("/dashboard/personas");
    expect(screen.getByText("Inicio").closest("a")).toHaveAttribute("href", "/dashboard");
  });

  it("shows a record id segment as is", () => {
    renderAt("/dashboard/administracion/workflows/42", "en");
    expect(screen.getByText("Workflows")).toBeInTheDocument();
    expect(screen.getByText("42")).toBeInTheDocument();
  });

  it("has a label in both catalogs for every dashboard route segment", () => {
    const appDir = join(__dirname, "../../app");
    const missing: string[] = [];
    for (const segs of routeSegments(join(appDir, "dashboard"), ["dashboard"])) {
      const seg = segs[segs.length - 1];
      const key = breadcrumbKey(seg, segs[segs.length - 2]);
      for (const [locale, messages] of [["es", es], ["en", en]] as const) {
        const value = key
          .split(".")
          .reduce<unknown>((o, k) => (o as Record<string, unknown> | undefined)?.[k], messages);
        if (typeof value !== "string") missing.push(`${locale}: /${segs.join("/")} → ${key}`);
      }
    }
    expect(missing).toEqual([]);
  });
});
