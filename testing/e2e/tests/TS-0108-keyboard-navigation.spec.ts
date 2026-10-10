/**
 * E2E — Keyboard bypass and route focus (#1352, RNF-10, CU76).
 * WCAG 2.4.1: the first Tab on a dashboard page reaches a visible
 * "Saltar al contenido" link that moves focus into <main>.
 * WCAG 2.4.3: after client-side navigation focus lands on the new page <h1>,
 * not on the clicked sidebar link.
 */
import { test, expect } from "@playwright/test";
import { establishAdminBrowserSession } from "./setup/auth";

test.describe("Keyboard navigation (#1352)", () => {
  test.beforeEach(async ({ page }) => {
    await establishAdminBrowserSession(page);
  });

  test("first Tab focuses a visible skip link; Enter moves focus to main", async ({ page }) => {
    await page.goto("/dashboard/personas");
    await expect(page.getByRole("heading", { level: 1 })).toBeVisible({ timeout: 10000 });

    await page.keyboard.press("Tab");
    const skip = page.getByTestId("skip-link");
    await expect(skip).toBeFocused();
    await expect(skip).toBeVisible();
    await expect(skip).toHaveText("Saltar al contenido");
    const box = await skip.boundingBox();
    expect(box && box.width > 1 && box.height > 1).toBeTruthy();

    await page.keyboard.press("Enter");
    await expect(page.locator("main#main-content")).toBeFocused();

    // The next Tab continues inside the page content, not in the sidebar.
    await page.keyboard.press("Tab");
    const inMain = await page.evaluate(
      () => !!document.activeElement?.closest("main#main-content"),
    );
    expect(inMain).toBe(true);
  });

  test("after clicking a sidebar link, focus is on the new page h1", async ({ page }) => {
    await page.setViewportSize({ width: 1280, height: 800 });
    await page.goto("/dashboard/personas");
    await expect(page.getByRole("heading", { level: 1 })).toBeVisible({ timeout: 10000 });

    await page.getByTestId("sidebar").getByTestId("nav-gestiones").click();
    await expect(page).toHaveURL(/\/dashboard\/gestiones$/);

    const h1 = page.locator("main h1").first();
    await expect(h1).toBeFocused({ timeout: 5000 });
    await expect(h1).toHaveText(/Gestiones/);
  });

  test("first load does not steal focus from the document", async ({ page }) => {
    await page.goto("/dashboard/personas");
    await expect(page.getByRole("heading", { level: 1 })).toBeVisible({ timeout: 10000 });
    const tag = await page.evaluate(() => document.activeElement?.tagName);
    expect(tag).toBe("BODY");
  });
});
