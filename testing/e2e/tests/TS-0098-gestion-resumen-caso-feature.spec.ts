/**
 * #774 - Resumen del caso de una gestión.
 *
 * A gestión whose trámite has a signed deed with a generated testimony is seeded through the API;
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

test.describe("#774 - Resumen del caso", () => {
  test.beforeEach(async ({ page }) => {
    await establishAdminBrowserSession(page);
  });

  test("lists the deed and the testimony state of a management", async ({ page }) => {
    const notary = await createPerson(page, { isClient: false });
    expect(notary.ok, notary.error ?? "createPerson failed").toBe(true);
    const management = await apiPost<{ idManagement: number }>(page, "/managements", {
      heading: `Gestión resumen E2E ${uniqueId()}`,
      dateStart: new Date().toISOString().split("T")[0],
      number: uniqueId() % 1_000_000,
      notaryPersonId: notary.data!.personId,
    });
    expect(management.ok, management.error ?? "create gestión failed").toBe(true);
    const idManagement = management.data!.idManagement;

    const deedNumber = uniqueId() % 1_000_000;
    const deed = await apiPost<{ idDeed: number }>(page, "/deeds", {
      number: deedNumber,
      dateDeedrecording: new Date().toISOString().split("T")[0],
      body: `Deed resumen E2E ${deedNumber}`,
      status: "Firmada",
    });
    expect(deed.ok, deed.error ?? "escritura failed").toBe(true);
    const testimony = await apiPost(page, `/testimonies/${deed.data!.idDeed}/generate`, {});
    expect(testimony.ok, testimony.error ?? "generar testimonio failed").toBe(true);

    const tipo = await createProcedureType(page);
    expect(tipo.ok, tipo.error ?? "createProcedureType failed").toBe(true);
    const procedure = await apiPost(page, "/procedures", {
      idProcedureType: tipo.data!.idProcedureType,
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
