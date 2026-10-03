/**
 * Static Gate 2 checks for #1066 / CU76 — E2E reliability rules.
 * These assert on source/config so false-green skips and sleep/retry budgets fail CI.
 */
import { readFileSync, readdirSync } from "node:fs";
import { join } from "node:path";
import { describe, expect, it } from "vitest";

const frontendRoot = join(__dirname, "../../..");
const e2eDir = join(frontendRoot, "tests/e2e");

function readE2E(relativePath: string): string {
  return readFileSync(join(e2eDir, relativePath), "utf8");
}

function readConfig(): string {
  return readFileSync(join(frontendRoot, "playwright.config.ts"), "utf8");
}

describe("E2E reliability (#1066 / CU76)", () => {
  it("configures CI Playwright retries to at most 1", () => {
    const config = readConfig();
    expect(config).toMatch(/retries:\s*process\.env\.CI\s*\?\s*1\s*:\s*0/);
    expect(config).not.toMatch(/retries:\s*process\.env\.CI\s*\?\s*[2-9]/);
  });

  it("retains flake triage artifacts on retry/failure", () => {
    const config = readConfig();
    expect(config).toMatch(/trace:\s*["']on-first-retry["']/);
    expect(config).toMatch(/screenshot:\s*["']only-on-failure["']/);
    expect(config).toMatch(/video:\s*["']retain-on-failure["']/);
  });

  it("does not use waitForTimeout in #1066 hotspot QA specs", () => {
    for (const file of [
      "TS-0040-l10n-language-switching-qa.spec.ts",
      "TS-0043-icons-ux-qa.spec.ts",
      "TS-0021-workflow-editor-admin.spec.ts",
      "TS-0022-workflow-assignment-admin.spec.ts",
    ]) {
      const source = readE2E(file);
      expect(source, `${file} must not use waitForTimeout`).not.toMatch(/waitForTimeout\s*\(/);
    }
  });

  it("gates demo paced pauses behind HEADED or SLOW_MO", () => {
    for (const file of [
      "TS-0071-first-case-tutorial-onboarding.spec.ts",
      "TS-0090-demo-two-full-cases.spec.ts",
    ]) {
      const source = readE2E(file);
      if (!source.includes("waitForTimeout")) {
        continue;
      }
      expect(
        source,
        `${file} must gate waitForTimeout behind HEADED/SLOW_MO`,
      ).toMatch(/HEADED|SLOW_MO/);
    }
  });

  it("TS-0021/TS-0022 arrange data and do not skip on empty tables", () => {
    const editor = readE2E("TS-0021-workflow-editor-admin.spec.ts");
    const assignment = readE2E("TS-0022-workflow-assignment-admin.spec.ts");

    expect(editor).toMatch(/createWorkflowDefinition/);
    expect(editor).not.toMatch(/test\.skip\s*\(\s*\)/);

    expect(assignment).toMatch(/createTipoTramite/);
    expect(assignment).not.toMatch(/test\.skip\s*\(\s*\)/);
  });

  it("TS-0021 validate assert uses validation-errors only (no strict-unsafe toast.or union)", () => {
    const editor = readE2E("TS-0021-workflow-editor-admin.spec.ts");
    expect(editor).toMatch(/getByTestId\(\s*["']validation-errors["']\s*\)/);
    // Ban locator("[data-sonner-toast]").or(...validation-errors...) style unions
    expect(editor).not.toMatch(/locator\(\s*["']\[data-sonner-toast\]["']\s*\)\s*\.or\(/);
    expect(editor).not.toMatch(/\.or\(\s*page\.getByTestId\(\s*["']validation-errors["']/);
  });

  it("intentional feature-gap skips in TS-0014/16/17/20 cite an open #issue", () => {
    // Live inventory for #1146 / CU76 (CU21 already unskipped in #1057 — not counted).
    const expectedCounts: Record<string, number> = {
      "TS-0014-pagos-workflow.spec.ts": 2,
      "TS-0016-usuarios-escribanos-workflow.spec.ts": 3,
      "TS-0017-suplencias-workflow.spec.ts": 2,
      "TS-0020-reportes-admin-workflow.spec.ts": 7,
    };

    let total = 0;
    for (const [file, expected] of Object.entries(expectedCounts)) {
      const source = readE2E(file);
      const skipBlocks = source.match(/test\.skip\s*\(\s*["'`][\s\S]*?["'`]\s*,/g) ?? [];
      expect(skipBlocks.length, `${file} intentional skip count`).toBe(expected);
      total += skipBlocks.length;

      for (const block of skipBlocks) {
        expect(block, `${file} skip must cite #issue`).toMatch(/#\d+/);
      }

      // CU21 edit was unskipped in #1057 — must not reappear as a feature-gap skip.
      if (file.includes("TS-0016")) {
        expect(source).not.toMatch(/test\.skip\s*\(\s*["'`][^"'`]*CU21[^"'`]*["'`]/);
      }
    }

    expect(total, "TS-0014/16/17/20 feature-gap skip inventory (#1146)").toBe(14);
  });

  it("keeps default timeout below the historic 300s hang mask (demo projects may override)", () => {
    const config = readConfig();
    const match = config.match(/^\s*timeout:\s*(\d+)/m);
    expect(match).not.toBeNull();
    const timeout = Number(match![1]);
    expect(timeout).toBeLessThanOrEqual(120_000);
  });
});

describe("E2E reliability inventory sanity", () => {
  it("hotspot files exist under tests/e2e", () => {
    const names = new Set(readdirSync(e2eDir));
    for (const required of [
      "TS-0021-workflow-editor-admin.spec.ts",
      "TS-0022-workflow-assignment-admin.spec.ts",
      "TS-0040-l10n-language-switching-qa.spec.ts",
      "TS-0043-icons-ux-qa.spec.ts",
      "playwright.config.ts",
    ]) {
      if (required === "playwright.config.ts") {
        continue;
      }
      expect(names.has(required), required).toBe(true);
    }
  });
});
