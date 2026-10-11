/**
 * #773 - Record a client's documentation against a procedure and view it from the management.
 *
 * The Documents screen creates a document by choosing its type, a management, and one of its procedures; the
 * list shows the type and the linked procedure, and the management case summary lists the document.
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

test.describe("#773 - Document linked to a procedure", () => {
  test.beforeEach(async ({ page }) => {
    await establishAdminBrowserSession(page);
  });

  test("creates a document for a procedure and lists it in the case summary", async ({ page }) => {
    const notary = await createPerson(page, { isClient: false });
    expect(notary.ok, notary.error ?? "createPerson failed").toBe(true);
    const numberManagement = uniqueId() % 1_000_000;
    const management = await apiPost<{ idManagement: number }>(page, "/managements", {
      heading: `Management documents E2E ${numberManagement}`,
      dateStart: new Date().toISOString().split("T")[0],
      number: numberManagement,
      notaryPersonId: notary.data!.personId,
    });
    expect(management.ok, management.error ?? "create management failed").toBe(true);
    const procedureType = await createProcedureType(page);
    expect(procedureType.ok, procedureType.error ?? "createProcedureType failed").toBe(true);
    const procedure = await createProcedure(page, management.data!.idManagement, procedureType.data!.idProcedureType);
    expect(procedure.ok, `createProcedure ${procedure.status}`).toBe(true);
    const typeName = `Certificado E2E ${uniqueId()}`;
    const documentType = await createDocumentType(page, { name: typeName, expires: false });
    expect(documentType.ok, documentType.error ?? "createDocumentType failed").toBe(true);

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
