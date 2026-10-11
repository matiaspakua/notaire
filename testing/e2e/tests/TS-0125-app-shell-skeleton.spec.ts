/**
 * Playwright E2E — no blank first paint on a dashboard hard load (#1359, RNF-03/RNF-05).
 * The dashboard layout rendered nothing until the client mounted and read the session, so
 * the server HTML and the first frame were an empty white page. The server now renders a
 * static app-shell skeleton that the real shell replaces without layout shift.
 */
import { test, expect } from "@playwright/test";
import { establishAdminBrowserSession } from "./setup/auth";

test.describe("App-shell skeleton (#1359)", () => {
  test("the first paint (before any script runs) is the app-shell skeleton, not a blank page", async ({ page, browser, baseURL }) => {
    await establishAdminBrowserSession(page);
    const cookies = await page.context().cookies();
    const noJs = await browser.newContext({ javaScriptEnabled: false, baseURL });
    try {
      await noJs.addCookies(cookies);
      const raw = await noJs.newPage();
      await raw.goto("/dashboard/personas");
      const shell = raw.getByTestId("app-shell-skeleton");
      await expect(shell).toBeVisible();
      await expect(shell).toHaveAttribute("aria-busy", "true");
      await expect(raw.getByTestId("app-shell-skeleton-sidebar")).toBeVisible();
    } finally {
      await noJs.close();
    }
  });

  test("the real shell replaces it after mount, with no layout shift", async ({ page }) => {
    await establishAdminBrowserSession(page);
    await page.addInitScript(() => {
      (window as unknown as { __cls: number }).__cls = 0;
      new PerformanceObserver((list) => {
        for (const e of list.getEntries() as unknown as { value: number; hadRecentInput: boolean }[]) {
          if (!e.hadRecentInput) (window as unknown as { __cls: number }).__cls += e.value;
        }
      }).observe({ type: "layout-shift", buffered: true });
    });
    await page.goto("/dashboard/personas");
    await expect(page.getByTestId("sidebar")).toBeVisible();
    await expect(page.getByTestId("app-shell-skeleton")).toHaveCount(0);
    await page.waitForLoadState("networkidle");
    const cls = await page.evaluate(() => (window as unknown as { __cls: number }).__cls);
    expect(cls).toBeLessThan(0.05);
  });
});
