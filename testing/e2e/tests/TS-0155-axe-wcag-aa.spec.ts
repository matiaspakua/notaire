/**
 * TS-0155 — Automated WCAG 2.1 AA axe scans (#1251 / #1955)
 *
 * CU76 / RNF-09. Parametrised over login + featured dashboard routes.
 * Pre-existing findings live in tests/setup/axe-allowlist.json (issue-linked).
 */
import { test, expect } from "@playwright/test";
import { establishAdminBrowserSession } from "./setup/auth";
import { AXE_ROUTES, formatViolations, scanWcagAa } from "./setup/axe";

test.describe("TS-0155 - axe WCAG 2.1 AA (#1251)", () => {
  for (const route of AXE_ROUTES) {
    test(`axe ${route.name} (${route.path})`, async ({ page }) => {
      if (route.login === false) {
        await page.goto(route.path);
      } else {
        await establishAdminBrowserSession(page);
        await page.goto(route.path);
      }
      await page.waitForLoadState("networkidle");
      const violations = await scanWcagAa(page);
      expect(violations, formatViolations(violations)).toEqual([]);
    });
  }
});
