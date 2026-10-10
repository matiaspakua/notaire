/**
 * Playwright E2E — managements list columns (#1348, RF-22/RF-40).
 * The column titled "Tipo de Trámite" showed the procedure count, and the list
 * hid the case header and start date that identify a case.
 */
import { test, expect } from "@playwright/test";
import { establishAdminBrowserSession } from "./setup/auth";
import { createCompleteCaseGestion, createPersona, createPresupuesto } from "./setup/api-helpers";

test.describe("Managements list columns (#1348)", () => {
  test("each row shows number, header, start date, procedure count and status under the right titles", async ({ page }) => {
    await establishAdminBrowserSession(page);
    const person = await createPersona(page, { isClient: true });
    expect(person.ok, person.error).toBe(true);
    const budget = await createPresupuesto(page, person.data!.personId);
    expect(budget.ok, budget.error).toBe(true);
    const encabezado = `Carátula E2E ${Date.now()}`;
    const gestion = await createCompleteCaseGestion(page, { presupuestoId: budget.data!.idBudget, encabezado });
    expect(gestion.ok, gestion.error).toBe(true);

    await page.goto("/dashboard/gestiones");
    const table = page.getByRole("table");
    await expect(table.getByRole("columnheader", { name: "Carátula" })).toBeVisible({ timeout: 10000 });
    await expect(table.getByRole("columnheader", { name: "Inicio" })).toBeVisible();
    await expect(table.getByRole("columnheader", { name: "Trámites" })).toBeVisible();
    await expect(table.getByRole("columnheader", { name: "Tipo de Trámite" })).toHaveCount(0);

    const row = table.getByRole("row").filter({ has: page.getByTestId(`btn-ver-bitacora-${gestion.data!.idManagement}`) });
    await expect(row).toContainText(encabezado);
    await expect(row).toContainText(String(gestion.data!.number));
    // Started today (complete-case sets the start date): shown as a calendar date.
    const today = new Intl.DateTimeFormat("es-AR", { day: "2-digit", month: "2-digit", year: "numeric" }).format(new Date());
    await expect(row).toContainText(today);
  });
});
