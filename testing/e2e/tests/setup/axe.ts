/**
 * Shared axe-core Playwright helpers (#1251 / #1955).
 */
import fs from "node:fs";
import path from "node:path";
import AxeBuilder from "@axe-core/playwright";
import type { Page } from "@playwright/test";
import type { Result } from "axe-core";

const here = path.join(process.cwd(), "tests", "setup");

export type AllowlistEntry = {
  ruleId: string;
  /** First CSS selector from the violation target (axe node target[0]). */
  target: string;
  issue: string;
  note?: string;
};

type AllowlistFile = {
  entries: AllowlistEntry[];
};

function loadAllowlist(): AllowlistEntry[] {
  const file = path.join(here, "axe-allowlist.json");
  const raw = JSON.parse(fs.readFileSync(file, "utf8")) as AllowlistFile;
  return raw.entries ?? [];
}

function fingerprint(ruleId: string, target: string): string {
  return `${ruleId}|${target}`;
}

export function filterNewViolations(violations: Result[]): Result[] {
  const allowed = new Set(
    loadAllowlist().map((e) => fingerprint(e.ruleId, e.target)),
  );
  return violations
    .map((v) => {
      const nodes = v.nodes.filter((n) => {
        const target = Array.isArray(n.target) ? String(n.target[0] ?? "") : "";
        return !allowed.has(fingerprint(v.id, target));
      });
      return { ...v, nodes };
    })
    .filter((v) => v.nodes.length > 0);
}

export async function scanWcagAa(page: Page): Promise<Result[]> {
  const results = await new AxeBuilder({ page })
    .withTags(["wcag2a", "wcag2aa"])
    .analyze();
  return filterNewViolations(results.violations);
}

export function formatViolations(violations: Result[]): string {
  return violations
    .map((v) => {
      const nodes = v.nodes
        .slice(0, 5)
        .map((n) => `  - ${n.target.join(" ")}`)
        .join("\n");
      return `${v.id} (${v.impact}): ${v.help}\n${nodes}`;
    })
    .join("\n\n");
}

/** Featured dashboard routes + login for the Wave 7 axe ratchet (#1955). */
export const AXE_ROUTES: { name: string; path: string; login?: boolean }[] = [
  { name: "login", path: "/login", login: false },
  { name: "dashboard", path: "/dashboard", login: true },
  { name: "people", path: "/dashboard/people", login: true },
  { name: "managements", path: "/dashboard/managements", login: true },
  { name: "budgets", path: "/dashboard/budgets", login: true },
  { name: "deeds", path: "/dashboard/deeds", login: true },
  { name: "reports", path: "/dashboard/reports", login: true },
  { name: "protocol", path: "/dashboard/protocol", login: true },
  { name: "items", path: "/dashboard/items", login: true },
  { name: "substitutions", path: "/dashboard/substitutions", login: true },
];
