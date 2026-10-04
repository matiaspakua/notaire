/**
 * #773 - Registrar documentación del cliente contra un trámite y verla desde la gestión.
 *
 * The Documentos screen creates a document choosing its type, a gestión and one of its trámites; the
 * list shows the type and the linked trámite, and the case summary of the gestión lists the document.
 */
import { test, expect } from "@playwright/test";
import { establishAdminBrowserSession } from "./setup/auth";
import {
  apiPost,
  createPersona,
  createTipoDocumento,
  createTipoTramite,
  createTramite,
  uniqueId,
} from "./setup/api-helpers";

test.describe("#773 - Documento vinculado a un trámite", () => {
  test.beforeEach(async ({ page }) => {
    await establishAdminBrowserSession(page);
  });

  test("creates a document for a trámite and lists it in the case summary", async ({ page }) => {
    const escribano = await createPersona(page, { isClient: false });
    expect(escribano.ok, escribano.error ?? "createPersona failed").toBe(true);
    const numeroGestion = uniqueId() % 1_000_000;
    const gestion = await apiPost<{ idManagement: number }>(page, "/gestiones", {
      encabezado: `Gestión documentos E2E ${numeroGestion}`,
      dateStart: new Date().toISOString().split("T")[0],
      number: numeroGestion,
      notaryPersonId: escribano.data!.personId,
    });
    expect(gestion.ok, gestion.error ?? "create gestión failed").toBe(true);
    const tipoTramite = await createTipoTramite(page);
    expect(tipoTramite.ok, tipoTramite.error ?? "createTipoTramite failed").toBe(true);
    const tramite = await createTramite(page, gestion.data!.idManagement, tipoTramite.data!.idProcedureType);
    expect(tramite.ok, `createTramite ${tramite.status}`).toBe(true);
    const tipoNombre = `Certificado E2E ${uniqueId()}`;
    const tipoDocumento = await createTipoDocumento(page, { name: tipoNombre, expires: false });
    expect(tipoDocumento.ok, tipoDocumento.error ?? "createTipoDocumento failed").toBe(true);

    await page.goto("/dashboard/documentos");
    await page.getByTestId("btn-nuevo-documento").click();
    await page.getByTestId("select-tipo-documento").click();
    await page.getByRole("option", { name: tipoNombre }).click();
    await page.getByTestId("select-gestion-documento").click();
    await page.getByRole("option", { name: new RegExp(String(numeroGestion)) }).click();
    await page.getByTestId("select-tramite-documento").click();
    await page.getByRole("option").first().click();
    await page.getByTestId("btn-guardar-documento").click();

    const row = page.getByRole("row").filter({ hasText: tipoNombre }).first();
    await expect(row).toBeVisible({ timeout: 15000 });
    await expect(row).toContainText(`#${tramite.data!.idProcedure}`);

    await page.goto("/dashboard/gestiones");
    await page.getByTestId(`btn-resumen-caso-${gestion.data!.idManagement}`).click();
    const dialog = page.getByTestId("dialog-resumen-caso");
    await expect(dialog.getByTestId("resumen-documento")).toContainText(tipoNombre, { timeout: 15000 });
  });
});
