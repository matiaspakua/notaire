/**
 * E2E — Dashboard stat cards (#1358): exact counts from size=1 page requests,
 * no size=1000 list downloads, and a skeleton (not 0) while a count loads.
 */
import { test, expect, type Page } from "@playwright/test";
import { establishAdminBrowserSession } from "./setup/auth";
import { apiGet } from "./setup/api-helpers";

const RESOURCES = ["gestiones", "personas", "presupuestos"] as const;
const PATHS: Record<(typeof RESOURCES)[number], string> = {
  gestiones: "/gestiones",
  personas: "/people",
  presupuestos: "/presupuestos",
};

async function totalOf(page: Page, path: string): Promise<number> {
  const res = await apiGet<{ totalElements: number }>(page, `${path}?page=0&size=1`);
  return res.data!.totalElements;
}

test.describe("Dashboard stats (#1358)", () => {
  test("counts are the API totals, fetched with size=1 and never size=1000", async ({ page }) => {
    await establishAdminBrowserSession(page);
    const seen: string[] = [];
    page.on("request", (r) => seen.push(r.url()));
    await page.goto("/dashboard");
    await page.waitForLoadState("networkidle");

    const cards = page.getByTestId("stat-value");
    await expect(cards).toHaveCount(3);
    for (const [i, resource] of RESOURCES.entries()) {
      const total = await totalOf(page, PATHS[resource]);
      const formatted = new Intl.NumberFormat("es-AR").format(total);
      await expect(cards.nth(i)).toHaveText(formatted);
      expect(seen.some((u) => u.includes(PATHS[resource]) && /[?&]size=1(&|$)/.test(u))).toBe(true);
    }
    expect(seen.filter((u) => u.includes("size=1000"))).toEqual([]);
  });

  test("a slow count shows a skeleton, not 0", async ({ page }) => {
    await establishAdminBrowserSession(page);
    let release: () => void = () => {};
    const gate = new Promise<void>((r) => (release = r));
    await page.route(/\/people\?page=0&size=1(&|$)/, async (route) => {
      await gate;
      await route.continue();
    });
    await page.goto("/dashboard");
    const people = page.getByTestId("stat-value").nth(1);
    await expect(people).toHaveAttribute("aria-busy", "true", { timeout: 10000 });
    await expect(people).not.toHaveText("0");
    release();
    await expect(people).toHaveAttribute("aria-busy", "false", { timeout: 10000 });
  });
});
