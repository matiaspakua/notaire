/**
 * E2E tests — Mobile/responsive viewport coverage (issue #610)
 *
 * .claude/rules/ui-ux-design.md mandates testing at the 320px/768px/1024px
 * breakpoints, but no spec previously asserted layout at those widths.
 * Runs under both the default "chromium" project (Desktop Chrome) and the
 * "mobile" project (devices["iPhone SE"]) added to playwright.config.ts.
 */
import { type Page, test, expect } from "@playwright/test";

async function hasNoHorizontalOverflow(page: Page): Promise<boolean> {
  return page.evaluate(
    () => document.documentElement.scrollWidth <= window.innerWidth,
  );
}

async function loginAsAdmin(page: Page) {
  await page.goto("/login");
  await page.getByTestId("input-usuario").fill("admin");
  await page.getByTestId("input-contrasenia").fill("admin");
  await page.getByTestId("btn-ingresar").click();
  await page.waitForURL(/\/dashboard/, { timeout: 15000 });
  await page.waitForLoadState("domcontentloaded");
}

test.describe("Mobile/responsive viewport @mobile", () => {
  test("login page has no horizontal overflow at 320px", async ({ page }) => {
    await page.setViewportSize({ width: 320, height: 568 });
    await page.goto("/login");

    await expect(page.getByTestId("input-usuario")).toBeVisible();
    await expect(page.getByTestId("input-contrasenia")).toBeVisible();
    await expect(page.getByTestId("btn-ingresar")).toBeVisible();
    expect(await hasNoHorizontalOverflow(page)).toBe(true);
  });

  test("login page has no horizontal overflow at 768px (tablet)", async ({
    page,
  }) => {
    await page.setViewportSize({ width: 768, height: 1024 });
    await page.goto("/login");

    expect(await hasNoHorizontalOverflow(page)).toBe(true);
  });

  test("dashboard has no horizontal overflow at 320px after login", async ({
    page,
  }) => {
    await page.setViewportSize({ width: 1280, height: 800 });
    await loginAsAdmin(page);

    await page.setViewportSize({ width: 320, height: 568 });
    expect(await hasNoHorizontalOverflow(page)).toBe(true);
  });

  test("sidebar is hidden by default and opens via hamburger below 768px", async ({
    page,
  }) => {
    await page.setViewportSize({ width: 1280, height: 800 });
    await loginAsAdmin(page);

    await page.setViewportSize({ width: 320, height: 568 });
    await expect(page.getByTestId("sidebar")).not.toBeVisible();
    await expect(page.getByTestId("btn-sidebar-toggle")).toBeVisible();

    await page.getByTestId("btn-sidebar-toggle").click();
    await expect(page.getByTestId("sidebar")).toBeVisible();
    expect(await hasNoHorizontalOverflow(page)).toBe(true);

    await page.getByTestId("sidebar-backdrop").click();
    await expect(page.getByTestId("sidebar")).not.toBeVisible();
  });

  test("sidebar is always visible with no hamburger at desktop widths", async ({
    page,
  }) => {
    await page.setViewportSize({ width: 1280, height: 800 });
    await loginAsAdmin(page);

    await expect(page.getByTestId("sidebar")).toBeVisible();
    await expect(page.getByTestId("btn-sidebar-toggle")).not.toBeVisible();
  });
});
