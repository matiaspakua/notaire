/**
 * Playwright E2E — the managements list is paginated server-side (#1340 slice 4, CU02/CU19).
 * It requested size=1000, so managements after the 1000th were unreachable and the
 * page rendered 1000 rows (leaving it outlasted the TS-0070 tour's navigation timeout).
 */
import { test, expect } from "@playwright/test";
import { establishAdminBrowserSession } from "./setup/auth";
import { apiGet } from "./setup/api-helpers";

interface Page_ {
  content: { idManagement: number }[];
  totalElements: number;
}

test.describe("Managements list pagination (#1340)", () => {
  test("shows a page of 20, newest first, with the total and reaches the oldest management", async ({ page }) => {
    await establishAdminBrowserSession(page);
    const newest = await apiGet<Page_>(page, "/gestiones?page=0&size=1&sort=idManagement,desc");
    const oldest = await apiGet<Page_>(page, "/gestiones?page=0&size=1&sort=idManagement,asc");
    expect(newest.ok && oldest.ok).toBe(true);
    test.skip(newest.data!.totalElements <= 20, "needs more than one page of managements");

    const requested: string[] = [];
    page.on("request", (r) => {
      if (/\/api\/v1\/gestiones(\?|$)/.test(r.url())) requested.push(r.url());
    });

    await page.goto("/dashboard/gestiones");
    await page.waitForLoadState("networkidle");
    const table = page.getByRole("table");
    await expect(table.locator("tbody tr")).toHaveCount(20);
    await expect(table.locator("tbody tr").first().locator("td").first()).toHaveText(
      String(newest.data!.content[0].idManagement),
    );
    const nav = page.getByRole("navigation", { name: /paginaci[oó]n|pagination/i });
    const shown = (await nav.getByTestId("pagination-status").innerText()).replace(/[.,\s\u00a0]/g, "");
    expect(shown).toContain(String(newest.data!.totalElements));
    expect(requested.some((u) => u.includes("size=1000"))).toBe(false);

    await nav.getByRole("button", { name: /última|last/i }).click();
    await expect(page).toHaveURL(/[?&]page=\d+/);
    await expect(table.locator("tbody tr").last().locator("td").first()).toHaveText(
      String(oldest.data!.content[0].idManagement),
      { timeout: 15000 },
    );
  });

  test("the dashboard shows the real managements total", async ({ page }) => {
    await establishAdminBrowserSession(page);
    const total = await apiGet<Page_>(page, "/gestiones?page=0&size=1");
    expect(total.ok).toBe(true);
    const requested: string[] = [];
    page.on("request", (r) => {
      if (/\/api\/v1\/gestiones(\?|$)/.test(r.url())) requested.push(r.url());
    });
    await page.goto("/dashboard");
    await page.waitForLoadState("networkidle");
    const formatted = new Intl.NumberFormat("es-AR").format(total.data!.totalElements);
    await expect(page.getByText(new RegExp(`^(${total.data!.totalElements}|${formatted.replace(".", "\\.")})$`)).first()).toBeVisible();
    expect(requested.some((u) => u.includes("size=1000"))).toBe(false);
  });
});
