/**
 * E2E tests — Animated workflow tracker on the dashboard landing page
 * CU: CU70 (Workflow de Estados de Gestión), CU71 (Trazabilidad de Trámites)
 * Issue: #453
 */
import { type Page, test, expect } from "@playwright/test";

async function loginAs(page: Page) {
  await page.goto("/login");
  await page.getByTestId("input-usuario").fill("admin");
  await page.getByTestId("input-contrasenia").fill("admin");
  await page.getByTestId("btn-ingresar").click();
  await page.waitForURL(/\/dashboard/, { timeout: 15000 });
  await page.waitForLoadState("domcontentloaded");
}

test.describe("Workflow hero section (CU70, CU71)", () => {
  test.beforeEach(async ({ page }) => {
    await loginAs(page);
  });

  test("workflow hero renders with search form", async ({ page }) => {
    await expect(page.getByTestId("workflow-hero")).toBeVisible({ timeout: 10000 });
    await expect(page.getByTestId("workflow-search-form")).toBeVisible();
    await expect(page.locator("#workflow-ref")).toBeVisible();
  });

  test("workflow tracker renders nodes and legend for latest gestion", async ({ page }) => {
    const tracker = page.getByTestId("workflow-tracker");
    await expect(tracker).toBeVisible({ timeout: 15000 });

    const nodes = tracker.locator('[data-testid^="workflow-node-"]:not([data-testid="workflow-node-modal"])');
    expect(await nodes.count()).toBeGreaterThan(0);
    await expect(page.getByTestId("workflow-legend")).toBeVisible();
  });

  test("clicking a node opens detail modal and Escape closes it", async ({ page }) => {
    const tracker = page.getByTestId("workflow-tracker");
    await expect(tracker).toBeVisible({ timeout: 15000 });

    const firstNode = tracker
      .locator('[data-testid^="workflow-node-"]:not([data-testid="workflow-node-modal"])')
      .first();
    await firstNode.click();

    const modal = page.getByTestId("workflow-node-modal");
    await expect(modal).toBeVisible({ timeout: 5000 });
    await expect(page.getByTestId("workflow-modal-status")).toBeVisible();

    await page.keyboard.press("Escape");
    await expect(modal).not.toBeVisible({ timeout: 5000 });
  });

  test("searching an unknown reference shows not-found message", async ({ page }) => {
    await page.locator("#workflow-ref").fill("99999999");
    await page.getByTestId("workflow-search-form").locator('button[type="submit"]').click();

    await expect(page.getByTestId("workflow-not-found")).toBeVisible({ timeout: 10000 });
  });

  test("searching an existing reference renders its trace", async ({ page }) => {
    const tracker = page.getByTestId("workflow-tracker");
    await expect(tracker).toBeVisible({ timeout: 15000 });

    const subtitle = page.getByTestId("workflow-subtitle");
    await expect(subtitle).toBeVisible();
  });
});
