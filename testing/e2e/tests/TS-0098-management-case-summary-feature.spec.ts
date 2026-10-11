/**
 * #774 - Management case summary.
 *
 * A management whose procedure has a signed deed with a generated testimony is seeded through the API;
 * the case summary dialog of the managements screen must list the deed and the testimony state.
 */
import { test, expect } from "@playwright/test";
import { establishAdminBrowserSession } from "./setup/auth";
import {
  apiPost,
  createPerson,
  createProcedureType,
  uniqueId,
} from "./setup/api-helpers";

test.describe("#774 - Case summary", () => {
  test.beforeEach(async ({ page }) => {
    await establishAdminBrowserSession(page);
  });

  test("lists the deed and the testimony state of a management", async ({ page }) => {
    const notary = await createPerson(page, { isClient: false });
    expect(notary.ok, notary.error ?? "createPerson failed").toBe(true);
    const management = await apiPost<{ idManagement: number }>(page, "/managements", {
      heading: `Management summary E2E ${uniqueId()}`,
      dateStart: new Date().toISOString().split("T")[0],
      number: uniqueId() % 1_000_000,
      notaryPersonId: notary.data!.personId,
    });
    expect(management.ok, management.error ?? "create management failed").toBe(true);
    const idManagement = management.data!.idManagement;

    const deedNumber = uniqueId() % 1_000_000;
    const deed = await apiPost<{ idDeed: number }>(page, "/deeds", {
      number: deedNumber,
      dateDeedrecording: new Date().toISOString().split("T")[0],
      body: `Deed summary E2E ${deedNumber}`,
      status: "Firmada",
    });
    expect(deed.ok, deed.error ?? "deed failed").toBe(true);
    const testimony = await apiPost(page, `/testimonies/${deed.data!.idDeed}/generate`, {});
    expect(testimony.ok, testimony.error ?? "generate testimony failed").toBe(true);

    const procedureType = await createProcedureType(page);
    expect(procedureType.ok, procedureType.error ?? "createProcedureType failed").toBe(true);
    const procedure = await apiPost(page, "/procedures", {
      idProcedureType: procedureType.data!.idProcedureType,
      idManagement: idManagement,
      idDeed: deed.data!.idDeed,
    });
    expect(procedure.ok, `createProcedure ${procedure.status}`).toBe(true);

    await page.goto("/dashboard/managements");
    await page.getByTestId(`btn-summary-case-${idManagement}`).click();

    const dialog = page.getByTestId("dialog-summary-case");
    await expect(dialog.getByTestId("summary-deed")).toContainText(String(deedNumber), { timeout: 15000 });
    await expect(dialog.getByTestId("summary-testimony")).toContainText(/sin ingresar|not filed/i);
    await expect(dialog.getByTestId("summary-payments")).toBeVisible();
  });
});
