/**
 * TS-0094 - Admin route guard for non-admin users
 *
 * Covers: CU78 – Security and Compliance (frontend access control)
 * Issue: #1052
 *
 * Golden path:
 *   admin creates EMPLEADO → login as EMPLEADO → open administración URL
 *   → /dashboard?forbidden=1 + access-denied message (no admin UI)
 */
import { test, expect, type Page } from "@playwright/test";
import { createUsuario } from "./setup/api-helpers";
import { authenticateAsAdmin } from "./setup/auth";

async function loginAs(page: Page, nombre: string, contrasenia: string) {
  await page.goto("/login");
  await page.getByTestId("input-usuario").fill(nombre);
  await page.getByTestId("input-contrasenia").fill(contrasenia);
  await page.getByTestId("btn-ingresar").click();
  await expect(page).toHaveURL(/\/dashboard/, { timeout: 15000 });
}

test.describe("TS-0094 - Admin route guard (CU78 / #1052)", () => {
  test("non-admin EMPLEADO is redirected from admin URLs with message", async ({ page }) => {
    const username = `emp1052-${Date.now()}`;
    const password = "Test1234!";

    await authenticateAsAdmin(page);
    const created = await createUsuario(page, undefined, {
      name: username,
      password,
      type: "EMPLEADO",
      active: true,
    });
    expect(created.ok, `createUsuario failed: ${created.status} ${created.error}`).toBe(true);

    // Drop admin cookies first so middleware does not bounce /login → /dashboard
    // (ERR_ABORTED). Auth helper init script only hydrates when status cookie
    // is present (#1051), so localStorage will not be re-poisoned after clear.
    await page.context().clearCookies();
    await page.goto("/login");
    await page.evaluate(() => localStorage.clear());

    await loginAs(page, username, password);

    await page.goto("/dashboard/administracion/usuarios");

    await expect(page).toHaveURL(/\/dashboard\?forbidden=1/, { timeout: 15000 });
    await expect(page.getByTestId("access-denied-message")).toBeVisible();
    await expect(page.getByTestId("access-denied-message")).toContainText(
      /permisos|permission/i,
    );
    await expect(page.getByTestId("btn-nuevo-usuario")).toHaveCount(0);
  });

  test("access-denied message is visible at mobile, tablet, and desktop widths", async ({
    page,
  }) => {
    await authenticateAsAdmin(page);
    for (const width of [320, 768, 1024] as const) {
      await page.setViewportSize({ width, height: 800 });
      await page.goto("/dashboard?forbidden=1");
      await expect(page.getByTestId("access-denied-message")).toBeVisible();
    }
  });

  test("admin can still open administración usuarios", async ({ page }) => {
    await loginAs(page, "admin", "admin");
    await page.goto("/dashboard/administracion/usuarios");
    await expect(page).toHaveURL(/\/dashboard\/administracion\/usuarios/);
    await expect(page.getByTestId("access-denied-message")).toHaveCount(0);
  });
});
