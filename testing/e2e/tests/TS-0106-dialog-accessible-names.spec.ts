/**
 * TS-0106 - Dialogs have accessible names (issue #1344)
 *
 * Covers: RNF-06 (Diseño de ventanas), CU76
 * Issue: #1344 — 28 of 30 form dialogs had no DialogTitle, so `role=dialog`
 * had no accessible name, and the close button said "Cerrar" in English too.
 */
import { test, expect, type Page } from "@playwright/test";

const CREATE_DIALOGS = [
  { route: "/dashboard/personas", button: "btn-nueva-persona", es: "Nueva persona", en: "New person" },
  { route: "/dashboard/presupuestos", button: "btn-nuevo-presupuesto", es: "Nuevo presupuesto", en: "New quote" },
  { route: "/dashboard/gestiones", button: "btn-nueva-gestion", es: "Nueva gestión", en: "New case" },
  { route: "/dashboard/escrituras", button: "btn-nueva-escritura", es: "Nueva escritura", en: "New deed" },
  { route: "/dashboard/pagos", button: "btn-nuevo-pago", es: "Nuevo pago", en: "New payment" },
];

async function login(page: Page) {
  await page.goto("/login");
  await page.getByTestId("input-usuario").fill("admin");
  await page.getByTestId("input-contrasenia").fill("admin");
  await page.getByTestId("btn-ingresar").click();
  await expect(page).toHaveURL(/\/dashboard/, { timeout: 15000 });
}

test.describe("TS-0106 - dialogs are named by their visible title (#1344)", () => {
  for (const d of CREATE_DIALOGS) {
    test(`create dialog on ${d.route} is announced as "${d.es}" with a localized close button`, async ({ page }) => {
      await login(page);
      await page.goto(d.route);
      await page.getByTestId(d.button).click();
      const dialog = page.getByRole("dialog", { name: d.es });
      await expect(dialog).toBeVisible();
      await expect(dialog.getByRole("button", { name: "Cerrar" })).toBeVisible();
    });
  }

  test("in English the dialog name and the close button are English", async ({ page, context }) => {
    await login(page);
    await context.addCookies([{ name: "NEXT_LOCALE", value: "en", url: page.url() }]);
    await page.goto("/dashboard/personas");
    await page.getByTestId("btn-nueva-persona").click();
    const dialog = page.getByRole("dialog", { name: "New person" });
    await expect(dialog).toBeVisible();
    await expect(dialog.getByRole("button", { name: "Close" })).toBeVisible();
  });
});
