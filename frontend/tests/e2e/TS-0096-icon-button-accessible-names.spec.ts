/**
 * Playwright E2E — icon-only action buttons expose accessible names (#1057).
 * CU76 accessibility validation / WCAG 2.1 SC 4.1.2.
 */
import { test, expect } from "@playwright/test";
import { authenticateAsAdmin as adminAuthSetup } from "./setup/auth";

test.describe("Icon-only buttons expose accessible names (#1057)", () => {
  test.beforeEach(async ({ page }) => {
    await adminAuthSetup(page);
  });

  test("usuarios row actions are reachable by role name", async ({ page }) => {
    await page.goto("/dashboard/administracion/usuarios");
    const table = page.getByRole("table");
    await expect(table).toBeVisible({ timeout: 15000 });
    await expect(table.getByRole("button", { name: /editar/i }).first()).toBeVisible();
    await expect(table.getByRole("button", { name: /eliminar/i }).first()).toBeVisible();
  });

  test("personas row actions are reachable by role name", async ({ page }) => {
    await page.goto("/dashboard/personas");
    const table = page.getByRole("table");
    await expect(table).toBeVisible({ timeout: 15000 });
    await expect(table.getByRole("button", { name: /editar/i }).first()).toBeVisible();
    await expect(table.getByRole("button", { name: /eliminar/i }).first()).toBeVisible();
  });

  test("presupuestos resumen action is reachable by role name", async ({ page }) => {
    await page.goto("/dashboard/presupuestos");
    const table = page.getByRole("table");
    await expect(table).toBeVisible({ timeout: 15000 });
    await expect(
      table.getByRole("button", { name: /resumen financiero|financial summary/i }).first()
    ).toBeVisible();
  });

  test("administracion/conceptos NotaireIcon actions use button aria-label", async ({ page }) => {
    await page.goto("/dashboard/administracion/conceptos");
    const table = page.getByRole("table");
    await expect(table).toBeVisible({ timeout: 15000 });
    await expect(table.getByRole("button", { name: /editar/i }).first()).toBeVisible();
    await expect(table.getByRole("button", { name: /eliminar/i }).first()).toBeVisible();
  });

  test("named edit/delete remain visible at mobile viewport", async ({ page }) => {
    await page.setViewportSize({ width: 320, height: 720 });
    await page.goto("/dashboard/administracion/usuarios");
    const table = page.getByRole("table");
    await expect(table).toBeVisible({ timeout: 15000 });
    await expect(table.getByRole("button", { name: /editar/i }).first()).toBeVisible();
  });
});
