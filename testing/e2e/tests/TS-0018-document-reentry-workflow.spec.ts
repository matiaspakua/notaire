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

async function seedManagementWithRequiredDocumentation(page: Page, nameDocument: string) {
  const person = await createPerson(page);
  const budget = await createBudget(page, person.data!.personId);
  const estado = await createManagementStatus(page);
  const typeProcedure = await createProcedureType(page);
  // `complete-case` creates the management and its single procedure in one call;
  // creating a second procedure for the same procedure type here would duplicate
  // the "Certificado de Dominio" row and break the dialog's strict-mode lookup.
  const management = await createCompleteCaseManagement(page, {
    budgetId: budget.data!.idBudget,
    notaryId: person.data!.personId,
    statusManagementId: estado.data!.idManagementStatus,
    procedureTypeId: typeProcedure.data!.idProcedureType,
  });
  const typeDocument = await createDocumentType(page, { name: nameDocument });
  await createProcedureTemplate(page, typeProcedure.data!.idProcedureType, typeDocument.data!.idDocumentType);
  const reingreso = await getDocumentReentry(page, management.data!.idManagement);
  const idProcedure = reingreso.data!.procedures[0].idProcedure;

  return {
    idManagement: management.data!.idManagement,
    number: management.data!.number,
    idProcedure,
    idTypeDocument: typeDocument.data!.idDocumentType,
  };
}

test.describe("CU43 - Re-enter documentation", () => {
  test.beforeEach(async ({ page }) => {
    await authenticateAsAdmin(page);
  });

  test("golden path: reingresar a document shows a success toast", async ({ page }) => {
    const { idManagement, number, idProcedure, idTypeDocument } = await seedManagementWithRequiredDocumentation(
      page,
      "Certificado de Dominio",
    );

    await page.goto("/dashboard/document-reentry");
    await page.waitForLoadState("domcontentloaded");

    const row = page.getByRole("row", { name: new RegExp(String(number)) });
    await expect(row).toBeVisible({ timeout: 10000 });
    await page.getByTestId(`btn-view-procedures-${idManagement}`).click();

    const detalleDialog = page.getByRole("dialog");
    await expect(detalleDialog).toBeVisible();
    await expect(detalleDialog.getByText("Certificado de Dominio")).toBeVisible();

    await page.getByTestId(`btn-reenter-${idProcedure}-${idTypeDocument}`).click();

    await expect(page.getByText(/document reingresado/i)).toBeVisible({ timeout: 10000 });
  });

  test("edge path: a management without procedures shows the empty state", async ({ page }) => {
    const person = await createPerson(page);
    const management = await createManagementWithoutProcedure(page, person.data!.personId);

    await page.goto("/dashboard/document-reentry");
    await page.waitForLoadState("domcontentloaded");

    const row = page.getByRole("row", { name: new RegExp(String(management.data!.number)) });
    await expect(row).toBeVisible({ timeout: 10000 });
    await page.getByTestId(`btn-view-procedures-${management.data!.idManagement}`).click();

    const detalleDialog = page.getByRole("dialog");
    await expect(detalleDialog).toBeVisible();
    await expect(detalleDialog.getByText(/no tiene trámites/i)).toBeVisible();
  });
});
