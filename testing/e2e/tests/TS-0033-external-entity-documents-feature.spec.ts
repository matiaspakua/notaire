/**
 * E2E tests — Record external-entity document movements (CU10).
 */
import { type Page, test, expect } from "@playwright/test";
import { authenticateAsAdmin } from "./setup/auth";
import {
  createPerson,
  createBudget,
  createCompleteCaseManagement,
  createProcedureType,
  createProcedure,
  createExternalEntityDocument,
  createManagementStatus,
} from "./setup/api-helpers";

async function seedManagementWithExternalEntityDocument(page: Page, documentName: string) {
  const person = await createPerson(page);
  const budget = await createBudget(page, person.data!.personId);
  const managementStatus = await createManagementStatus(page);
  const procedureType = await createProcedureType(page);
  const management = await createCompleteCaseManagement(page, {
    budgetId: budget.data!.idBudget,
    notaryId: person.data!.personId,
    statusManagementId: managementStatus.data!.idManagementStatus,
    procedureTypeId: procedureType.data!.idProcedureType,
  });
  const procedure = await createProcedure(page, management.data!.idManagement, procedureType.data!.idProcedureType);
  const document = await createExternalEntityDocument(page, procedure.data!.idProcedure, {
    name: documentName,
  });

  return {
    idManagement: management.data!.idManagement,
    number: management.data!.number,
    idSubmittedDocument: document.data!.idSubmittedDocument,
  };
}

test.describe("CU10 - Record movements of external-entity documentation", () => {
  test.beforeEach(async ({ page }) => {
    await authenticateAsAdmin(page);
  });

  test("golden path: registering a movement updates the document and shows a success toast", async ({ page }) => {
    const { idManagement, number, idSubmittedDocument } = await seedManagementWithExternalEntityDocument(
      page,
      "Certificado de Dominio",
    );

    await page.goto("/dashboard/external-entity-documents");
    await page.waitForLoadState("domcontentloaded");

    const row = page.getByRole("row", { name: new RegExp(String(number)) });
    await expect(row).toBeVisible({ timeout: 10000 });
    await page.getByTestId(`btn-view-documents-${idManagement}`).click();

    const detailDialog = page.getByRole("dialog");
    await expect(detailDialog).toBeVisible();
    await expect(detailDialog.getByText("Certificado de Dominio")).toBeVisible();

    await page.getByTestId(`btn-edit-document-${idSubmittedDocument}`).click();
    const movementDialog = page.getByTestId("dialog-movement");
    await expect(movementDialog).toBeVisible();

    await movementDialog.getByTestId("input-card-number").fill("42");
    await movementDialog.getByTestId("input-notes").fill("Withdrawn from the registry");
    await movementDialog.getByTestId("checkbox-delivered").click();
    await movementDialog.getByTestId("btn-save-movement").click();

    await expect(page.getByText(/movimiento registrado/i)).toBeVisible({ timeout: 10000 });
    await expect(movementDialog).not.toBeVisible();
  });

  test("edge path: a management without external-entity documents shows the empty state", async ({ page }) => {
    const person = await createPerson(page);
    const budget = await createBudget(page, person.data!.personId);
    const managementStatus = await createManagementStatus(page);
    const procedureType = await createProcedureType(page);
    const management = await createCompleteCaseManagement(page, {
      budgetId: budget.data!.idBudget,
      notaryId: person.data!.personId,
      statusManagementId: managementStatus.data!.idManagementStatus,
      procedureTypeId: procedureType.data!.idProcedureType,
    });

    await page.goto("/dashboard/external-entity-documents");
    await page.waitForLoadState("domcontentloaded");

    const row = page.getByRole("row", { name: new RegExp(String(management.data!.number)) });
    await expect(row).toBeVisible({ timeout: 10000 });
    await page.getByTestId(`btn-view-documents-${management.data!.idManagement}`).click();

    const detailDialog = page.getByRole("dialog");
    await expect(detailDialog).toBeVisible();
    await expect(detailDialog.getByText(/no hay documentos/i)).toBeVisible();
  });
});
