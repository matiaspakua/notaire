/**
 * CU39 - Cargar ítems del presupuesto desde la plantilla del tipo de trámite.
 * Issue #834.
 */
import { test, expect } from "@playwright/test";
import { GherkinSteps } from "./gherkin-helpers";
import { authenticateAsAdmin } from "./setup/auth";
import {
  createPersona,
  createPresupuesto,
  createTipoTramite,
  createConcepto,
  createPlantillaPresupuesto,
} from "./setup/api-helpers";

test.describe("CU39 - Cargar ítems desde la plantilla (golden path)", () => {
  let steps: GherkinSteps;

  test.beforeEach(async ({ page }) => {
    steps = new GherkinSteps(page);
    await authenticateAsAdmin(page);
  });

  test("carga los ítems de la plantilla del tipo de trámite en el presupuesto", async ({ page }) => {
    // GIVEN: presupuesto, tipo de trámite con plantilla configurada
    const personaResult = await createPersona(page);
    expect(personaResult.ok).toBe(true);
    const presupuestoResult = await createPresupuesto(page, personaResult.data!.personId);
    expect(presupuestoResult.ok).toBe(true);
    const idPresupuesto = presupuestoResult.data!.idBudget;

    // Own uniquely named type: `.last()` on a shared name raced with parallel tests.
    const tipoTramiteName = `Tipo Items E2E ${Date.now()}`;
    const tipoTramiteResult = await createTipoTramite(page, { name: tipoTramiteName });
    expect(tipoTramiteResult.ok).toBe(true);
    const idTipoTramite = tipoTramiteResult.data!.idProcedureType;

    const conceptoResult = await createConcepto(page, { name: "Honorarios E2E", value: 1500 });
    expect(conceptoResult.ok).toBe(true);

    const plantillaResult = await createPlantillaPresupuesto(
      page,
      idTipoTramite,
      conceptoResult.data!.idConcept,
    );
    expect(plantillaResult.ok, `createPlantillaPresupuesto failed: ${plantillaResult.error}`).toBe(true);

    // WHEN: operador abre los ítems del presupuesto y elige el tipo de trámite
    await steps.givenUserIsOnPage("/dashboard/presupuestos");
    const itemsBtn = page.getByTestId(`btn-items-presupuesto-${idPresupuesto}`);
    await expect(itemsBtn).toBeVisible({ timeout: 8000 });
    await itemsBtn.click();

    const dialog = page.getByTestId("dialog-items-presupuesto");
    await expect(dialog).toBeVisible();

    await dialog.getByTestId("select-tipo-tramite-items").click();
    await page.getByRole("option", { name: tipoTramiteName }).click();

    // AND: carga los ítems de la plantilla
    await dialog.getByTestId("btn-cargar-plantilla").click();

    // THEN: el ítem de la plantilla aparece en el desglose con su subtotal
    const table = dialog.getByTestId("table-items-presupuesto");
    await expect(table).toBeVisible({ timeout: 8000 });
    await expect(table).toContainText("Honorarios E2E");
    await expect(dialog.getByTestId("items-subtotal")).toContainText("1.500");
  });

  test("crea el presupuesto desde el formulario con los ítems de la plantilla del tipo de trámite", async ({ page }) => {
    // GIVEN: persona, tipo de trámite con plantilla configurada
    const lastName = `Plantilla-${Date.now()}`;
    const personaResult = await createPersona(page, { lastName });
    expect(personaResult.ok).toBe(true);
    const tipoTramiteName = `Tipo Alta E2E ${Date.now()}`;
    const tipoTramiteResult = await createTipoTramite(page, { name: tipoTramiteName });
    expect(tipoTramiteResult.ok).toBe(true);
    const conceptoResult = await createConcepto(page, { name: "Honorarios Alta E2E", value: 2500 });
    expect(conceptoResult.ok).toBe(true);
    const plantillaResult = await createPlantillaPresupuesto(
      page,
      tipoTramiteResult.data!.idProcedureType,
      conceptoResult.data!.idConcept,
    );
    expect(plantillaResult.ok, `createPlantillaPresupuesto failed: ${plantillaResult.error}`).toBe(true);

    // WHEN: el operador crea el presupuesto eligiendo cliente, monto y tipo de trámite
    await steps.givenUserIsOnPage("/dashboard/presupuestos");
    await page.getByTestId("btn-nuevo-presupuesto").click();
    await page.getByTestId("select-persona").fill(lastName);
    await page.getByRole("option", { name: new RegExp(lastName) }).click();
    await page.locator('input[type="date"]').fill(new Date().toISOString().split("T")[0]);
    await page.getByTestId("input-monto").fill("100000");
    await page.getByTestId("select-tipo-tramite-nuevo").click();
    await page.getByRole("option", { name: tipoTramiteName }).click();
    await page.getByTestId("btn-guardar-presupuesto").click();

    // THEN: los ítems de la plantilla ya están en el desglose del nuevo presupuesto
    const row = page.getByRole("row").filter({ hasText: lastName }).first();
    await expect(row).toBeVisible({ timeout: 8000 });
    await row.getByRole("button", { name: /ítems|items/i }).click();
    const dialog = page.getByTestId("dialog-items-presupuesto");
    await expect(dialog.getByTestId("table-items-presupuesto")).toContainText("Honorarios Alta E2E", { timeout: 8000 });
  });

  test("muestra un error cuando el tipo de trámite no tiene plantilla configurada", async ({ page }) => {
    const personaResult = await createPersona(page);
    expect(personaResult.ok).toBe(true);
    const presupuestoResult = await createPresupuesto(page, personaResult.data!.personId);
    expect(presupuestoResult.ok).toBe(true);
    const idPresupuesto = presupuestoResult.data!.idBudget;

    const tipoTramiteResult = await createTipoTramite(page, { name: `Sin Plantilla E2E ${Date.now()}` });
    expect(tipoTramiteResult.ok).toBe(true);

    await steps.givenUserIsOnPage("/dashboard/presupuestos");
    const itemsBtn = page.getByTestId(`btn-items-presupuesto-${idPresupuesto}`);
    await expect(itemsBtn).toBeVisible({ timeout: 8000 });
    await itemsBtn.click();

    const dialog = page.getByTestId("dialog-items-presupuesto");
    await expect(dialog).toBeVisible();

    await dialog.getByTestId("select-tipo-tramite-items").click();
    await page.getByRole("option", { name: /Sin Plantilla E2E/i }).last().click();
    await dialog.getByTestId("btn-cargar-plantilla").click();

    // THEN: curated Spanish toast for 400 (preferFallback → items.errorNoPlantilla)
    await expect(
      page
        .locator("[data-sonner-toast]")
        .getByText(/no tiene una plantilla configurada/i)
    ).toBeVisible({ timeout: 8000 });
    await expect(dialog.getByTestId("items-sin-datos")).toBeVisible();
  });
});

for (const viewport of [
  { width: 320, height: 568, label: "320px (mobile)" },
  { width: 768, height: 1024, label: "768px (tablet)" },
  { width: 1024, height: 768, label: "1024px (desktop)" },
]) {
  test(`CU39 - el diálogo de ítems es usable a ${viewport.label}`, async ({ page }) => {
    const steps = new GherkinSteps(page);
    await authenticateAsAdmin(page);

    const personaResult = await createPersona(page);
    expect(personaResult.ok).toBe(true);
    const presupuestoResult = await createPresupuesto(page, personaResult.data!.personId);
    expect(presupuestoResult.ok).toBe(true);
    const idPresupuesto = presupuestoResult.data!.idBudget;

    await page.setViewportSize({ width: viewport.width, height: viewport.height });
    await steps.givenUserIsOnPage("/dashboard/presupuestos");

    const itemsBtn = page.getByTestId(`btn-items-presupuesto-${idPresupuesto}`);
    await expect(itemsBtn).toBeVisible({ timeout: 8000 });
    await itemsBtn.click();

    const dialog = page.getByTestId("dialog-items-presupuesto");
    await expect(dialog).toBeVisible();
    await steps.thenHasNoHorizontalOverflow();
  });
}
