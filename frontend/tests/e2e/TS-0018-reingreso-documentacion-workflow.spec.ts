/**
 * E2E tests — Reingresar documentación (CU43).
 */
import { type Page, test, expect } from "@playwright/test";
import { authenticateAsAdmin } from "./setup/auth";
import {
  createPersona,
  createPresupuesto,
  createCompleteCaseGestion,
  createGestionSinTramite,
  createTipoTramite,
  createTipoDocumento,
  createPlantillaTramite,
  createEstadoGestion,
  getReingresoDocumentacion,
} from "./setup/api-helpers";

async function seedGestionConDocumentacionNecesaria(page: Page, nombreDocumento: string) {
  const persona = await createPersona(page);
  const presupuesto = await createPresupuesto(page, persona.data!.idPersona);
  const estado = await createEstadoGestion(page);
  const tipoTramite = await createTipoTramite(page);
  // `complete-case` creates the gestión AND its single trámite in one call;
  // creating a second trámite for the same tipoTramite here would duplicate
  // the "Certificado de Dominio" row and break the dialog's strict-mode lookup.
  const gestion = await createCompleteCaseGestion(page, {
    presupuestoId: presupuesto.data!.idPresupuesto,
    escribanoId: persona.data!.idPersona,
    estadoGestionId: estado.data!.idEstadoGestion,
    tipoTramiteId: tipoTramite.data!.idTipoDeTramite,
  });
  const tipoDocumento = await createTipoDocumento(page, { nombre: nombreDocumento });
  await createPlantillaTramite(page, tipoTramite.data!.idTipoDeTramite, tipoDocumento.data!.idTipoDocumento);
  const reingreso = await getReingresoDocumentacion(page, gestion.data!.idGestion);
  const idTramite = reingreso.data!.tramites[0].idTramite;

  return {
    idGestion: gestion.data!.idGestion,
    numero: gestion.data!.numero,
    idTramite,
    idTipoDocumento: tipoDocumento.data!.idTipoDocumento,
  };
}

test.describe("CU43 - Reingresar documentación", () => {
  test.beforeEach(async ({ page }) => {
    await authenticateAsAdmin(page);
  });

  test("golden path: reingresar a document shows a success toast", async ({ page }) => {
    const { idGestion, numero, idTramite, idTipoDocumento } = await seedGestionConDocumentacionNecesaria(
      page,
      "Certificado de Dominio",
    );

    await page.goto("/dashboard/reingreso-documentacion");
    await page.waitForLoadState("domcontentloaded");

    const row = page.getByRole("row", { name: new RegExp(String(numero)) });
    await expect(row).toBeVisible({ timeout: 10000 });
    await page.getByTestId(`btn-ver-tramites-${idGestion}`).click();

    const detalleDialog = page.getByRole("dialog");
    await expect(detalleDialog).toBeVisible();
    await expect(detalleDialog.getByText("Certificado de Dominio")).toBeVisible();

    await page.getByTestId(`btn-reingresar-${idTramite}-${idTipoDocumento}`).click();

    await expect(page.getByText(/documento reingresado/i)).toBeVisible({ timeout: 10000 });
  });

  test("edge path: a gestión without trámites shows the empty state", async ({ page }) => {
    const persona = await createPersona(page);
    const gestion = await createGestionSinTramite(page, persona.data!.idPersona);

    await page.goto("/dashboard/reingreso-documentacion");
    await page.waitForLoadState("domcontentloaded");

    const row = page.getByRole("row", { name: new RegExp(String(gestion.data!.numero)) });
    await expect(row).toBeVisible({ timeout: 10000 });
    await page.getByTestId(`btn-ver-tramites-${gestion.data!.idGestion}`).click();

    const detalleDialog = page.getByRole("dialog");
    await expect(detalleDialog).toBeVisible();
    await expect(detalleDialog.getByText(/no tiene trámites/i)).toBeVisible();
  });
});
