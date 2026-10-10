/**
 * E2E — One design-token source (#1365, RNF-05 / RNF-09).
 *
 * The primary button, the active navigation item and the focus ring all render
 * the brand primary #0071E3 (owner decision 2026-10-09), and the dashboard
 * module tiles use one semantic tint instead of 14 raw palette gradients.
 */
import { test, expect } from "@playwright/test";
import { establishAdminBrowserSession } from "./setup/auth";

const BRAND = "rgb(0, 113, 227)";
const SHOTS = "test-results/ui-shots/1365-design-tokens";

test.describe("Design tokens (#1365)", () => {
  test("primary button, active nav item and focus ring share the brand primary", async ({ page }) => {
    await establishAdminBrowserSession(page);
    await page.goto("/dashboard/personas");
    const create = page.getByTestId("btn-nueva-persona");
    await expect(create).toBeVisible();
    await expect(create).toHaveCSS("background-color", BRAND);

    const active = page.locator('nav a[aria-current="page"]').first();
    await expect(active).toBeVisible();
    const activeBg = await active.evaluate((el) => {
      const own = getComputedStyle(el).backgroundColor;
      if (own !== "rgba(0, 0, 0, 0)") return own;
      const pill = el.querySelector<HTMLElement>("span.absolute");
      return pill ? getComputedStyle(pill).backgroundColor : own;
    });
    expect(activeBg).toBe(BRAND);

    const ring = await page.evaluate(() =>
      getComputedStyle(document.documentElement).getPropertyValue("--ring").trim(),
    );
    const primary = await page.evaluate(() =>
      getComputedStyle(document.documentElement).getPropertyValue("--primary").trim(),
    );
    expect(ring).toBe(primary);
    await page.screenshot({ path: `${SHOTS}/personas-desktop.png`, fullPage: false });
  });

  test("dashboard module tiles use one semantic tint, not palette gradients", async ({ page }) => {
    await establishAdminBrowserSession(page);
    await page.goto("/dashboard");
    const tiles = page.locator('a[href^="/dashboard/"] .rounded-2xl.w-14');
    await expect(tiles.first()).toBeVisible();
    const styles = await tiles.evaluateAll((els) =>
      els.map((el) => {
        const cs = getComputedStyle(el);
        return `${cs.backgroundImage}|${cs.backgroundColor}|${cs.color}`;
      }),
    );
    expect(styles.length).toBeGreaterThanOrEqual(12);
    expect(new Set(styles).size).toBe(1);
    expect(styles[0].startsWith("none|")).toBe(true);
    expect(styles[0].endsWith(`|${BRAND}`)).toBe(true);
    await page.screenshot({ path: `${SHOTS}/dashboard-desktop.png`, fullPage: true });
    await page.setViewportSize({ width: 390, height: 844 });
    await page.screenshot({ path: `${SHOTS}/dashboard-mobile.png`, fullPage: true });
  });

  test("no element carries the removed dark-mode class", async ({ page }) => {
    await establishAdminBrowserSession(page);
    await page.goto("/dashboard");
    await expect(page.locator(".dark")).toHaveCount(0);
  });
});
