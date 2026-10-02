/**
 * TS-0095 - API error toasts / field errors on CRUD mutations
 *
 * Covers: CU15 – Procesar pago; CU20 – Dar alta usuario
 * Issues: #1054 (surface backend messages), #615 (E2E validation gap — focused slice)
 *
 * Approach: stub API 4xx with known ErrorResponse bodies so assertions are
 * stable (no seed-data dependency for forced business failures).
 */
import { test, expect, type Page } from "@playwright/test";

async function loginAsAdmin(page: Page) {
  await page.goto("/login");
  await page.getByTestId("input-usuario").fill("admin");
  await page.getByTestId("input-contrasenia").fill("admin");
  await page.getByTestId("btn-ingresar").click();
  await expect(page).toHaveURL(/\/dashboard/, { timeout: 15000 });
}

test.describe("TS-0095 - API error toasts (CU15 / CU20 / #1054)", () => {
  test("pago mutation shows server message in toast (not only generic fallback)", async ({
    page,
  }) => {
    await loginAsAdmin(page);

    const serverMessage = "El monto excede el saldo pendiente del presupuesto";
    await page.route("**/api/v1/pagos", async (route) => {
      if (route.request().method() === "POST") {
        await route.fulfill({
          status: 409,
          contentType: "application/json",
          body: JSON.stringify({
            status: 409,
            error: "Conflict",
            message: serverMessage,
            path: "/api/v1/pagos",
          }),
        });
        return;
      }
      await route.continue();
    });

    await page.goto("/dashboard/pagos");
    await page.getByTestId("btn-nuevo-pago").click();
    await page.getByTestId("input-monto-pago").fill("999999");
    // Submit even if presupuesto picker is empty — stub owns the failure.
    await page.getByRole("button", { name: /crear|create/i }).click();

    await expect(
      page.locator("[data-sonner-toast]").getByText(serverMessage)
    ).toBeVisible({ timeout: 10000 });
  });

  test("usuario form shows FormField error and aria-invalid for field detail", async ({
    page,
  }) => {
    await loginAsAdmin(page);

    const fieldMessage = "name: must not be blank";
    await page.route("**/api/v1/usuarios", async (route) => {
      if (route.request().method() === "POST") {
        await route.fulfill({
          status: 400,
          contentType: "application/json",
          body: JSON.stringify({
            status: 400,
            error: "Bad Request",
            message: fieldMessage,
            path: "/api/v1/usuarios",
          }),
        });
        return;
      }
      await route.continue();
    });

    await page.goto("/dashboard/administracion/usuarios");
    await page.getByTestId("btn-nuevo-usuario").click();
    // Bypass client-side empty-name guard so the stubbed API error is exercised.
    await page.getByTestId("input-nombre-usuario").fill("stub-user");
    await page.getByRole("button", { name: /crear|create/i }).click();

    await expect(
      page.locator("[data-sonner-toast]").getByText(fieldMessage)
    ).toBeVisible({ timeout: 10000 });
    await expect(page.getByTestId("input-nombre-usuario")).toHaveAttribute(
      "aria-invalid",
      "true"
    );
    await expect(page.getByText(/must not be blank/i)).toBeVisible();
  });

  test("error toast / field error visible at 320px, 768px, and 1024px", async ({
    page,
  }) => {
    await loginAsAdmin(page);

    const serverMessage = "Rol duplicado: ya existe un rol con ese nombre";
    await page.route("**/api/v1/roles", async (route) => {
      if (route.request().method() === "POST") {
        await route.fulfill({
          status: 409,
          contentType: "application/json",
          body: JSON.stringify({
            status: 409,
            error: "Conflict",
            message: serverMessage,
            path: "/api/v1/roles",
          }),
        });
        return;
      }
      await route.continue();
    });

    for (const width of [320, 768, 1024] as const) {
      await page.setViewportSize({ width, height: 800 });
      await page.goto("/dashboard/administracion/roles");
      await page.getByTestId("btn-nuevo-rol").click();
      await page.getByTestId("input-nombre-rol").fill(`rol-e2e-${width}`);
      await page.getByRole("button", { name: /crear|create/i }).click();
      await expect(
        page.locator("[data-sonner-toast]").getByText(serverMessage)
      ).toBeVisible({ timeout: 10000 });
      // Dismiss toast / close dialog before next viewport iteration.
      await page.keyboard.press("Escape");
    }
  });
});
