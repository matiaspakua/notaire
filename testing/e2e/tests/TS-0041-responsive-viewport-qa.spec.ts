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

  // #1350: the drawer is a modal navigation sheet (WAI-ARIA dialog pattern).
  test("mobile drawer: aria-expanded, focus moves in, Tab stays inside, Escape closes and returns focus", async ({
    page,
  }) => {
    await page.setViewportSize({ width: 1280, height: 800 });
    await loginAsAdmin(page);
    await page.setViewportSize({ width: 390, height: 844 });

    const toggle = page.getByTestId("btn-sidebar-toggle");
    await expect(toggle).toHaveAttribute("aria-expanded", "false");
    await expect(toggle).toHaveAccessibleName("Abrir menú");
    await toggle.click();

    const drawer = page.getByRole("dialog");
    await expect(drawer).toBeVisible();
    await expect(toggle).toHaveAttribute("aria-expanded", "true");
    await expect(drawer).toHaveAttribute("id", (await toggle.getAttribute("aria-controls"))!);
    // Focus starts on the first navigation link.
    await expect(drawer.getByRole("navigation").getByRole("link").first()).toBeFocused();
    // A visible, translated close button.
    await expect(drawer.getByRole("button", { name: "Cerrar menú" })).toBeVisible();
    // The page behind does not scroll.
    expect(await page.evaluate(() => getComputedStyle(document.body).overflow)).toBe("hidden");

    for (let i = 0; i < 30; i++) {
      await page.keyboard.press("Tab");
      expect(await drawer.evaluate((el) => el.contains(document.activeElement))).toBe(true);
    }

    await page.keyboard.press("Escape");
    await expect(drawer).toBeHidden();
    await expect(toggle).toBeFocused();
    await expect(toggle).toHaveAttribute("aria-expanded", "false");
  });

  test("mobile drawer: the close button closes it", async ({ page }) => {
    await page.setViewportSize({ width: 1280, height: 800 });
    await loginAsAdmin(page);
    await page.setViewportSize({ width: 390, height: 844 });
    await page.getByTestId("btn-sidebar-toggle").click();
    await page.getByRole("dialog").getByRole("button", { name: "Cerrar menú" }).click();
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
