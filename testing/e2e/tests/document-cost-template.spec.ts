/**
 * Templates - Document costs (CU27/CU39)
 *
 * Covers: CU27 (enter a new document type), CU39 (create a budget template)
 * Test Level: E2E UI/UX
 *
 * Workflow:
 *   - Select a procedure type on the templates page
 *   - Define a fixed or variable cost for a document type
 *   - Verify the cost appears in the document costs table
 *
 * Reference: docs/openspec/changes/costos-documents-budget
 */
import { test, expect } from "@playwright/test";
import { GherkinSteps } from "./gherkin-helpers";
import { createProcedureType, createDocumentType } from "./setup/api-helpers";

test.describe("Plantillas - Costos de Documentos", () => {
  let steps: GherkinSteps;

  test.beforeEach(async ({ page }) => {
    steps = new GherkinSteps(page);
    await steps.givenUserIsLoggedIn();
    await steps.givenUserIsOnPage("/dashboard/administration/templates");
  });

  test("CU39-GW01: Given no procedure type is selected, Then the document-costs section is hidden", async ({ page }) => {
    await expect(page.getByTestId("btn-new-cost-document")).not.toBeVisible();
  });

  test("CU39-GW02: Given a procedure type is selected, Then the document-costs section appears", async ({ page }) => {
    await page.getByTestId("select-procedure-type-costs").click();
    await page.getByRole("listbox").getByRole("option").first().click();

    await expect(page.getByTestId("btn-new-cost-document")).toBeVisible();
  });

  test("CU27-GW01: Given costo form open, When creating a fixed cost, Then it is saved and listed", async ({ page }) => {
    // A fresh procedure type and document type pair avoids colliding with a
    // combination already created (unique constraint) by an earlier/parallel
    // run against the shared "first option" catalog entries.
    const runId = Date.now();
    const nameProcedure = `Tipo Procedure Costo E2E ${runId}`;
    const nameDocument = `Tipo Documento Costo E2E ${runId}`;
    const typeProcedureResult = await createProcedureType(page, { name: nameProcedure });
    expect(typeProcedureResult.ok).toBe(true);
    const typeDocumentResult = await createDocumentType(page, { name: nameDocument });
    expect(typeDocumentResult.ok).toBe(true);

    await page.reload();
    await page.getByTestId("select-procedure-type-costs").click();
    await page.getByRole("option", { name: nameProcedure }).click();

    await page.getByTestId("btn-new-cost-document").click();
    await steps.thenModalIsVisible();

    await page.getByTestId("select-document-type-cost").click();
    await page.getByRole("option", { name: nameDocument }).click();
    await page.getByTestId("input-value-cost-document").fill("1500");

    await page.getByTestId("btn-save-cost-document").click();

    await steps.thenShowsSuccessMessage("Costo creado");
  });
});
