/**
 * Playwright E2E — icon-only action buttons expose accessible names (#1057).
 * CU76 accessibility validation / WCAG 2.1 SC 4.1.2.
 *
 * List pages show an empty-state row when the browser session has no JWT
 * (or when no entity rows exist). Establish a real admin browser session,
 * create a row via API, reload the list, then assert role names.
 */
import { test, expect } from "@playwright/test";
import { establishAdminBrowserSession } from "./setup/auth";
import {
  createConcepto,
  createPersona,
  createPresupuesto,
  createUsuario,
} from "./setup/api-helpers";

async function openList(page: import("@playwright/test").Page, path: string) {
  await page.goto(path);
  await page.waitForLoadState("networkidle");
  const table = page.getByRole("table");
  await expect(table).toBeVisible({ timeout: 15000 });
  return table;
}

test.describe("Icon-only buttons expose accessible names (#1057)", () => {
  test.beforeEach(async ({ page }) => {
    await establishAdminBrowserSession(page);
  });

  test("usuarios row actions are reachable by role name", async ({ page }) => {
    const created = await createUsuario(page);
    expect(created.ok, created.error ?? "createUsuario failed").toBe(true);

    const table = await openList(page, "/dashboard/administracion/usuarios");
    await expect(table.getByRole("button", { name: /editar|edit/i }).first()).toBeVisible({
      timeout: 15000,
    });
    await expect(table.getByRole("button", { name: /eliminar|delete/i }).first()).toBeVisible();
  });

  test("personas row actions are reachable by role name", async ({ page }) => {
    const created = await createPersona(page);
    expect(created.ok, created.error ?? "createPersona failed").toBe(true);

    const table = await openList(page, "/dashboard/personas");
    await expect(table.getByRole("button", { name: /editar|edit/i }).first()).toBeVisible({
      timeout: 15000,
    });
    await expect(table.getByRole("button", { name: /eliminar|delete/i }).first()).toBeVisible();
  });

  test("presupuestos resumen action is reachable by role name", async ({ page }) => {
    const persona = await createPersona(page);
    expect(persona.ok, persona.error ?? "createPersona failed").toBe(true);
    const presupuesto = await createPresupuesto(page, persona.data!.personId);
    expect(presupuesto.ok, presupuesto.error ?? "createPresupuesto failed").toBe(true);

    const table = await openList(page, "/dashboard/presupuestos");
    await expect(page.getByTestId(/^btn-resumen-presupuesto-/).first()).toBeVisible({
      timeout: 15000,
    });
    await expect(
      table.getByRole("button", { name: /resumen financiero|financial summary/i }).first(),
    ).toBeVisible();
  });

  test("administracion/conceptos NotaireIcon actions use button aria-label", async ({ page }) => {
    const created = await createConcepto(page);
    expect(created.ok, created.error ?? "createConcepto failed").toBe(true);

    const table = await openList(page, "/dashboard/administracion/conceptos");
    await expect(table.getByRole("button", { name: /editar|edit/i }).first()).toBeVisible({
      timeout: 15000,
    });
    await expect(table.getByRole("button", { name: /eliminar|delete/i }).first()).toBeVisible();
  });

  test("named edit/delete remain visible at mobile viewport", async ({ page }) => {
    const created = await createUsuario(page);
    expect(created.ok, created.error ?? "createUsuario failed").toBe(true);

    await page.setViewportSize({ width: 320, height: 720 });
    const table = await openList(page, "/dashboard/administracion/usuarios");
    await expect(table.getByRole("button", { name: /editar|edit/i }).first()).toBeVisible({
      timeout: 15000,
    });
  });
});
