/**
 * CU42 - Report upcoming expirations (issue #802).
 *
 * A document type that expires after 10 days and a document submitted today are
 * seeded through the API; the screen must list the document inside the default
 * 30-day window and stop listing it when the window is narrowed to 7 days.
 */
import { test, expect } from "@playwright/test";
import { establishAdminBrowserSession } from "./setup/auth";
import { apiPost, createDocumentType, createProcedureType, uniqueId } from "./setup/api-helpers";

test.describe("CU42 - Upcoming expirations", () => {
  test.beforeEach(async ({ page }) => {
    await establishAdminBrowserSession(page);
  });

  test("lists a document due in 10 days and hides it when the window is 7 days", async ({ page }) => {
    const documentType = await createDocumentType(page, { expires: true, dueDays: 10 });
    expect(documentType.ok, documentType.error ?? "createDocumentType failed").toBe(true);

    // A document is presented for a procedure (CU04, #655): procedureId is required on create.
    const procedureType = await createProcedureType(page);
    expect(procedureType.ok, procedureType.error ?? "createProcedureType failed").toBe(true);
    const procedure = await apiPost<{ idProcedure: number }>(page, "/procedures", {
      idProcedureType: procedureType.data!.idProcedureType,
    });
    expect(procedure.ok, procedure.error ?? "create procedure failed").toBe(true);

    const name = `Expiration E2E ${uniqueId()}`;
    const document = await apiPost<{ idSubmittedDocument: number }>(page, "/submitted-documents", {
      typeId: documentType.data!.idDocumentType,
      procedureId: procedure.data!.idProcedure,
      date: new Date().toISOString().split("T")[0],
      delivered: false,
      name,
    });
    expect(document.ok, document.error ?? "submitted document failed").toBe(true);

    await page.goto("/dashboard/upcoming-expirations");
    const table = page.getByRole("table");
    await expect(table.getByText(name)).toBeVisible({ timeout: 15000 });

    await page.getByTestId("select-expiration-window").click();
    await page.getByRole("option", { name: /^7 / }).click();
    await expect(table.getByText(name)).toHaveCount(0);
  });
});
