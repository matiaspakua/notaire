/**
 * E2E tests — Reingresar documentación (CU43).
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

async function seedManagementWithRequiredDocumentation(page: Page, nombreDocumento: string) {
  const persona = await createPerson(page);
  const presupuesto = await createBudget(page, persona.data!.personId);
  const estado = await createManagementStatus(page);
  const tipoTramite = await createProcedureType(page);
  // `complete-case` creates the gestión AND its single trámite in one call;
  // creating a second trámite for the same tipoTramite here would duplicate
  // the "Certificado de Dominio" row and break the dialog's strict-mode lookup.
  const gestion = await createCompleteCaseManagement(page, {
    budgetId: presupuesto.data!.idBudget,
    notaryId: persona.data!.personId,
    statusManagementId: estado.data!.idManagementStatus,
    procedureTypeId: tipoTramite.data!.idProcedureType,
  });
  const tipoDocumento = await createDocumentType(page, { name: nombreDocumento });
  await createProcedureTemplate(page, tipoTramite.data!.idProcedureType, tipoDocumento.data!.idDocumentType);
  const reingreso = await getDocumentReentry(page, gestion.data!.idManagement);
  const idTramite = reingreso.data!.procedures[0].idProcedure;

  return {
    idGestion: gestion.data!.idManagement,
    numero: gestion.data!.number,
    idTramite,
    idTipoDocumento: tipoDocumento.data!.idDocumentType,
  };
}

test.describe("CU43 - Reingresar documentación", () => {
  test.beforeEach(async ({ page }) => {
    await authenticateAsAdmin(page);
  });

  test("golden path: reingresar a document shows a success toast", async ({ page }) => {
    const { idGestion, numero, idTramite, idTipoDocumento } = await seedManagementWithRequiredDocumentation(
      page,
      "Certificado de Dominio",
    );

    await page.goto("/dashboard/document-reentry");
    await page.waitForLoadState("domcontentloaded");

    const row = page.getByRole("row", { name: new RegExp(String(numero)) });
    await expect(row).toBeVisible({ timeout: 10000 });
    await page.getByTestId(`btn-view-procedures-${idGestion}`).click();

    const detalleDialog = page.getByRole("dialog");
    await expect(detalleDialog).toBeVisible();
    await expect(detalleDialog.getByText("Certificado de Dominio")).toBeVisible();

    await page.getByTestId(`btn-reenter-${idTramite}-${idTipoDocumento}`).click();

    await expect(page.getByText(/document reingresado/i)).toBeVisible({ timeout: 10000 });
  });

  test("edge path: a gestión without trámites shows the empty state", async ({ page }) => {
    const persona = await createPerson(page);
    const gestion = await createManagementWithoutProcedure(page, persona.data!.personId);

    await page.goto("/dashboard/document-reentry");
    await page.waitForLoadState("domcontentloaded");

    const row = page.getByRole("row", { name: new RegExp(String(gestion.data!.number)) });
    await expect(row).toBeVisible({ timeout: 10000 });
    await page.getByTestId(`btn-view-procedures-${gestion.data!.idManagement}`).click();

    const detalleDialog = page.getByRole("dialog");
    await expect(detalleDialog).toBeVisible();
    await expect(detalleDialog.getByText(/no tiene trámites/i)).toBeVisible();
  });
});
