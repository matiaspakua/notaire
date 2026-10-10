/**
 * Playwright E2E — the people list is paginated server-side (#1340 slice 2, CU18/CU54).
 * The list used to request size=1000, so on a database with more people every
 * person after the 1000th was unreachable from /dashboard/personas.
 */
import { test, expect } from "@playwright/test";
import { establishAdminBrowserSession } from "./setup/auth";
import { apiGet } from "./setup/api-helpers";

interface PeoplePage {
  content: { personId: number; identificationNumber?: string }[];
  totalElements: number;
}

test.describe("People list pagination (#1340)", () => {
  test("shows a page of 20, newest first, with the total and reaches the oldest person", async ({ page }) => {
    await establishAdminBrowserSession(page);
    const newest = await apiGet<PeoplePage>(page, "/people?page=0&size=1&sort=idPerson,desc");
    const oldest = await apiGet<PeoplePage>(page, "/people?page=0&size=1&sort=idPerson,asc");
    expect(newest.ok && oldest.ok).toBe(true);
    test.skip(newest.data!.totalElements <= 20, "needs more than one page of people");

    const requested: string[] = [];
    page.on("request", (r) => {
      if (/\/api\/v1\/people(\?|$)/.test(r.url())) requested.push(r.url());
    });

    await page.goto("/dashboard/personas");
    await page.waitForLoadState("networkidle");
    const table = page.getByRole("table");
    await expect(table.locator("tbody tr")).toHaveCount(20);
    await expect(table.locator("tbody tr").first().locator("td").first()).toHaveText(
      String(newest.data!.content[0].personId),
    );
    const nav = page.getByRole("navigation", { name: /paginaci[oó]n|pagination/i });
    const status = nav.getByTestId("pagination-status");
    await expect(status).toContainText(/1[–-]20/);
    const shown = (await status.innerText()).replace(/[.,\s\u00a0]/g, "");
    expect(shown).toContain(String(newest.data!.totalElements));
    expect(requested.some((u) => u.includes("size=1000"))).toBe(false);

    await nav.getByRole("button", { name: /última|last/i }).click();
    await expect(page).toHaveURL(/[?&]page=\d+/);
    await expect(
      table.getByRole("cell", { name: String(oldest.data!.content[0].personId), exact: true }),
    ).toBeVisible({ timeout: 15000 });

    await page.reload();
    await page.waitForLoadState("networkidle");
    await expect(
      table.getByRole("cell", { name: String(oldest.data!.content[0].personId), exact: true }),
    ).toBeVisible({ timeout: 15000 });
  });

  test("the duplicate-document toast links to an existing person outside the first page", async ({ page }) => {
    await establishAdminBrowserSession(page);
    const oldest = await apiGet<PeoplePage>(page, "/people?page=0&size=50&sort=idPerson,asc");
    const target = oldest.data!.content.find((p) => p.identificationNumber);
    test.skip(!target, "no old person with a document number");

    await page.goto("/dashboard/personas");
    await page.waitForLoadState("networkidle");
    await page.getByTestId("btn-nueva-persona").click();
    await page.getByTestId("input-firstName").fill("Duplicado");
    await page.getByTestId("input-lastName").fill("Paginado");
    await page.getByTestId("input-dni").fill(target!.identificationNumber!);
    await page.getByRole("button", { name: /^crear$|^create$/i }).click();

    const link = page.locator("[data-sonner-toast]").getByRole("button", { name: /ver persona existente/i });
    await expect(link).toBeVisible({ timeout: 8000 });
    await link.click();
    await expect(page.getByRole("dialog")).toBeVisible();
    await expect(page.getByTestId("input-dni")).toHaveValue(target!.identificationNumber!);
  });
});
