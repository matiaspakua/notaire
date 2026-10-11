/**
 * CU39 - Cargar ítems del budget desde la template del tipo de trámite.
 * Issue #834.
 */
import { test, expect } from "@playwright/test";
import { GherkinSteps } from "./gherkin-helpers";
import { authenticateAsAdmin } from "./setup/auth";
import {
  createPerson,
  createBudget,
  createProcedureType,
  createConcept,
  createBudgetTemplate,
} from "./setup/api-helpers";

test.describe("CU39 - Load items from the template (golden path)", () => {
  let steps: GherkinSteps;

  test.beforeEach(async ({ page }) => {
    steps = new GherkinSteps(page);
    await authenticateAsAdmin(page);
  });

  test("loads the procedure type's template items into the budget", async ({ page }) => {
    // GIVEN: budget, tipo de trámite con template configurada
    const personResult = await createPerson(page);
    expect(personResult.ok).toBe(true);
    const budgetResult = await createBudget(page, personResult.data!.personId);
    expect(budgetResult.ok).toBe(true);
    const idBudget = budgetResult.data!.idBudget;

    // Own uniquely named type: `.last()` on a shared name raced with parallel tests.
    const typeProcedureName = `Tipo Items E2E ${Date.now()}`;
    const typeProcedureResult = await createProcedureType(page, { name: typeProcedureName });
    expect(typeProcedureResult.ok).toBe(true);
    const idTypeProcedure = typeProcedureResult.data!.idProcedureType;

    const conceptResult = await createConcept(page, { name: "Honorarios E2E", value: 1500 });
    expect(conceptResult.ok).toBe(true);

    const templateResult = await createBudgetTemplate(
      page,
      idTypeProcedure,
      conceptResult.data!.idConcept,
    );
    expect(templateResult.ok, `createBudgetTemplate failed: ${templateResult.error}`).toBe(true);

    // WHEN: operador abre los ítems del budget y elige el tipo de trámite
    await steps.givenUserIsOnPage("/dashboard/budgets");
    const itemsBtn = page.getByTestId(`btn-items-budget-${idBudget}`);
    await expect(itemsBtn).toBeVisible({ timeout: 8000 });
    await itemsBtn.click();

    const dialog = page.getByTestId("dialog-items-budget");
    await expect(dialog).toBeVisible();

    await dialog.getByTestId("select-procedure-type-items").click();
    await page.getByRole("option", { name: typeProcedureName, exact: true }).click();

    // AND: carga los ítems de la template
    await dialog.getByTestId("btn-load-template").click();

    // THEN: el ítem de la template aparece en el desglose con su subtotal
    const table = dialog.getByTestId("table-items-budget");
    await expect(table).toBeVisible({ timeout: 8000 });
    await expect(table).toContainText("Honorarios E2E");
    await expect(dialog.getByTestId("items-subtotal")).toContainText("1.500");
  });

  test("creates the budget from the form with the procedure type's template items", async ({ page }) => {
    // GIVEN: person, tipo de trámite con template configurada
    const lastName = `Plantilla-${Date.now()}`;
    const personResult = await createPerson(page, { lastName });
    expect(personResult.ok).toBe(true);
    const typeProcedureName = `Tipo Alta E2E ${Date.now()}`;
    const typeProcedureResult = await createProcedureType(page, { name: typeProcedureName });
    expect(typeProcedureResult.ok).toBe(true);
    const conceptResult = await createConcept(page, { name: "Honorarios Alta E2E", value: 2500 });
    expect(conceptResult.ok).toBe(true);
    const templateResult = await createBudgetTemplate(
      page,
      typeProcedureResult.data!.idProcedureType,
      conceptResult.data!.idConcept,
    );
    expect(templateResult.ok, `createBudgetTemplate failed: ${templateResult.error}`).toBe(true);

    // WHEN: el operador crea el budget eligiendo client, amount y tipo de trámite
    await steps.givenUserIsOnPage("/dashboard/budgets");
    await page.getByTestId("btn-new-budget").click();
    await page.getByTestId("select-person").fill(lastName);
    await page.getByRole("option", { name: new RegExp(lastName) }).click();
    await page.locator('input[type="date"]').fill(new Date().toISOString().split("T")[0]);
    await page.getByTestId("input-amount").fill("100000");
    await page.getByTestId("select-procedure-type-new").click();
    await page.getByRole("option", { name: typeProcedureName }).click();
    await page.getByTestId("btn-save-budget").click();

    // THEN: los ítems de la template ya están en el desglose del nuevo budget
    const row = page.getByRole("row").filter({ hasText: lastName }).first();
    await expect(row).toBeVisible({ timeout: 8000 });
    await row.getByRole("button", { name: /ítems|items/i }).click();
    const dialog = page.getByTestId("dialog-items-budget");
    await expect(dialog.getByTestId("table-items-budget")).toContainText("Honorarios Alta E2E", { timeout: 8000 });
  });

  test("shows an error when the procedure type has no template configured", async ({ page }) => {
    const personResult = await createPerson(page);
    expect(personResult.ok).toBe(true);
    const budgetResult = await createBudget(page, personResult.data!.personId);
    expect(budgetResult.ok).toBe(true);
    const idBudget = budgetResult.data!.idBudget;

    const typeProcedureResult = await createProcedureType(page, { name: `Sin Plantilla E2E ${Date.now()}` });
    expect(typeProcedureResult.ok).toBe(true);

    await steps.givenUserIsOnPage("/dashboard/budgets");
    const itemsBtn = page.getByTestId(`btn-items-budget-${idBudget}`);
    await expect(itemsBtn).toBeVisible({ timeout: 8000 });
    await itemsBtn.click();

    const dialog = page.getByTestId("dialog-items-budget");
    await expect(dialog).toBeVisible();

    await dialog.getByTestId("select-procedure-type-items").click();
    await page.getByRole("option", { name: /Sin Plantilla E2E/i }).last().click();
    await dialog.getByTestId("btn-load-template").click();

    // THEN: curated Spanish toast for 400 (preferFallback → items.errorNoPlantilla)
    await expect(
      page
        .locator("[data-sonner-toast]")
        .getByText(/no tiene una plantilla configurada/i)
    ).toBeVisible({ timeout: 8000 });
    await expect(dialog.getByTestId("items-empty-data")).toBeVisible();
  });
});

for (const viewport of [
  { width: 320, height: 568, label: "320px (mobile)" },
  { width: 768, height: 1024, label: "768px (tablet)" },
  { width: 1024, height: 768, label: "1024px (desktop)" },
]) {
  test(`CU39 - the items dialog is usable at ${viewport.label}`, async ({ page }) => {
    const steps = new GherkinSteps(page);
    await authenticateAsAdmin(page);

    const personResult = await createPerson(page);
    expect(personResult.ok).toBe(true);
    const budgetResult = await createBudget(page, personResult.data!.personId);
    expect(budgetResult.ok).toBe(true);
    const idBudget = budgetResult.data!.idBudget;

    await page.setViewportSize({ width: viewport.width, height: viewport.height });
    await steps.givenUserIsOnPage("/dashboard/budgets");

    const itemsBtn = page.getByTestId(`btn-items-budget-${idBudget}`);
    await expect(itemsBtn).toBeVisible({ timeout: 8000 });
    await itemsBtn.click();

    const dialog = page.getByTestId("dialog-items-budget");
    await expect(dialog).toBeVisible();
    await steps.thenHasNoHorizontalOverflow();
  });
}
