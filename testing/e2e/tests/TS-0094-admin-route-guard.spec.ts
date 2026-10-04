/**
 * TS-0094 - Admin route guard for non-admin users
 *
 * Covers: CU78 – Security and Compliance (frontend access control)
 * Issue: #1052
 *
 * Golden path:
 *   admin creates EMPLEADO → login as EMPLEADO → open administración URL
 *   → /dashboard?forbidden=1 + access-denied message (no admin UI)
 *
 * Server side (#559): the same EMPLEADO session gets 403 from the user, role and
 * audit-log APIs and cannot change a catalog, while catalog reads stay open.
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

async function loginAsNewEmployee(page: Page): Promise<void> {
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

  // Drop admin cookies first so edge proxy does not bounce /login → /dashboard
  // (ERR_ABORTED). Auth helper init script only hydrates when status cookie
  // is present (#1051), so localStorage will not be re-poisoned after clear.
  await page.context().clearCookies();
  await page.goto("/login");
  await page.evaluate(() => localStorage.clear());

  await loginAs(page, username, password);
}

test.describe("TS-0094 - Admin route guard (CU78 / #1052)", () => {
  test("non-admin EMPLEADO is redirected from admin URLs with message", async ({ page }) => {
    await loginAsNewEmployee(page);

    await page.goto("/dashboard/administracion/usuarios");

    await expect(page).toHaveURL(/\/dashboard\?forbidden=1/, { timeout: 15000 });
    await expect(page.getByTestId("access-denied-message")).toBeVisible();
    await expect(page.getByTestId("access-denied-message")).toContainText(
      /permisos|permission/i,
    );
    await expect(page.getByTestId("btn-nuevo-usuario")).toHaveCount(0);
  });

  test("EMPLEADO is redirected from the audit screen (#559)", async ({ page }) => {
    await loginAsNewEmployee(page);

    await page.goto("/dashboard/auditoria");

    await expect(page).toHaveURL(/\/dashboard\?forbidden=1/, { timeout: 15000 });
    await expect(page.getByTestId("access-denied-message")).toBeVisible();
  });

  test("EMPLEADO session gets 403 from administrative APIs and can read catalogs (#559)", async ({
    page,
  }) => {
    await loginAsNewEmployee(page);

    for (const path of ["/api/v1/usuarios", "/api/v1/roles", "/api/v1/audit-log"]) {
      const response = await page.request.get(path);
      expect(response.status(), `GET ${path}`).toBe(403);
    }
    const create = await page.request.post("/api/v1/tipo-tramite", {
      data: { name: `forbidden-${Date.now()}`, notes: "rbac", isArchived: false, isRegistered: false },
    });
    expect(create.status()).toBe(403);
    const catalog = await page.request.get("/api/v1/tipo-tramite");
    expect(catalog.status()).toBe(200);
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
