/**
 * E2E tests — Registrar movimientos de documentación de entidades externas (CU10).
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

async function seedManagementWithExternalEntityDocument(page: Page, nombreDocumento: string) {
  const persona = await createPerson(page);
  const presupuesto = await createBudget(page, persona.data!.personId);
  const estado = await createManagementStatus(page);
  const tipoTramite = await createProcedureType(page);
  const gestion = await createCompleteCaseManagement(page, {
    budgetId: presupuesto.data!.idBudget,
    notaryId: persona.data!.personId,
    statusManagementId: estado.data!.idManagementStatus,
    procedureTypeId: tipoTramite.data!.idProcedureType,
  });
  const tramite = await createProcedure(page, gestion.data!.idManagement, tipoTramite.data!.idProcedureType);
  const documento = await createExternalEntityDocument(page, tramite.data!.idProcedure, {
    name: nombreDocumento,
  });

  return {
    idGestion: gestion.data!.idManagement,
    numero: gestion.data!.number,
    idDocumentoPresentado: documento.data!.idSubmittedDocument,
  };
}

test.describe("CU10 - Registrar movimientos de documentación de entidades externas", () => {
  test.beforeEach(async ({ page }) => {
    await authenticateAsAdmin(page);
  });

  test("golden path: registering a movement updates the document and shows a success toast", async ({ page }) => {
    const { idGestion, numero, idDocumentoPresentado } = await seedManagementWithExternalEntityDocument(
      page,
      "Certificado de Dominio",
    );

    await page.goto("/dashboard/external-entity-documents");
    await page.waitForLoadState("domcontentloaded");

    const row = page.getByRole("row", { name: new RegExp(String(numero)) });
    await expect(row).toBeVisible({ timeout: 10000 });
    await page.getByTestId(`btn-view-documents-${idGestion}`).click();

    const detalleDialog = page.getByRole("dialog");
    await expect(detalleDialog).toBeVisible();
    await expect(detalleDialog.getByText("Certificado de Dominio")).toBeVisible();

    await page.getByTestId(`btn-edit-document-${idDocumentoPresentado}`).click();
    const movimientoDialog = page.getByTestId("dialog-movimiento");
    await expect(movimientoDialog).toBeVisible();

    await movimientoDialog.getByTestId("input-card-number").fill("42");
    await movimientoDialog.getByTestId("input-notes").fill("Retirado del registro");
    await movimientoDialog.getByTestId("checkbox-delivered").click();
    await movimientoDialog.getByTestId("btn-save-movimiento").click();

    await expect(page.getByText(/movimiento registrado/i)).toBeVisible({ timeout: 10000 });
    await expect(movimientoDialog).not.toBeVisible();
  });

  test("edge path: a gestión without entidad externa documents shows the empty state", async ({ page }) => {
    const persona = await createPerson(page);
    const presupuesto = await createBudget(page, persona.data!.personId);
    const estado = await createManagementStatus(page);
    const tipoTramite = await createProcedureType(page);
    const gestion = await createCompleteCaseManagement(page, {
      budgetId: presupuesto.data!.idBudget,
      notaryId: persona.data!.personId,
      statusManagementId: estado.data!.idManagementStatus,
      procedureTypeId: tipoTramite.data!.idProcedureType,
    });

    await page.goto("/dashboard/external-entity-documents");
    await page.waitForLoadState("domcontentloaded");

    const row = page.getByRole("row", { name: new RegExp(String(gestion.data!.number)) });
    await expect(row).toBeVisible({ timeout: 10000 });
    await page.getByTestId(`btn-view-documents-${gestion.data!.idManagement}`).click();

    const detalleDialog = page.getByRole("dialog");
    await expect(detalleDialog).toBeVisible();
    await expect(detalleDialog.getByText(/no hay documentos/i)).toBeVisible();
  });
});
