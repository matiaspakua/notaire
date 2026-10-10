/**
 * Playwright E2E — edit dialogs pre-fill the stored date (#1338, #1339).
 * RF-06 / RNF-08: `<input type="date">` only accepts yyyy-MM-dd; binding the
 * API's ISO instant left the field empty. Saving without changes must keep
 * the same calendar day in any browser zone.
 */
import { test, expect } from "@playwright/test";
import { establishAdminBrowserSession } from "./setup/auth";
import { createPago, createPersona, createPresupuesto } from "./setup/api-helpers";

const DAY = "2026-09-05";

for (const timezoneId of ["America/Argentina/Buenos_Aires", "Europe/Madrid"]) {
  test.describe(`Edit dialog dates in ${timezoneId} (#1338)`, () => {
    test.use({ timezoneId });

    test("budget edit shows the stored date and saving keeps it", async ({ page }) => {
      await establishAdminBrowserSession(page);
      const persona = await createPersona(page);
      expect(persona.ok, persona.error ?? "createPersona failed").toBe(true);
      const budget = await createPresupuesto(page, persona.data!.personId, undefined, { date: DAY });
      expect(budget.ok, budget.error ?? "createPresupuesto failed").toBe(true);
      const id = budget.data!.idBudget;

      await page.goto("/dashboard/presupuestos");
      await page.waitForLoadState("networkidle");
      await page.getByTestId(`btn-editar-presupuesto-${id}`).click();
      const dialog = page.getByRole("dialog");
      await expect(dialog.locator('input[type="date"]')).toHaveValue(DAY);

      await dialog.getByTestId("btn-guardar-presupuesto").click();
      await expect(dialog).toBeHidden({ timeout: 15000 });
      await page.goto("/dashboard/presupuestos");
      await page.getByTestId(`btn-editar-presupuesto-${id}`).click();
      await expect(page.getByRole("dialog").locator('input[type="date"]')).toHaveValue(DAY);
    });

    test("payment edit shows the stored date", async ({ page }) => {
      await establishAdminBrowserSession(page);
      const persona = await createPersona(page);
      const budget = await createPresupuesto(page, persona.data!.personId, undefined, {
        propertyAmount: 10000, // a payment may not exceed the budget balance
      });
      const pago = await createPago(page, budget.data!.idBudget, { date: DAY });
      expect(pago.ok, pago.error ?? "createPago failed").toBe(true);

      await page.goto("/dashboard/pagos");
      await page.waitForLoadState("networkidle");
      const row = page
        .getByRole("row")
        .filter({ has: page.getByRole("cell", { name: String(pago.data!.idPayment), exact: true }) });
      await row.getByRole("button", { name: /editar|edit/i }).click();
      await expect(page.getByRole("dialog").locator('input[type="date"]')).toHaveValue(DAY);
    });
  });
}
