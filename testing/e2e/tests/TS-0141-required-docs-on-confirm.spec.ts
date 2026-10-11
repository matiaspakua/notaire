/**
 * #773 remaining — Required documents are seeded when a procedure is confirmed via complete-case.
 *
 * Setup creates a procedure template before confirm so the checklist materializes; the management
 * case-summary dialog then lists the pending required document names (CU02 / CU03 / CU72).
 */
import { test, expect } from "@playwright/test";
import { establishAdminBrowserSession } from "./setup/auth";
import {
  createPerson,
  createBudget,
  createCompleteCaseManagement,
  createProcedureType,
  createDocumentType,
  createProcedureTemplate,
  createManagementStatus,
  uniqueId,
} from "./setup/api-helpers";

test.describe("#773 - Required documents seeded on complete-case", () => {
  test.beforeEach(async ({ page }) => {
    await establishAdminBrowserSession(page);
  });

  test("case summary lists pending required docs after complete-case", async ({ page }) => {
    const notary = await createPerson(page, { isClient: false });
    expect(notary.ok, notary.error ?? "createPerson failed").toBe(true);
    const budget = await createBudget(page, notary.data!.personId);
    expect(budget.ok, budget.error ?? "createBudget failed").toBe(true);
    const status = await createManagementStatus(page);
    expect(status.ok, status.error ?? "createManagementStatus failed").toBe(true);
    const procedureType = await createProcedureType(page);
    expect(procedureType.ok, procedureType.error ?? "createProcedureType failed").toBe(true);

    const typeName = `Required Doc E2E ${uniqueId()}`;
    const documentType = await createDocumentType(page, { name: typeName, expires: false });
    expect(documentType.ok, documentType.error ?? "createDocumentType failed").toBe(true);
    const template = await createProcedureTemplate(
      page,
      procedureType.data!.idProcedureType,
      documentType.data!.idDocumentType,
    );
    expect(template.ok, template.error ?? "createProcedureTemplate failed").toBe(true);

    const management = await createCompleteCaseManagement(page, {
      budgetId: budget.data!.idBudget,
      notaryId: notary.data!.personId,
      statusManagementId: status.data!.idManagementStatus,
      procedureTypeId: procedureType.data!.idProcedureType,
    });
    expect(management.ok, management.error ?? "complete-case failed").toBe(true);

    await page.goto("/dashboard/managements");
    await page.getByTestId(`btn-summary-case-${management.data!.idManagement}`).click();
    const dialog = page.getByTestId("dialog-summary-case");
    await expect(dialog.getByTestId("summary-document")).toContainText(typeName, { timeout: 15000 });
  });
});
