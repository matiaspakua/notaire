import { render, screen } from "@testing-library/react";
import { describe, expect, it } from "vitest";
import { AITools } from "@/components/AITools";
import { FunFacts } from "@/components/FunFacts";
import { tr } from "@/lib/i18n/es";
import "@/lib/i18n/strings";
import type { SiteMetrics } from "@/lib/metrics-core";

const metrics = {
  commits: 1350,
  pullRequests: 210,
  markdownFiles: 260,
} as SiteMetrics;

describe("product story stays honest", () => {
  it("does not draw a share of AI work the repository never measured", () => {
    render(<AITools />);
    expect(screen.queryByText(/80%/)).toBeNull();
    expect(screen.queryByText(/~80%/)).toBeNull();
    expect(screen.getByRole("heading", { name: /the ai tools that built it/i })).toBeInTheDocument();
  });

  it("says the legacy JPA package was retired", () => {
    render(<FunFacts metrics={metrics} java="21" />);
    const text = document.body.textContent ?? "";
    expect(text).not.toMatch(/still being migrated/i);
    expect(text).toMatch(/retired/i);
    expect(text).toMatch(/#576/);
  });

  it("translates the release-history lead", () => {
    const en =
      "One product version covers the backend, the frontend, and the Flyway schema recorded with that cut. The schema number is not the semver. Delivery trains ({train} for {version}) are milestones, not tags.";
    expect(tr("es", en)).toMatch(/versión/);
    expect(tr("es", en)).toContain("{train}");
  });
});
