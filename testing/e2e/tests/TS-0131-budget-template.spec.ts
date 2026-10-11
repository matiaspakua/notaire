/**
 * CU39 - Load budget items from the procedure type template.
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
    // GIVEN: a budget and a procedure type with a configured template
    const personResult = await createPerson(page);
    expect(personResult.ok).toBe(true);
    const budgetResult = await createBudget(page, personResult.data!.personId);
    expect(budgetResult.ok).toBe(true);
    const idBudget = budgetResult.data!.idBudget;

    // Own uniquely named type: `.last()` on a shared name raced with parallel tests.
    const procedureTypeName = `Items type E2E ${Date.now()}`;
    const procedureTypeResult = await createProcedureType(page, { name: procedureTypeName });
    expect(procedureTypeResult.ok).toBe(true);
    const idProcedureType = procedureTypeResult.data!.idProcedureType;

    const conceptResult = await createConcept(page, { name: "Honorarios E2E", value: 1500 });
    expect(conceptResult.ok).toBe(true);

    const templateResult = await createBudgetTemplate(
      page,
      idProcedureType,
      conceptResult.data!.idConcept,
    );
    expect(templateResult.ok, `createBudgetTemplate failed: ${templateResult.error}`).toBe(true);

    // WHEN: the operator opens the budget items and chooses the procedure type
    await steps.givenUserIsOnPage("/dashboard/budgets");
    const itemsBtn = page.getByTestId(`btn-items-budget-${idBudget}`);
    await expect(itemsBtn).toBeVisible({ timeout: 8000 });
    await itemsBtn.click();

    const dialog = page.getByTestId("dialog-items-budget");
    await expect(dialog).toBeVisible();

    await dialog.getByTestId("select-procedure-type-items").click();
    await page.getByRole("option", { name: procedureTypeName, exact: true }).click();

    // AND: loads the template items
    await dialog.getByTestId("btn-load-template").click();

    // THEN: the template item appears in the breakdown with its subtotal
    const table = dialog.getByTestId("table-items-budget");
    await expect(table).toBeVisible({ timeout: 8000 });
    await expect(table).toContainText("Honorarios E2E");
    await expect(dialog.getByTestId("items-subtotal")).toContainText("1.500");
  });

  test("creates the budget from the form with the procedure type's template items", async ({ page }) => {
    // GIVEN: a person and a procedure type with a configured template
    const lastName = `Template-${Date.now()}`;
    const personResult = await createPerson(page, { lastName });
    expect(personResult.ok).toBe(true);
    const procedureTypeName = `Create type E2E ${Date.now()}`;
    const procedureTypeResult = await createProcedureType(page, { name: procedureTypeName });
    expect(procedureTypeResult.ok).toBe(true);
    const conceptResult = await createConcept(page, { name: "Honorarios Create E2E", value: 2500 });
    expect(conceptResult.ok).toBe(true);
    const templateResult = await createBudgetTemplate(
      page,
      procedureTypeResult.data!.idProcedureType,
      conceptResult.data!.idConcept,
    );
    expect(templateResult.ok, `createBudgetTemplate failed: ${templateResult.error}`).toBe(true);

    // WHEN: the operator creates the budget choosing client, amount, and procedure type
    await steps.givenUserIsOnPage("/dashboard/budgets");
    await page.getByTestId("btn-new-budget").click();
    await page.getByTestId("select-person").fill(lastName);
    await page.getByRole("option", { name: new RegExp(lastName) }).click();
    await page.locator('input[type="date"]').fill(new Date().toISOString().split("T")[0]);
    await page.getByTestId("input-amount").fill("100000");
    await page.getByTestId("select-procedure-type-new").click();
    await page.getByRole("option", { name: procedureTypeName }).click();
    await page.getByTestId("btn-save-budget").click();

    // THEN: the template items are already in the new budget breakdown
    const row = page.getByRole("row").filter({ hasText: lastName }).first();
    await expect(row).toBeVisible({ timeout: 8000 });
    await row.getByRole("button", { name: /ítems|items/i }).click();
    const dialog = page.getByTestId("dialog-items-budget");
    await expect(dialog.getByTestId("table-items-budget")).toContainText("Honorarios Create E2E", { timeout: 8000 });
  });

  test("shows an error when the procedure type has no template configured", async ({ page }) => {
    const personResult = await createPerson(page);
    expect(personResult.ok).toBe(true);
    const budgetResult = await createBudget(page, personResult.data!.personId);
    expect(budgetResult.ok).toBe(true);
    const idBudget = budgetResult.data!.idBudget;

    const procedureTypeName = `No template E2E ${Date.now()}`;
    const procedureTypeResult = await createProcedureType(page, { name: procedureTypeName });
    expect(procedureTypeResult.ok).toBe(true);

    await steps.givenUserIsOnPage("/dashboard/budgets");
    const itemsBtn = page.getByTestId(`btn-items-budget-${idBudget}`);
    await expect(itemsBtn).toBeVisible({ timeout: 8000 });
    await itemsBtn.click();

    const dialog = page.getByTestId("dialog-items-budget");
    await expect(dialog).toBeVisible();

    await dialog.getByTestId("select-procedure-type-items").click();
    // Pick the procedure type this test created (exact name): the #1495 rename changed the seeded
    // name to English, so the old /Sin Plantilla E2E/ pattern only matched leftovers on old dev DBs.
    await page.getByRole("option", { name: procedureTypeName, exact: true }).click();
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
