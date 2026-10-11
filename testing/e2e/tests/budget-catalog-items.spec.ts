/**
 * CU71 - Agregar al budget copias de ítems existentes del catálogo.
 * Issue #834.
 */
import { test, expect } from "@playwright/test";
import { GherkinSteps } from "./gherkin-helpers";
import { authenticateAsAdmin } from "./setup/auth";
import { createPerson, createBudget, createItem } from "./setup/api-helpers";

test.describe("CU71 - Add catalog items (golden path)", () => {
  let steps: GherkinSteps;

  test.beforeEach(async ({ page }) => {
    steps = new GherkinSteps(page);
    await authenticateAsAdmin(page);
  });

  test("adds a catalog item to the budget and shows it in the breakdown", async ({ page }) => {
    // GIVEN: budget y un ítem reutilizable en el catálogo
    const personResult = await createPerson(page);
    expect(personResult.ok).toBe(true);
    const budgetResult = await createBudget(page, personResult.data!.personId);
    expect(budgetResult.ok).toBe(true);
    const idBudget = budgetResult.data!.idBudget;

    const itemResult = await createItem(page, { name: "Sellado E2E", value: 750 });
    expect(itemResult.ok, `createItem failed: ${itemResult.error}`).toBe(true);

    // WHEN: operador abre los ítems del budget y agrega el ítem del catálogo
    await steps.givenUserIsOnPage("/dashboard/budgets");
    const itemsBtn = page.getByTestId(`btn-items-budget-${idBudget}`);
    await expect(itemsBtn).toBeVisible({ timeout: 8000 });
    await itemsBtn.click();

    const dialog = page.getByTestId("dialog-items-budget");
    await expect(dialog).toBeVisible();

    await dialog.getByTestId("select-catalog-item").click();
    await page.getByRole("option", { name: /Sellado E2E/i }).last().click();
    await dialog.getByTestId("btn-agregar-catalogo").click();

    // THEN: el ítem agregado aparece en el desglose con su subtotal
    const table = dialog.getByTestId("table-items-budget");
    await expect(table).toBeVisible({ timeout: 8000 });
    await expect(table).toContainText("Sellado E2E");
    await expect(dialog.getByTestId("items-subtotal")).toContainText("750");
  });

  test("allows a template item and a catalog item on the same budget", async ({ page }) => {
    const personResult = await createPerson(page);
    expect(personResult.ok).toBe(true);
    const budgetResult = await createBudget(page, personResult.data!.personId);
    expect(budgetResult.ok).toBe(true);
    const idBudget = budgetResult.data!.idBudget;

    const item1 = await createItem(page, { name: "Item Uno E2E", value: 200 });
    const item2 = await createItem(page, { name: "Item Dos E2E", value: 300 });
    expect(item1.ok).toBe(true);
    expect(item2.ok).toBe(true);

    await steps.givenUserIsOnPage("/dashboard/budgets");
    const itemsBtn = page.getByTestId(`btn-items-budget-${idBudget}`);
    await expect(itemsBtn).toBeVisible({ timeout: 8000 });
    await itemsBtn.click();

    const dialog = page.getByTestId("dialog-items-budget");
    await expect(dialog).toBeVisible();

    await dialog.getByTestId("select-catalog-item").click();
    await page.getByRole("option", { name: /Item Uno E2E/i }).last().click();
    await dialog.getByTestId("btn-agregar-catalogo").click();
    await expect(dialog.getByTestId("table-items-budget")).toContainText("Item Uno E2E");

    await dialog.getByTestId("select-catalog-item").click();
    await page.getByRole("option", { name: /Item Dos E2E/i }).last().click();
    await dialog.getByTestId("btn-agregar-catalogo").click();

    // THEN: ambos ítems y el subtotal combinado son visibles
    const table = dialog.getByTestId("table-items-budget");
    await expect(table).toContainText("Item Uno E2E");
    await expect(table).toContainText("Item Dos E2E");
    await expect(dialog.getByTestId("items-subtotal")).toContainText("500");
  });
});
