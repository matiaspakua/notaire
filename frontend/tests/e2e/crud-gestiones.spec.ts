/**
 * E2E tests — Gestiones CRUD
 * CU02 Iniciar Gestión, CU13 Ver historial, CU14 Consultar estado,
 * CU16 Archivar Gestión, CU53 Modificar Gestión
 */
import { test, expect } from "@playwright/test";
import { authenticateAsAdmin } from "./setup/auth";
import { createPersona, createPresupuesto, createCompleteCaseGestion } from "./setup/api-helpers";

test.describe("Gestiones CRUD", () => {
  test.beforeEach(async ({ page }) => {
    await authenticateAsAdmin(page);
    await page.goto("/dashboard/gestiones");
    await page.waitForLoadState("domcontentloaded");
  });

  test("can open create modal and cancel", async ({ page }) => {
    await page.getByTestId("btn-nueva-gestion").click();
    const dialog = page.getByRole("dialog");
    await expect(dialog).toBeVisible({ timeout: 10000 });
    await expect(dialog.getByTestId("input-numero-gestion")).toBeVisible();
    await dialog.getByRole("button", { name: /cancelar|cancel/i }).click();
    await expect(dialog).not.toBeVisible();
  });

  test("create form has number input", async ({ page }) => {
    await page.getByTestId("btn-nueva-gestion").click();
    await expect(page.getByTestId("input-numero-gestion")).toBeVisible();
  });
});

/**
 * CU16 - Archivar Gestión with pending-debt verification (RF-22, RF-37, issue #819)
 * Archiving is non-blocking: a gestión with a pending balance can still be
 * archived, but the user sees a debt warning before confirming.
 */
test.describe("CU16 - Archivar Gestión con verificación de deuda", () => {
  async function seedGestion(page: import("@playwright/test").Page, monto: number) {
    const persona = await createPersona(page);
    const idPersona = persona.data!.idPersona;
    const presupuesto = await createPresupuesto(page, idPersona, undefined, { monto });
    const idPresupuesto = presupuesto.data!.idPresupuesto;
    const gestion = await createCompleteCaseGestion(page, {
      presupuestoId: idPresupuesto,
      tipoTramiteId: 1,
      estadoGestionId: 3,
    });
    return gestion.data!;
  }

  test.beforeEach(async ({ page }) => {
    await authenticateAsAdmin(page);
  });

  test("golden path: archiving a gestión with no pending debt shows no-debt warning", async ({ page }) => {
    const { idGestion, numero } = await seedGestion(page, 0);

    await page.goto("/dashboard/gestiones");
    await page.waitForLoadState("domcontentloaded");

    const row = page.getByRole("row", { name: new RegExp(String(numero)) });
    await expect(row).toBeVisible({ timeout: 10000 });

    await page.getByTestId(`btn-archivar-gestion-${idGestion}`).click();
    const dialog = page.getByRole("alertdialog");
    await expect(dialog).toBeVisible();
    await expect(dialog).toContainText(/¿Archivar esta gestión?/i);
    await expect(dialog).toContainText(/no tiene saldo pendiente/i);

    await dialog.getByRole("button", { name: /archivar gestión/i }).click();
    await expect(dialog).not.toBeVisible();
    await expect(page.getByTestId(`btn-archivar-gestion-${idGestion}`)).not.toBeVisible();
  });

  test("edge path: archiving a gestión with pending debt shows debt warning and can be cancelled", async ({
    page,
  }) => {
    const { idGestion, numero } = await seedGestion(page, 1500);

    await page.goto("/dashboard/gestiones");
    await page.waitForLoadState("domcontentloaded");

    const row = page.getByRole("row", { name: new RegExp(String(numero)) });
    await expect(row).toBeVisible({ timeout: 10000 });

    await page.getByTestId(`btn-archivar-gestion-${idGestion}`).click();
    const dialog = page.getByRole("alertdialog");
    await expect(dialog).toBeVisible();
    await expect(dialog).toContainText(/saldo pendiente de/i);

    await dialog.getByRole("button", { name: /cancelar/i }).click();
    await expect(dialog).not.toBeVisible();
    await expect(page.getByTestId(`btn-archivar-gestion-${idGestion}`)).toBeVisible();

    await page.getByTestId(`btn-archivar-gestion-${idGestion}`).click();
    await expect(dialog).toBeVisible();
    await dialog.getByRole("button", { name: /archivar gestión/i }).click();
    await expect(dialog).not.toBeVisible();
    await expect(page.getByTestId(`btn-archivar-gestion-${idGestion}`)).not.toBeVisible();
  });

  for (const viewport of [
    { width: 320, height: 568, label: "320px (mobile)" },
    { width: 768, height: 1024, label: "768px (tablet)" },
    { width: 1024, height: 768, label: "1024px (desktop)" },
  ]) {
    test(`archive action and debt warning are usable at ${viewport.label}`, async ({ page }) => {
      const { idGestion, numero } = await seedGestion(page, 1500);

      await page.setViewportSize({ width: viewport.width, height: viewport.height });
      await page.goto("/dashboard/gestiones");
      await page.waitForLoadState("domcontentloaded");

      const row = page.getByRole("row", { name: new RegExp(String(numero)) });
      await expect(row).toBeVisible({ timeout: 10000 });

      await page.getByTestId(`btn-archivar-gestion-${idGestion}`).click();
      const dialog = page.getByRole("alertdialog");
      await expect(dialog).toBeVisible();
      await expect(dialog).toContainText(/saldo pendiente de/i);
      await dialog.getByRole("button", { name: /cancelar/i }).click();
      await expect(dialog).not.toBeVisible();
    });
  }
});
