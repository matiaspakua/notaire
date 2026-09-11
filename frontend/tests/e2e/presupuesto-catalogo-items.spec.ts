/**
 * CU71 - Agregar al presupuesto copias de ítems existentes del catálogo.
 * Issue #834.
 */
import { test, expect } from "@playwright/test";
import { GherkinSteps } from "./gherkin-helpers";
import { authenticateAsAdmin } from "./setup/auth";
import { createPersona, createPresupuesto, createItem } from "./setup/api-helpers";

test.describe("CU71 - Agregar ítems del catálogo (golden path)", () => {
  let steps: GherkinSteps;

  test.beforeEach(async ({ page }) => {
    steps = new GherkinSteps(page);
    await authenticateAsAdmin(page);
  });

  test("agrega un ítem del catálogo al presupuesto y lo muestra en el desglose", async ({ page }) => {
    // GIVEN: presupuesto y un ítem reutilizable en el catálogo
    const personaResult = await createPersona(page);
    expect(personaResult.ok).toBe(true);
    const presupuestoResult = await createPresupuesto(page, personaResult.data!.personId);
    expect(presupuestoResult.ok).toBe(true);
    const idPresupuesto = presupuestoResult.data!.idBudget;

    const itemResult = await createItem(page, { nombre: "Sellado E2E", valor: 750 });
    expect(itemResult.ok, `createItem failed: ${itemResult.error}`).toBe(true);

    // WHEN: operador abre los ítems del presupuesto y agrega el ítem del catálogo
    await steps.givenUserIsOnPage("/dashboard/presupuestos");
    const itemsBtn = page.getByTestId(`btn-items-presupuesto-${idPresupuesto}`);
    await expect(itemsBtn).toBeVisible({ timeout: 8000 });
    await itemsBtn.click();

    const dialog = page.getByTestId("dialog-items-presupuesto");
    await expect(dialog).toBeVisible();

    await dialog.getByTestId("select-catalog-item").click();
    await page.getByRole("option", { name: /Sellado E2E/i }).last().click();
    await dialog.getByTestId("btn-agregar-catalogo").click();

    // THEN: el ítem agregado aparece en el desglose con su subtotal
    const table = dialog.getByTestId("table-items-presupuesto");
    await expect(table).toBeVisible({ timeout: 8000 });
    await expect(table).toContainText("Sellado E2E");
    await expect(dialog.getByTestId("items-subtotal")).toContainText("750");
  });

  test("permite combinar un ítem de plantilla y uno de catálogo en el mismo presupuesto", async ({ page }) => {
    const personaResult = await createPersona(page);
    expect(personaResult.ok).toBe(true);
    const presupuestoResult = await createPresupuesto(page, personaResult.data!.personId);
    expect(presupuestoResult.ok).toBe(true);
    const idPresupuesto = presupuestoResult.data!.idBudget;

    const item1 = await createItem(page, { nombre: "Item Uno E2E", valor: 200 });
    const item2 = await createItem(page, { nombre: "Item Dos E2E", valor: 300 });
    expect(item1.ok).toBe(true);
    expect(item2.ok).toBe(true);

    await steps.givenUserIsOnPage("/dashboard/presupuestos");
    const itemsBtn = page.getByTestId(`btn-items-presupuesto-${idPresupuesto}`);
    await expect(itemsBtn).toBeVisible({ timeout: 8000 });
    await itemsBtn.click();

    const dialog = page.getByTestId("dialog-items-presupuesto");
    await expect(dialog).toBeVisible();

    await dialog.getByTestId("select-catalog-item").click();
    await page.getByRole("option", { name: /Item Uno E2E/i }).last().click();
    await dialog.getByTestId("btn-agregar-catalogo").click();
    await expect(dialog.getByTestId("table-items-presupuesto")).toContainText("Item Uno E2E");

    await dialog.getByTestId("select-catalog-item").click();
    await page.getByRole("option", { name: /Item Dos E2E/i }).last().click();
    await dialog.getByTestId("btn-agregar-catalogo").click();

    // THEN: ambos ítems y el subtotal combinado son visibles
    const table = dialog.getByTestId("table-items-presupuesto");
    await expect(table).toContainText("Item Uno E2E");
    await expect(table).toContainText("Item Dos E2E");
    await expect(dialog.getByTestId("items-subtotal")).toContainText("500");
  });
});
