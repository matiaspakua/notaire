/**
 * Playwright E2E — semantic design tokens reach the rendered UI (#1337).
 * RNF-05 / RNF-09: primary actions are brand blue (#0071E3) with white text,
 * the current page is highlighted in the sidebar, destructive actions are red.
 *
 * Tailwind v4 only generates `bg-primary`, `text-destructive`... when the
 * tokens are registered in an `@theme` block; without it these classes
 * compile to nothing and every primary button renders white.
 */
import { test, expect, type Locator } from "@playwright/test";
import { establishAdminBrowserSession } from "./setup/auth";
import { createPersona } from "./setup/api-helpers";

const BRAND_PRIMARY = "rgb(0, 113, 227)"; // #0071E3, owner decision 2026-10-09
const WHITE = "rgb(255, 255, 255)";

function rgb(value: string): number[] {
  return (value.match(/\d+(\.\d+)?/g) ?? []).map(Number);
}

async function style(locator: Locator, prop: "backgroundColor" | "color"): Promise<string> {
  return locator.evaluate((el, p) => getComputedStyle(el)[p], prop);
}

test.describe("Design tokens are applied (#1337)", () => {
  test("login submit button is brand primary with white text", async ({ page }) => {
    await page.goto("/login");
    const submit = page.getByTestId("btn-ingresar");
    await expect(submit).toBeVisible();
    expect(await style(submit, "backgroundColor")).toBe(BRAND_PRIMARY);
    expect(await style(submit, "color")).toBe(WHITE);
  });

  test("active nav item has the primary pill and delete icons are red", async ({ page }) => {
    await establishAdminBrowserSession(page);
    const created = await createPersona(page);
    expect(created.ok, created.error ?? "createPersona failed").toBe(true);

    await page.goto("/dashboard/personas");
    await page.waitForLoadState("networkidle");

    const active = page.locator('[data-testid="sidebar"] a[aria-current="page"]');
    await expect(active).toHaveCount(1);
    const pill = active.locator("span.absolute").first();
    expect(await style(pill, "backgroundColor")).toBe(BRAND_PRIMARY);
    expect(await style(active, "color")).toBe(WHITE);

    const del = page.getByRole("table").getByRole("button", { name: /eliminar|delete/i }).first();
    await expect(del).toBeVisible({ timeout: 15000 });
    const [r, g, b] = rgb(await style(del, "color"));
    expect(r, "delete icon red channel").toBeGreaterThan(180);
    expect(g, "delete icon green channel").toBeLessThan(100);
    expect(b, "delete icon blue channel").toBeLessThan(100);
  });
});
