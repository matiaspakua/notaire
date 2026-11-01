import { expect, test } from "@playwright/test";
import AxeBuilder from "@axe-core/playwright";

// The read-only demo (#1985): the real frontend under /notaire/demo/, API answered by the
// recorded fixtures. Built by `npm run build:demo` after `npm run build`.

test("demo signs in, shows sample data and the read-only banner, and sends no API request", async ({ page }) => {
  const apiRequests: string[] = [];
  page.on("request", (r) => {
    if (new URL(r.url()).pathname.includes("/api/")) apiRequests.push(`${r.method()} ${r.url()}`);
  });

  await page.goto("demo/");
  await expect(page).toHaveURL(/\/notaire\/demo\/login\/?$/);
  await expect(page.getByRole("region", { name: /demo notice/i })).toContainText("Demo – sample data, read-only");

  await page.getByTestId("input-username").fill("visitor");
  await page.getByTestId("input-password").fill("anything");
  await page.getByRole("button", { name: /sign in|log in|iniciar/i }).click();
  await expect(page).toHaveURL(/\/notaire\/demo\/dashboard\/?$/);

  await page.goto("demo/dashboard/managements/");
  await expect(page.getByText("Compraventa Lote 12", { exact: false }).first()).toBeVisible();
  await expect(page.getByRole("region", { name: /demo notice/i })).toBeVisible();

  const status = await page.evaluate(async () => (await fetch("/api/v1/people", { method: "POST", body: "{}" })).status);
  expect(status).toBe(403);
  expect(apiRequests).toEqual([]);

  const overflow = await page.evaluate(() => document.documentElement.scrollWidth - document.documentElement.clientWidth);
  expect(overflow, "page scrolls sideways").toBeLessThanOrEqual(1);
});

test("demo banner has no axe violations", async ({ page }) => {
  await page.goto("demo/login/");
  await page.waitForLoadState("networkidle");
  const axe = await new AxeBuilder({ page }).include("[data-demo-banner]").withTags(["wcag2a", "wcag2aa"]).analyze();
  expect(axe.violations.map((v) => v.id)).toEqual([]);
});
