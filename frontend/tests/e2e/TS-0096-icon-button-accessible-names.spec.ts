/**
 * Playwright E2E — icon-only action buttons expose accessible names (#1057).
 * CU76 accessibility validation / WCAG 2.1 SC 4.1.2.
 *
 * List pages can briefly (or durably) show an empty table if global seed is
 * missing for that entity. Each case creates its own row via API so the
 * assertion targets a real action button, not the empty-state placeholder.
 */
import { test, expect } from "@playwright/test";
import { authenticateAsAdmin as adminAuthSetup } from "./setup/auth";
import {
  createConcepto,
  createPersona,
  createPresupuesto,
  createUsuario,
} from "./setup/api-helpers";

test.describe("Icon-only buttons expose accessible names (#1057)", () => {
  test.beforeEach(async ({ page }) => {
    await adminAuthSetup(page);
  });

  test("usuarios row actions are reachable by role name", async ({ page }) => {
    const created = await createUsuario(page);
    expect(created.ok, created.error ?? "createUsuario failed").toBeTruthy();

    await page.goto("/dashboard/administracion/usuarios");
    const table = page.getByRole("table");
    await expect(table).toBeVisible({ timeout: 15000 });
    await expect(table.getByRole("button", { name: /editar|edit/i }).first()).toBeVisible({
      timeout: 15000,
    });
    await expect(table.getByRole("button", { name: /eliminar|delete/i }).first()).toBeVisible();
  });

  test("personas row actions are reachable by role name", async ({ page }) => {
    const created = await createPersona(page);
    expect(created.ok, created.error ?? "createPersona failed").toBeTruthy();

    await page.goto("/dashboard/personas");
    const table = page.getByRole("table");
    await expect(table).toBeVisible({ timeout: 15000 });
    await expect(table.getByRole("button", { name: /editar|edit/i }).first()).toBeVisible({
      timeout: 15000,
    });
    await expect(table.getByRole("button", { name: /eliminar|delete/i }).first()).toBeVisible();
  });

  test("presupuestos resumen action is reachable by role name", async ({ page }) => {
    const persona = await createPersona(page);
    expect(persona.ok && persona.data?.personId, persona.error ?? "createPersona failed").toBeTruthy();
    const presupuesto = await createPresupuesto(page, persona.data!.personId);
    expect(
      presupuesto.ok && presupuesto.data?.idBudget,
      presupuesto.error ?? "createPresupuesto failed",
    ).toBeTruthy();

    await page.goto("/dashboard/presupuestos");
    const table = page.getByRole("table");
    await expect(table).toBeVisible({ timeout: 15000 });
    await expect(page.getByTestId(/^btn-resumen-presupuesto-/).first()).toBeVisible({
      timeout: 15000,
    });
    await expect(
      table.getByRole("button", { name: /resumen financiero|financial summary/i }).first(),
    ).toBeVisible();
  });

  test("administracion/conceptos NotaireIcon actions use button aria-label", async ({ page }) => {
    const created = await createConcepto(page);
    expect(created.ok, created.error ?? "createConcepto failed").toBeTruthy();

    await page.goto("/dashboard/administracion/conceptos");
    const table = page.getByRole("table");
    await expect(table).toBeVisible({ timeout: 15000 });
    await expect(table.getByRole("button", { name: /editar|edit/i }).first()).toBeVisible({
      timeout: 15000,
    });
    await expect(table.getByRole("button", { name: /eliminar|delete/i }).first()).toBeVisible();
  });

  test("named edit/delete remain visible at mobile viewport", async ({ page }) => {
    const created = await createUsuario(page);
    expect(created.ok, created.error ?? "createUsuario failed").toBeTruthy();

    await page.setViewportSize({ width: 320, height: 720 });
    await page.goto("/dashboard/administracion/usuarios");
    const table = page.getByRole("table");
    await expect(table).toBeVisible({ timeout: 15000 });
    await expect(table.getByRole("button", { name: /editar|edit/i }).first()).toBeVisible({
      timeout: 15000,
    });
  });
});
