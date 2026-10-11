/**
 * E2E tests — Reenter documentation (CU43).
 */
import { type Page, test, expect } from "@playwright/test";
import { authenticateAsAdmin } from "./setup/auth";
import {
  createPerson,
  createBudget,
  createCompleteCaseManagement,
  createManagementWithoutProcedure,
  createProcedureType,
  createDocumentType,
  createProcedureTemplate,
  createManagementStatus,
  getDocumentReentry,
} from "./setup/api-helpers";

async function seedManagementWithRequiredDocumentation(page: Page, documentName: string) {
  const person = await createPerson(page);
  const budget = await createBudget(page, person.data!.personId);
  const status = await createManagementStatus(page);
  const procedureType = await createProcedureType(page);
  // `complete-case` creates the management and its single procedure in one call;
  // creating a second procedure for the same procedure type here would duplicate
  // the "Certificado de Dominio" row and break the dialog's strict-mode lookup.
  const management = await createCompleteCaseManagement(page, {
    budgetId: budget.data!.idBudget,
    notaryId: person.data!.personId,
    statusManagementId: status.data!.idManagementStatus,
    procedureTypeId: procedureType.data!.idProcedureType,
  });
  const documentType = await createDocumentType(page, { name: documentName });
  await createProcedureTemplate(page, procedureType.data!.idProcedureType, documentType.data!.idDocumentType);
  const reentry = await getDocumentReentry(page, management.data!.idManagement);
  const idProcedure = reentry.data!.procedures[0].idProcedure;

  return {
    idManagement: management.data!.idManagement,
    number: management.data!.number,
    idProcedure,
    idDocumentType: documentType.data!.idDocumentType,
  };
}

test.describe("CU43 - Re-enter documentation", () => {
  test.beforeEach(async ({ page }) => {
    await authenticateAsAdmin(page);
  });

  test("golden path: re-entering a document shows a success toast", async ({ page }) => {
    const { idManagement, number, idProcedure, idDocumentType } = await seedManagementWithRequiredDocumentation(
      page,
      "Certificado de Dominio",
    );

    await page.goto("/dashboard/document-reentry");
    await page.waitForLoadState("domcontentloaded");

    const row = page.getByRole("row", { name: new RegExp(String(number)) });
    await expect(row).toBeVisible({ timeout: 10000 });
    await page.getByTestId(`btn-view-procedures-${idManagement}`).click();

    const detailDialog = page.getByRole("dialog");
    await expect(detailDialog).toBeVisible();
    await expect(detailDialog.getByText("Certificado de Dominio")).toBeVisible();

    await page.getByTestId(`btn-reenter-${idProcedure}-${idDocumentType}`).click();

    await expect(page.getByText(/documento reingresado/i)).toBeVisible({ timeout: 10000 });
  });

  test("edge path: a management without procedures shows the empty state", async ({ page }) => {
    const person = await createPerson(page);
    const management = await createManagementWithoutProcedure(page, person.data!.personId);

    await page.goto("/dashboard/document-reentry");
    await page.waitForLoadState("domcontentloaded");

    const row = page.getByRole("row", { name: new RegExp(String(management.data!.number)) });
    await expect(row).toBeVisible({ timeout: 10000 });
    await page.getByTestId(`btn-view-procedures-${management.data!.idManagement}`).click();

    const detailDialog = page.getByRole("dialog");
    await expect(detailDialog).toBeVisible();
    await expect(detailDialog.getByText(/no tiene trámites/i)).toBeVisible();
  });
});
