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
  createPerson,
  createDocumentType,
  createProcedureType,
  createProcedure,
  uniqueId,
} from "./setup/api-helpers";

test.describe("#773 - Documento vinculado a un trámite", () => {
  test.beforeEach(async ({ page }) => {
    await establishAdminBrowserSession(page);
  });

  test("creates a document for a trámite and lists it in the case summary", async ({ page }) => {
    const escribano = await createPerson(page, { isClient: false });
    expect(escribano.ok, escribano.error ?? "createPerson failed").toBe(true);
    const numeroGestion = uniqueId() % 1_000_000;
    const gestion = await apiPost<{ idManagement: number }>(page, "/managements", {
      heading: `Gestión documentos E2E ${numeroGestion}`,
      dateStart: new Date().toISOString().split("T")[0],
      number: numeroGestion,
      notaryPersonId: escribano.data!.personId,
    });
    expect(gestion.ok, gestion.error ?? "create gestión failed").toBe(true);
    const tipoTramite = await createProcedureType(page);
    expect(tipoTramite.ok, tipoTramite.error ?? "createProcedureType failed").toBe(true);
    const tramite = await createProcedure(page, gestion.data!.idManagement, tipoTramite.data!.idProcedureType);
    expect(tramite.ok, `createProcedure ${tramite.status}`).toBe(true);
    const tipoNombre = `Certificado E2E ${uniqueId()}`;
    const tipoDocumento = await createDocumentType(page, { name: tipoNombre, expires: false });
    expect(tipoDocumento.ok, tipoDocumento.error ?? "createDocumentType failed").toBe(true);

    await page.goto("/dashboard/documents");
    await page.getByTestId("btn-new-document").click();
    await page.getByTestId("select-document-type").click();
    await page.getByRole("option", { name: tipoNombre }).click();
    // Server-search management picker (#1340): type the case number, then pick it.
    await page.getByTestId("select-management-document").click();
    await page.getByTestId("select-management-document").fill(String(numeroGestion));
    await page.getByRole("option", { name: new RegExp(`^${numeroGestion} `) }).click();
    await page.getByTestId("select-procedure-document").click();
    await page.getByRole("listbox").getByRole("option").first().click();
    await page.getByTestId("btn-save-document").click();

    const row = page.getByRole("row").filter({ hasText: tipoNombre }).first();
    await expect(row).toBeVisible({ timeout: 15000 });
    await expect(row).toContainText(`#${tramite.data!.idProcedure}`);

    await page.goto("/dashboard/managements");
    await page.getByTestId(`btn-summary-case-${gestion.data!.idManagement}`).click();
    const dialog = page.getByTestId("dialog-summary-case");
    await expect(dialog.getByTestId("summary-document")).toContainText(tipoNombre, { timeout: 15000 });
  });
});
