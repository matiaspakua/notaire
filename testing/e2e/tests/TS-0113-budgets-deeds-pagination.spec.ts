/**
 * Playwright E2E — the budgets and deeds lists are paginated server-side (#1340 slice 5,
 * CU01/CU60, CU06/CU07). Both requested size=1000 (budgets after the 1000th were unreachable),
 * and their filters sent parameters the backend ignores, so they listed every row.
 */
import { test, expect, type Page } from "@playwright/test";
import { establishAdminBrowserSession } from "./setup/auth";
import { apiGet } from "./setup/api-helpers";

interface Paged<T> {
  content: T[];
  totalElements: number;
}

function track(page: Page, re: RegExp) {
  const seen: string[] = [];
  page.on("request", (r) => {
    if (re.test(r.url())) seen.push(r.url());
  });
  return seen;
}

async function expectPagedList(page: Page, route: string, newestId: number, oldestId: number, total: number) {
  await page.goto(route);
  await page.waitForLoadState("networkidle");
  const rows = page.getByRole("table").locator("tbody tr");
  await expect(rows).toHaveCount(20);
  await expect(rows.first().locator("td").first()).toHaveText(String(newestId));
  const nav = page.getByRole("navigation", { name: /paginaci[oó]n|pagination/i });
  const shown = (await nav.getByTestId("pagination-status").innerText()).replace(/[.,\s\u00a0]/g, "");
  expect(shown).toContain(String(total));
  await nav.getByRole("button", { name: /última|last/i }).click();
  await expect(page).toHaveURL(/[?&]page=\d+/);
  await expect(rows.last().locator("td").first()).toHaveText(String(oldestId), { timeout: 15000 });
}

test.describe("Budgets and deeds list pagination (#1340)", () => {
  test("budgets: a page of 20, newest first, the total, the oldest budget and the real dashboard count", async ({ page }) => {
    await establishAdminBrowserSession(page);
    const newest = await apiGet<Paged<{ idBudget: number }>>(page, "/budgets?page=0&size=1&sort=idBudget,desc");
    const oldest = await apiGet<Paged<{ idBudget: number }>>(page, "/budgets?page=0&size=1&sort=idBudget,asc");
    test.skip(newest.data!.totalElements <= 20, "needs more than one page of budgets");
    const seen = track(page, /\/api\/v1\/budgets(\?|$)/);

    await expectPagedList(page, "/dashboard/budgets", newest.data!.content[0].idBudget, oldest.data!.content[0].idBudget, newest.data!.totalElements);

    await page.goto("/dashboard");
    await page.waitForLoadState("networkidle");
    const total = newest.data!.totalElements;
    const formatted = new Intl.NumberFormat("es-AR").format(total).replace(".", "\\.");
    await expect(page.getByText(new RegExp(`^(${total}|${formatted})$`)).first()).toBeVisible();
    expect(seen.some((u) => u.includes("size=1000"))).toBe(false);
  });

  test("budgets: the status filter asks the backend for that status", async ({ page }) => {
    await establishAdminBrowserSession(page);
    const seen = track(page, /\/api\/v1\/budgets\/search/);
    await page.goto("/dashboard/budgets");
    await page.getByTestId("select-status").click();
    await page.getByRole("option", { name: "Aprobado" }).click();
    await expect.poll(() => seen.some((u) => /[?&]status=APROBADO/.test(u))).toBe(true);
    const cells = page.getByRole("table").locator("tbody tr td:nth-child(5)");
    const texts = await cells.allInnerTexts();
    // The status column shows the translated label of the stored code (#1346).
    expect(texts.every((s) => s.trim() === "Aprobado" || s.trim() === "")).toBe(true);
  });

  test("deeds: a page of 20, newest first, the total, the oldest deed and the number search", async ({ page }) => {
    await establishAdminBrowserSession(page);
    const newest = await apiGet<Paged<{ idDeed: number; number: number }>>(page, "/deeds?page=0&size=1&sort=idDeed,desc");
    const oldest = await apiGet<Paged<{ idDeed: number }>>(page, "/deeds?page=0&size=1&sort=idDeed,asc");
    test.skip(newest.data!.totalElements <= 20, "needs more than one page of deeds");
    const seen = track(page, /\/api\/v1\/deeds(\?|\/search)/);

    await expectPagedList(page, "/dashboard/deeds", newest.data!.content[0].idDeed, oldest.data!.content[0].idDeed, newest.data!.totalElements);
    expect(seen.some((u) => u.includes("size=1000"))).toBe(false);

    const number = newest.data!.content[0].number;
    await page.getByTestId("input-search-deed").fill(String(number));
    const rows = page.getByRole("table").locator("tbody tr");
    await expect.poll(() => seen.some((u) => u.includes(`buscar?number=${number}`))).toBe(true);
    await expect(rows.first().locator("td").first()).toHaveText(String(newest.data!.content[0].idDeed));
    expect(await rows.count()).toBeLessThan(20);
  });
});
