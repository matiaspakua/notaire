/**
 * #774 - Resumen del caso de una gestión.
 *
 * A gestión whose trámite has a signed escritura with a generated testimonio is seeded through the API;
 * the case summary dialog of the gestiones screen must list the escritura and the testimonio state.
 */
import { test, expect } from "@playwright/test";
import { establishAdminBrowserSession } from "./setup/auth";
import {
  apiPost,
  createPersona,
  createTipoTramite,
  uniqueId,
} from "./setup/api-helpers";

test.describe("#774 - Resumen del caso", () => {
  test.beforeEach(async ({ page }) => {
    await establishAdminBrowserSession(page);
  });

  test("lists the escritura and the testimonio state of a gestión", async ({ page }) => {
    const escribano = await createPersona(page, { isClient: false });
    expect(escribano.ok, escribano.error ?? "createPersona failed").toBe(true);
    const gestion = await apiPost<{ idManagement: number }>(page, "/gestiones", {
      encabezado: `Gestión resumen E2E ${uniqueId()}`,
      dateStart: new Date().toISOString().split("T")[0],
      number: uniqueId() % 1_000_000,
      notaryPersonId: escribano.data!.personId,
    });
    expect(gestion.ok, gestion.error ?? "create gestión failed").toBe(true);
    const idGestion = gestion.data!.idManagement;

    const escrituraNumero = uniqueId() % 1_000_000;
    const escritura = await apiPost<{ idDeed: number }>(page, "/escrituras", {
      number: escrituraNumero,
      dateDeedrecording: new Date().toISOString().split("T")[0],
      body: `Escritura resumen E2E ${escrituraNumero}`,
      status: "Firmada",
    });
    expect(escritura.ok, escritura.error ?? "escritura failed").toBe(true);
    const testimonio = await apiPost(page, `/testimonio/${escritura.data!.idDeed}/generar`, {});
    expect(testimonio.ok, testimonio.error ?? "generar testimonio failed").toBe(true);

    const tipo = await createTipoTramite(page);
    expect(tipo.ok, tipo.error ?? "createTipoTramite failed").toBe(true);
    const tramite = await apiPost(page, "/tramites", {
      idProcedureType: tipo.data!.idProcedureType,
      idManagement: idGestion,
      idDeed: escritura.data!.idDeed,
    });
    expect(tramite.ok, `createTramite ${tramite.status}`).toBe(true);

    await page.goto("/dashboard/gestiones");
    await page.getByTestId(`btn-resumen-caso-${idGestion}`).click();

    const dialog = page.getByTestId("dialog-resumen-caso");
    await expect(dialog.getByTestId("resumen-escritura")).toContainText(String(escrituraNumero), { timeout: 15000 });
    await expect(dialog.getByTestId("resumen-testimonio")).toContainText(/sin ingresar|not filed/i);
    await expect(dialog.getByTestId("resumen-pagos")).toBeVisible();
  });
});
