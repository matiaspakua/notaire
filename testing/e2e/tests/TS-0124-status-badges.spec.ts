/**
 * Playwright E2E — status columns render one shared StatusBadge (#1366, RF-23, RNF-09):
 * tone + decorative icon + text on budgets, cases, testimonies, protocol, users and folios.
 * Before, budgets/cases/testimonies showed plain text and folios the raw stored value.
 */
import { test, expect, type Page } from "@playwright/test";
import { establishAdminBrowserSession } from "./setup/auth";

async function expectBadgeOnEveryRow(page: Page, route: string) {
  await page.goto(route);
  await page.waitForLoadState("networkidle");
  // The first table is the list (folios also shows a folio-type table below it).
  const table = page.getByRole("table").first();
  const rows = table.locator("tbody tr");
  await expect(rows.first()).toBeVisible({ timeout: 15000 });
  const count = await rows.count();
  test.skip(count === 0, `no rows on ${route}`);
  const badges = table.getByTestId("status-badge");
  await expect(badges).toHaveCount(count);
  const first = badges.first();
  await expect(first).toHaveAttribute("data-tone", /^(neutral|info|success|warning|danger)$/);
  await expect(first.locator("svg")).toHaveAttribute("aria-hidden", "true");
  await expect(first).not.toHaveText("");
}

test.describe("Shared status badge (#1366)", () => {
  test.beforeEach(async ({ page }) => {
    await establishAdminBrowserSession(page);
  });

  for (const route of [
    "/dashboard/presupuestos",
    "/dashboard/gestiones",
    "/dashboard/testimonios",
    "/dashboard/administracion/usuarios",
    "/dashboard/administracion/folios",
  ]) {
    test(`${route}: every row's status is a badge with tone, icon and text`, async ({ page }) => {
      await expectBadgeOnEveryRow(page, route);
    });
  }

  test("budgets: a Pendiente budget is a warning badge labelled with the translated status", async ({ page }) => {
    await page.goto("/dashboard/presupuestos");
    await page.waitForLoadState("networkidle");
    const pending = page.getByRole("table").getByTestId("status-badge").filter({ hasText: /^Pendiente$/ });
    test.skip((await pending.count()) === 0, "no pending budget on the first page");
    await expect(pending.first()).toHaveAttribute("data-tone", "warning");
  });
});
