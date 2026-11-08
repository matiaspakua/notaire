/**
 * Delivery outcomes and About (#1985): the site prints only what the snapshot and about.json
 * hold, says when and how numbers were taken, and never renders a placeholder as a fact.
 */
import { render, screen, within } from "@testing-library/react";
import { describe, expect, it } from "vitest";
import { OutcomesSection } from "@/components/Outcomes";
import { AboutFacts } from "@/components/About";
import type { DeliveryMetrics } from "@/lib/outcomes";
import about from "@/lib/about.json";

const SNAPSHOT = {
  generated_at: "2026-11-01T10:55:24+00:00",
  generated_by: "python3 workspace/ci/generate-delivery-metrics.py",
  repository: "matiaspakua/notaire",
  window: { from: "2026-08-03", to: "2026-11-01", days: 90 },
  method: {
    lead_time: "LEAD-METHOD",
    throughput: "THROUGHPUT-METHOD",
    change_failure: "CFR-METHOD",
    restore_main: "RESTORE-METHOD",
    first_push_ci: "FIRST-PUSH-METHOD",
    issues_closed: "ISSUES-METHOD",
    releases: "RELEASES-METHOD",
  },
  lead_time_hours: { n: 584, median: 0.5, p90: 5.5 },
  throughput: { merged_prs: 584, bot_prs_excluded: 87, weeks: 13, median_per_week: 14, series: [] },
  change_failure: { failure_prs: 1, merged_prs: 584, rate_pct: 0.2, examples: ["#1204"] },
  restore_main_hours: { available: false, reason: "RESTORE-REASON" },
  first_push_ci: { available: true, prs_with_ci: 10, judged: 8, passed: 6, rate_pct: 75 },
  issues_closed: 598,
  releases: { count: 9, tags: [{ tag: "v0.2.5", published: "2026-10-28" }] },
} satisfies DeliveryMetrics;

describe("OutcomesSection", () => {
  it("prints the snapshot values with the window", () => {
    render(<OutcomesSection data={SNAPSHOT} />);
    const section = screen.getByRole("region", { name: /delivery outcomes/i });
    const text = section.textContent ?? "";
    expect(text).toContain("2026-08-03");
    expect(text).toContain("2026-11-01");
    expect(text).toContain("0.5 h");
    expect(text).toContain("5.5 h");
    expect(text).toContain("14");
    expect(text).toContain("0.2%");
    expect(text).toContain("598");
    expect(text).toContain("75%");
    expect(text).toContain("9");
  });

  it("shows how each value is computed", () => {
    render(<OutcomesSection data={SNAPSHOT} />);
    for (const m of Object.values(SNAPSHOT.method)) expect(screen.getByText(m, { exact: false })).toBeInTheDocument();
  });

  it("prints 'not available' and the reason for a metric the data cannot support", () => {
    render(<OutcomesSection data={SNAPSHOT} />);
    const restore = screen.getByRole("group", { name: /time to restore main/i });
    expect(within(restore).getByText(/not available/i)).toBeInTheDocument();
    expect(within(restore).getByText(/RESTORE-REASON/)).toBeInTheDocument();
    expect(restore.textContent).not.toMatch(/\d+(\.\d+)? h/);
  });
});

describe("About", () => {
  it("names a source for every fact in about.json", () => {
    for (const f of about.facts) {
      expect(f.value, f.label).toBeTruthy();
      expect(f.source.label, f.label).toBeTruthy();
      expect(f.source.href, f.label).toMatch(/^https:\/\//);
    }
  });

  it("keeps placeholders out of the page", () => {
    const { container } = render(<AboutFacts data={about} />);
    expect(about.placeholders.length).toBeGreaterThan(0);
    for (const p of about.placeholders) expect(container.textContent).not.toContain(p.todo);
    expect(container.textContent).not.toMatch(/TODO|PLACEHOLDER/i);
  });

  it("links every fact to its source", () => {
    render(<AboutFacts data={about} />);
    for (const f of about.facts) {
      expect(screen.getByText(f.value)).toBeInTheDocument();
      expect(screen.getAllByRole("link", { name: new RegExp(f.source.label.replace(/[.*+?^${}()|[\]\\]/g, "\\$&")) }).length).toBeGreaterThan(0);
    }
  });
});
