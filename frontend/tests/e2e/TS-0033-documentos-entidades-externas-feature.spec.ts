/**
 * E2E tests — Registrar movimientos de documentación de entidades externas (CU10).
 */
import { type Page, test, expect } from "@playwright/test";
import { authenticateAsAdmin } from "./setup/auth";
import {
  createPersona,
  createPresupuesto,
  createCompleteCaseGestion,
  createTipoTramite,
  createTramite,
  createDocumentoEntidadExterna,
  createEstadoGestion,
} from "./setup/api-helpers";

async function seedGestionConDocumentoEntidadExterna(page: Page, nombreDocumento: string) {
  const persona = await createPersona(page);
  const presupuesto = await createPresupuesto(page, persona.data!.idPersona);
  const estado = await createEstadoGestion(page);
  const tipoTramite = await createTipoTramite(page);
  const gestion = await createCompleteCaseGestion(page, {
    presupuestoId: presupuesto.data!.idPresupuesto,
    escribanoId: persona.data!.idPersona,
    estadoGestionId: estado.data!.idEstadoGestion,
    tipoTramiteId: tipoTramite.data!.idTipoDeTramite,
  });
  const tramite = await createTramite(page, gestion.data!.idGestion, tipoTramite.data!.idTipoDeTramite);
  const documento = await createDocumentoEntidadExterna(page, tramite.data!.idTramite, {
    nombre: nombreDocumento,
  });

  return {
    idGestion: gestion.data!.idGestion,
    numero: gestion.data!.numero,
    idDocumentoPresentado: documento.data!.idDocumentoPresentado,
  };
}

test.describe("CU10 - Registrar movimientos de documentación de entidades externas", () => {
  test.beforeEach(async ({ page }) => {
    await authenticateAsAdmin(page);
  });

  test("golden path: registering a movement updates the document and shows a success toast", async ({ page }) => {
    const { idGestion, numero, idDocumentoPresentado } = await seedGestionConDocumentoEntidadExterna(
      page,
      "Certificado de Dominio",
    );

    await page.goto("/dashboard/documentos-entidades-externas");
    await page.waitForLoadState("domcontentloaded");

    const row = page.getByRole("row", { name: new RegExp(String(numero)) });
    await expect(row).toBeVisible({ timeout: 10000 });
    await page.getByTestId(`btn-ver-documentos-${idGestion}`).click();

    const detalleDialog = page.getByRole("dialog");
    await expect(detalleDialog).toBeVisible();
    await expect(detalleDialog.getByText("Certificado de Dominio")).toBeVisible();

    await page.getByTestId(`btn-editar-documento-${idDocumentoPresentado}`).click();
    const movimientoDialog = page.getByTestId("dialog-movimiento");
    await expect(movimientoDialog).toBeVisible();

    await movimientoDialog.getByTestId("input-numero-carton").fill("42");
    await movimientoDialog.getByTestId("input-observaciones").fill("Retirado del registro");
    await movimientoDialog.getByTestId("checkbox-entregado").click();
    await movimientoDialog.getByTestId("btn-guardar-movimiento").click();

    await expect(page.getByText(/movimiento registrado/i)).toBeVisible({ timeout: 10000 });
    await expect(movimientoDialog).not.toBeVisible();
  });

  test("edge path: a gestión without entidad externa documents shows the empty state", async ({ page }) => {
    const persona = await createPersona(page);
    const presupuesto = await createPresupuesto(page, persona.data!.idPersona);
    const estado = await createEstadoGestion(page);
    const tipoTramite = await createTipoTramite(page);
    const gestion = await createCompleteCaseGestion(page, {
      presupuestoId: presupuesto.data!.idPresupuesto,
      escribanoId: persona.data!.idPersona,
      estadoGestionId: estado.data!.idEstadoGestion,
      tipoTramiteId: tipoTramite.data!.idTipoDeTramite,
    });

    await page.goto("/dashboard/documentos-entidades-externas");
    await page.waitForLoadState("domcontentloaded");

    const row = page.getByRole("row", { name: new RegExp(String(gestion.data!.numero)) });
    await expect(row).toBeVisible({ timeout: 10000 });
    await page.getByTestId(`btn-ver-documentos-${gestion.data!.idGestion}`).click();

    const detalleDialog = page.getByRole("dialog");
    await expect(detalleDialog).toBeVisible();
    await expect(detalleDialog.getByText(/no hay documentos/i)).toBeVisible();
  });
});
