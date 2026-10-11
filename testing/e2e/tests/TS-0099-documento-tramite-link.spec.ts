/**
 * #773 - Registrar documentación del client contra un trámite y verla desde la gestión.
 *
 * The Documents screen creates a document choosing its type, a gestión and one of its trámites; the
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
    const notary = await createPerson(page, { isClient: false });
    expect(notary.ok, notary.error ?? "createPerson failed").toBe(true);
    const numberManagement = uniqueId() % 1_000_000;
    const management = await apiPost<{ idManagement: number }>(page, "/managements", {
      heading: `Gestión documentos E2E ${numberManagement}`,
      dateStart: new Date().toISOString().split("T")[0],
      number: numberManagement,
      notaryPersonId: notary.data!.personId,
    });
    expect(management.ok, management.error ?? "create gestión failed").toBe(true);
    const typeProcedure = await createProcedureType(page);
    expect(typeProcedure.ok, typeProcedure.error ?? "createProcedureType failed").toBe(true);
    const procedure = await createProcedure(page, management.data!.idManagement, typeProcedure.data!.idProcedureType);
    expect(procedure.ok, `createProcedure ${procedure.status}`).toBe(true);
    const typeName = `Certificado E2E ${uniqueId()}`;
    const typeDocument = await createDocumentType(page, { name: typeName, expires: false });
    expect(typeDocument.ok, typeDocument.error ?? "createDocumentType failed").toBe(true);

    await page.goto("/dashboard/documents");
    await page.getByTestId("btn-new-document").click();
    await page.getByTestId("select-document-type").click();
    await page.getByRole("option", { name: typeName }).click();
    // Server-search management picker (#1340): type the case number, then pick it.
    await page.getByTestId("select-management-document").click();
    await page.getByTestId("select-management-document").fill(String(numberManagement));
    await page.getByRole("option", { name: new RegExp(`^${numberManagement} `) }).click();
    await page.getByTestId("select-procedure-document").click();
    await page.getByRole("listbox").getByRole("option").first().click();
    await page.getByTestId("btn-save-document").click();

    const row = page.getByRole("row").filter({ hasText: typeName }).first();
    await expect(row).toBeVisible({ timeout: 15000 });
    await expect(row).toContainText(`#${procedure.data!.idProcedure}`);

    await page.goto("/dashboard/managements");
    await page.getByTestId(`btn-summary-case-${management.data!.idManagement}`).click();
    const dialog = page.getByTestId("dialog-summary-case");
    await expect(dialog.getByTestId("summary-document")).toContainText(typeName, { timeout: 15000 });
  });
});
