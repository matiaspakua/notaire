/**
 * CU42 - Informar próximos vencimientos (issue #802).
 *
 * A document type that expires after 10 days and a document submitted today are
 * seeded through the API; the screen must list the document inside the default
 * 30-day window and stop listing it when the window is narrowed to 7 days.
 */
import { test, expect } from "@playwright/test";
import { establishAdminBrowserSession } from "./setup/auth";
import { apiPost, createTipoDocumento, createTipoTramite, uniqueId } from "./setup/api-helpers";

test.describe("CU42 - Próximos vencimientos", () => {
  test.beforeEach(async ({ page }) => {
    await establishAdminBrowserSession(page);
  });

  test("lists a document due in 10 days and hides it when the window is 7 days", async ({ page }) => {
    const tipo = await createTipoDocumento(page, { expires: true, dueDays: 10 });
    expect(tipo.ok, tipo.error ?? "createTipoDocumento failed").toBe(true);

    // A document is presented for a trámite (CU04, #655): procedureId is required on create.
    const tipoTramite = await createTipoTramite(page);
    expect(tipoTramite.ok, tipoTramite.error ?? "createTipoTramite failed").toBe(true);
    const tramite = await apiPost<{ idProcedure: number }>(page, "/tramites", {
      idProcedureType: tipoTramite.data!.idProcedureType,
    });
    expect(tramite.ok, tramite.error ?? "create trámite failed").toBe(true);

    const name = `Vencimiento E2E ${uniqueId()}`;
    const documento = await apiPost<{ idSubmittedDocument: number }>(page, "/documento-presentado", {
      typeId: tipo.data!.idDocumentType,
      procedureId: tramite.data!.idProcedure,
      date: new Date().toISOString().split("T")[0],
      delivered: false,
      name,
    });
    expect(documento.ok, documento.error ?? "documento-presentado failed").toBe(true);

    await page.goto("/dashboard/proximos-vencimientos");
    const table = page.getByRole("table");
    await expect(table.getByText(name)).toBeVisible({ timeout: 15000 });

    await page.getByTestId("select-ventana-vencimientos").click();
    await page.getByRole("option", { name: /^7 / }).click();
    await expect(table.getByText(name)).toHaveCount(0);
  });
});
