/**
 * Playwright E2E — budget status vocabulary (#1346, RF-02/RF-06).
 * The UI hardcoded BORRADOR/APROBADO/RECHAZADO/FACTURADO, so "Pendiente" budgets
 * could not be filtered and their edit dialog showed a blank status.
 */
import { test, expect } from "@playwright/test";
import { establishAdminBrowserSession } from "./setup/auth";
import { createPersona, createPresupuesto } from "./setup/api-helpers";

test.describe("Budget status vocabulary (#1346)", () => {
  test("a pending budget can be filtered, shows a translated status and keeps it in the edit dialog", async ({ page }) => {
    await establishAdminBrowserSession(page);
    const person = await createPersona(page, { isClient: true });
    expect(person.ok, person.error).toBe(true);
    // Any letter case is accepted and stored as the canonical code.
    const budget = await createPresupuesto(page, person.data!.personId, undefined, { status: "Pendiente" });
    expect(budget.ok, budget.error).toBe(true);
    const id = budget.data!.idBudget;

    const searched = page.waitForRequest((r) => /\/presupuestos\/buscar\?status=PENDIENTE$/.test(r.url()));
    await page.goto("/dashboard/presupuestos");
    await page.getByTestId("select-estado").click();
    await page.getByRole("option", { name: "Pendiente", exact: true }).click();
    await searched;
    const editButton = page.getByTestId(`btn-editar-presupuesto-${id}`);
    await expect(editButton).toBeVisible({ timeout: 10000 });
    const row = page.getByRole("row").filter({ has: editButton });
    await expect(row).toContainText("Pendiente");
    await expect(row).not.toContainText("PENDIENTE");

    await editButton.click();
    const dialog = page.getByRole("dialog");
    await expect(dialog.getByTestId("select-estado-presupuesto")).toHaveText("Pendiente");
  });

  test("the status filter offers every status of the vocabulary", async ({ page }) => {
    await establishAdminBrowserSession(page);
    await page.goto("/dashboard/presupuestos");
    await page.getByTestId("select-estado").click();
    const options = page.getByRole("listbox").getByRole("option");
    await expect(options).toHaveText(["Todos", "Borrador", "Pendiente", "Aprobado", "Rechazado", "Facturado"]);
  });
});
