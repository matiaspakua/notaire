/**
 * TS-0159 — Management create form rendered a11y (#2259, forms audit phase 3, module 1)
 *
 * Rendered pass at desktop (1280) and 320px: the primary Save meets the design-system height,
 * an empty required submit shows an error alert for each required field, every alert is tied to
 * an `aria-invalid` control through `aria-describedby` (searchable pickers included), and the
 * page does not scroll sideways.
 */
import { test, expect, type Page } from "@playwright/test";
import { establishAdminBrowserSession } from "./setup/auth";

async function openCreateDialog(page: Page) {
  await page.goto("/dashboard/managements");
  await page.getByTestId("btn-new-management").click();
  const dialog = page.getByRole("dialog");
  await expect(dialog).toBeVisible();
  return dialog;
}

async function untiedAlerts(page: Page): Promise<string[]> {
  return page.evaluate(() =>
    [...document.querySelectorAll("[role=alert]")]
      .filter((alert) => {
        const control = alert.id ? document.querySelector(`[aria-describedby~="${alert.id}"]`) : null;
        return !control || control.getAttribute("aria-invalid") !== "true";
      })
      .map((alert) => alert.textContent?.trim() ?? ""),
  );
}

test.describe("TS-0159 - management form rendered a11y (#2259)", () => {
  test.beforeEach(async ({ page }) => {
    await establishAdminBrowserSession(page);
  });

  for (const viewport of [
    { name: "desktop", width: 1280, height: 800 },
    { name: "320px", width: 320, height: 720 },
  ]) {
    test(`${viewport.name}: every required error is tied to an invalid control`, async ({ page }) => {
      await page.setViewportSize({ width: viewport.width, height: viewport.height });
      const dialog = await openCreateDialog(page);

      const save = dialog.getByTestId("btn-save-management");
      const box = await save.boundingBox();
      expect(box?.height ?? 0).toBeGreaterThanOrEqual(40);

      await save.click();
      await expect(dialog.getByRole("alert").first()).toBeVisible({ timeout: 5000 });
      expect(await dialog.getByRole("alert").count()).toBeGreaterThanOrEqual(3);
      expect(await untiedAlerts(page)).toEqual([]);

      const overflow = await page.evaluate(
        () => document.documentElement.scrollWidth > window.innerWidth + 1,
      );
      expect(overflow).toBe(false);
    });
  }
});
