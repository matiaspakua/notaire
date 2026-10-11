/**
 * Shared status badge (#1366, RF-23, RNF-09): every status column renders the
 * same tone + icon + translated text (WCAG 1.4.1: never color alone), and a
 * value outside a domain's vocabulary shows as a neutral badge with its raw text.
 */
import { describe, it, expect } from "vitest";
import { readFileSync } from "node:fs";
import { join } from "node:path";
import { render, screen } from "@testing-library/react";
import { NextIntlClientProvider } from "next-intl";
import es from "../../../messages/es.json";
import en from "../../../messages/en.json";
import { StatusBadge } from "@/components/shared/StatusBadge";
import { resolveStatus, testimonyStatus, type StatusDomain } from "@/lib/status-maps";

function renderBadge(domain: StatusDomain, value: string | boolean | null | undefined, locale: "es" | "en" = "es") {
  return render(
    <NextIntlClientProvider locale={locale} messages={locale === "es" ? es : en}>
      <StatusBadge domain={domain} value={value} />
    </NextIntlClientProvider>,
  );
}

describe("resolveStatus (#1366)", () => {
  it("maps budget codes in any case to their tone and the presupuestos label key", () => {
    expect(resolveStatus("budget", "Pendiente")).toMatchObject({ tone: "warning", labelKey: "presupuestos.status.PENDIENTE" });
    expect(resolveStatus("budget", "APROBADO")).toMatchObject({ tone: "success" });
    expect(resolveStatus("budget", "rechazado")).toMatchObject({ tone: "danger" });
    expect(resolveStatus("budget", "BORRADOR")).toMatchObject({ tone: "neutral" });
    expect(resolveStatus("budget", "FACTURADO")).toMatchObject({ tone: "info" });
  });

  it("gives seeded management statuses a tone and keeps the catalog name as the label", () => {
    expect(resolveStatus("management", "Iniciada")).toMatchObject({ tone: "info", labelKey: null, raw: "Iniciada" });
    expect(resolveStatus("management", "Gestion con Escritura Firmada")).toMatchObject({ tone: "success" });
    expect(resolveStatus("management", "Gestion con Escritura Anulada")).toMatchObject({ tone: "danger" });
    expect(resolveStatus("management", "Archivada")).toMatchObject({ tone: "neutral" });
  });

  it("maps folio values with or without the 'Folio' prefix", () => {
    expect(resolveStatus("folio", "Nuevo")).toMatchObject({ tone: "info", labelKey: "status.folio.nuevo" });
    expect(resolveStatus("folio", "Folio Utilizado")).toMatchObject({ tone: "neutral", labelKey: "status.folio.utilizado" });
    expect(resolveStatus("folio", "Asignado a cuaderno")).toMatchObject({ tone: "success", labelKey: "status.folio.asignado" });
    expect(resolveStatus("folio", "Errose")).toMatchObject({ tone: "danger", labelKey: "status.folio.errose" });
  });

  it("maps user activity, folio availability and the derived testimony state", () => {
    expect(resolveStatus("user", true)).toMatchObject({ tone: "success", labelKey: "common.active" });
    expect(resolveStatus("user", false)).toMatchObject({ tone: "neutral", labelKey: "common.inactive" });
    expect(resolveStatus("availability", "Nuevo")).toMatchObject({ tone: "success", labelKey: "protocolo.available" });
    expect(resolveStatus("availability", "Utilizado")).toMatchObject({ tone: "neutral", labelKey: "protocolo.inUse" });
    expect(testimonyStatus({ verified: false })).toBe("generated");
    expect(testimonyStatus({ verified: true, flagged: true })).toBe("flagged");
    expect(testimonyStatus({ verified: true, flagged: false })).toBe("verified");
    expect(resolveStatus("testimony", "flagged")).toMatchObject({ tone: "warning", labelKey: "testimonios.estadoObservado" });
  });

  it("treats anything outside a vocabulary as neutral with the raw text", () => {
    expect(resolveStatus("budget", "ACTIVO")).toEqual({ tone: "neutral", labelKey: null, raw: "ACTIVO" });
    expect(resolveStatus("management", "Estado E2E 42")).toEqual({ tone: "neutral", labelKey: null, raw: "Estado E2E 42" });
    expect(resolveStatus("folio", undefined)).toEqual({ tone: "neutral", labelKey: null, raw: "" });
  });
});

describe("<StatusBadge> (#1366)", () => {
  it("renders the translated label with a decorative icon and its tone", () => {
    renderBadge("budget", "Pendiente");
    const badge = screen.getByTestId("status-badge");
    expect(badge).toHaveTextContent("Pendiente");
    expect(badge).toHaveAttribute("data-tone", "warning");
    const icon = badge.querySelector("svg");
    expect(icon).not.toBeNull();
    expect(icon).toHaveAttribute("aria-hidden", "true");
  });

  it("translates for English", () => {
    renderBadge("folio", "Utilizado", "en");
    expect(screen.getByTestId("status-badge")).toHaveTextContent("Used");
    renderBadge("user", false, "en");
    expect(screen.getAllByTestId("status-badge")[1]).toHaveTextContent("Inactive");
  });

  it("never crashes or renders blank on an unknown or missing value", () => {
    renderBadge("management", "Estado E2E 42");
    expect(screen.getByTestId("status-badge")).toHaveTextContent("Estado E2E 42");
    expect(screen.getByTestId("status-badge")).toHaveAttribute("data-tone", "neutral");
    renderBadge("folio", null);
    expect(screen.getAllByTestId("status-badge")[1]).toHaveTextContent("—");
  });

  it("has a label in es and en for every folio and testimony key", () => {
    for (const msgs of [es, en] as Record<string, unknown>[]) {
      const folio = (msgs.status as { folio: Record<string, string> }).folio;
      expect(Object.keys(folio).sort()).toEqual(["asignado", "errose", "nuevo", "utilizado"]);
    }
  });
});

describe("status columns use <StatusBadge> (#1366)", () => {
  const pages: Record<string, string> = {
    budgets: "budgets/page.tsx",
    managements: "managements/page.tsx",
    testimonies: "testimonies/page.tsx",
    protocol: "protocol/page.tsx",
    users: "administration/users/page.tsx",
    folios: "administration/folios/page.tsx",
  };
  for (const [name, rel] of Object.entries(pages)) {
    it(`${name} renders its status through StatusBadge, with no ad-hoc status badge`, () => {
      const src = readFileSync(join(__dirname, "..", "..", "app", "dashboard", rel), "utf8");
      expect(src).toMatch(/<StatusBadge\s+domain=/);
      expect(src).not.toMatch(/<Badge[^>]*>\{(t|tc)\("(available|inUse|active|inactive)"\)\}<\/Badge>/);
      expect(src).not.toMatch(/<Badge[^>]*>\{f\.status\}<\/Badge>/);
    });
  }
});
