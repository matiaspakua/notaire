/**
 * Playwright E2E — dialog motion (#1349, RNF-05/RNF-06).
 * The shadcn classes animate-in / fade-in / zoom-in-95 had no CSS behind them
 * (no tw-animate-css), so dialogs popped in and vanished with no transition.
 */
import { test, expect, type Page } from "@playwright/test";
import { establishAdminBrowserSession } from "./setup/auth";

async function openDialog(page: Page) {
  await establishAdminBrowserSession(page);
  await page.goto("/dashboard/presupuestos");
  await page.getByTestId("btn-nuevo-presupuesto").click();
  return page.getByRole("dialog");
}

const animationOf = (page: Page, selector: string) =>
  page.locator(selector).first().evaluate((el) => {
    const s = getComputedStyle(el);
    return { name: s.animationName, duration: s.animationDuration };
  });

test.describe("Dialog motion (#1349)", () => {
  test("the dialog and its overlay animate in", async ({ page }) => {
    const dialog = await openDialog(page);
    await expect(dialog).toBeVisible();
    const content = await dialog.evaluate((el) => {
      const s = getComputedStyle(el);
      return { name: s.animationName, duration: s.animationDuration };
    });
    expect(content.name).not.toBe("none");
    expect(content.duration).not.toBe("0s");
    const overlay = await animationOf(page, "[data-state=open].fixed.inset-0");
    expect(overlay.name).not.toBe("none");
  });

  test("the dialog animates out before it unmounts", async ({ page }) => {
    const dialog = await openDialog(page);
    await expect(dialog).toBeVisible();
    await page.keyboard.press("Escape");
    // Radix keeps the content mounted with data-state=closed while an exit animation runs.
    const closing = await page
      .locator("[role=dialog][data-state=closed]")
      .first()
      .evaluate((el) => getComputedStyle(el).animationName)
      .catch(() => "unmounted");
    expect(closing).not.toBe("unmounted");
    expect(closing).not.toBe("none");
    await expect(dialog).toBeHidden();
  });

  test.describe("with reduced motion", () => {
    test.use({ reducedMotion: "reduce" });
    test("the dialog appears without perceptible motion", async ({ page }) => {
      const dialog = await openDialog(page);
      await expect(dialog).toBeVisible();
      // globals.css shortens every animation to 0.01ms under prefers-reduced-motion.
      const seconds = await dialog.evaluate((el) => parseFloat(getComputedStyle(el).animationDuration));
      expect(seconds).toBeLessThan(0.05);
    });
  });
});
