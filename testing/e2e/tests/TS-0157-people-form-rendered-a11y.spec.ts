/**
 * TS-0157 — People create dialog rendered a11y (#2087)
 *
 * Primary Create meets WCAG 2.2 AA 24px (design system default h-11 ≈ 44px).
 * CheckboxField is at least 24×24. Empty required submit shows alerts.
 * At 320px the dialog does not force horizontal page overflow.
 */
import { test, expect } from "@playwright/test";
import { establishAdminBrowserSession } from "./setup/auth";

test.describe("TS-0157 - people form rendered a11y (#2087)", () => {
  test.beforeEach(async ({ page }) => {
    await establishAdminBrowserSession(page);
  });

  test("desktop: primary height, checkbox floor, required errors", async ({ page }) => {
    await page.setViewportSize({ width: 1280, height: 800 });
    await page.goto("/dashboard/people");
    await page.getByTestId("btn-new-person").click();
    const dialog = page.getByRole("dialog");
    await expect(dialog).toBeVisible();

    const primary = dialog.getByTestId("btn-save-person");
    const pbox = await primary.boundingBox();
    expect(pbox?.height ?? 0).toBeGreaterThanOrEqual(24);
    expect(pbox?.height ?? 0).toBeGreaterThanOrEqual(40); // h-11 token ~44px

    const checkbox = dialog.getByTestId("check-is-client");
    const cbox = await checkbox.boundingBox();
    expect(cbox?.width ?? 0).toBeGreaterThanOrEqual(24);
    expect(cbox?.height ?? 0).toBeGreaterThanOrEqual(24);

    await primary.click();
    await expect(dialog.getByRole("alert").first()).toBeVisible({ timeout: 5000 });
    await expect(dialog.getByTestId("input-firstName")).toHaveAttribute("aria-invalid", "true");
  });

  test("320px: primary still ≥24px and page does not overflow horizontally", async ({ page }) => {
    await page.setViewportSize({ width: 320, height: 720 });
    await page.goto("/dashboard/people");
    await page.getByTestId("btn-new-person").click();
    const dialog = page.getByRole("dialog");
    await expect(dialog).toBeVisible();

    const primary = dialog.getByTestId("btn-save-person");
    const pbox = await primary.boundingBox();
    expect(pbox?.height ?? 0).toBeGreaterThanOrEqual(24);
    expect(await primary.isVisible()).toBe(true);

    const overflow = await page.evaluate(
      () => document.documentElement.scrollWidth > window.innerWidth + 1,
    );
    expect(overflow).toBe(false);
  });
});
