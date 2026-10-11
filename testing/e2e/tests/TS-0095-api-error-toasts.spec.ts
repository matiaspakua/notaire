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
  await page.getByTestId("input-username").fill("admin");
  await page.getByTestId("input-password").fill("admin");
  await page.getByTestId("btn-sign-in").click();
  await expect(page).toHaveURL(/\/dashboard/, { timeout: 15000 });
}

test.describe("TS-0095 - API error toasts (CU15 / CU20 / #1054)", () => {
  test("a payment mutation shows the server message in the toast (not only the generic fallback)", async ({
    page,
  }) => {
    await loginAsAdmin(page);

    const serverMessage = "El monto excede el saldo pendiente del presupuesto";
    await page.route("**/api/v1/payments", async (route) => {
      if (route.request().method() === "POST") {
        await route.fulfill({
          status: 409,
          contentType: "application/json",
          body: JSON.stringify({
            status: 409,
            error: "Conflict",
            message: serverMessage,
            path: "/api/v1/payments",
          }),
        });
        return;
      }
      await route.continue();
    });

    await page.goto("/dashboard/payments");
    await page.getByTestId("btn-new-payment").click();
    await page.getByTestId("input-amount-payment").fill("999999");
    // Submit even if budget picker is empty — stub owns the failure.
    await page.getByRole("button", { name: /crear|create/i }).click();

    await expect(
      page.locator("[data-sonner-toast]").getByText(serverMessage)
    ).toBeVisible({ timeout: 10000 });
  });

  test("user form shows FormField error and aria-invalid for field detail", async ({
    page,
  }) => {
    await loginAsAdmin(page);

    const fieldMessage = "name: must not be blank";
    await page.route("**/api/v1/users", async (route) => {
      if (route.request().method() === "POST") {
        await route.fulfill({
          status: 400,
          contentType: "application/json",
          body: JSON.stringify({
            status: 400,
            error: "Bad Request",
            message: fieldMessage,
            path: "/api/v1/users",
          }),
        });
        return;
      }
      await route.continue();
    });

    await page.goto("/dashboard/administration/users");
    await page.getByTestId("btn-new-user").click();
    // Bypass client-side empty-name guard so the stubbed API error is exercised.
    await page.getByTestId("input-name-user").fill("stub-user");
    await page.getByRole("button", { name: /crear|create/i }).click();

    await expect(
      page.locator("[data-sonner-toast]").getByText(fieldMessage)
    ).toBeVisible({ timeout: 10000 });
    await expect(page.getByTestId("input-name-user")).toHaveAttribute(
      "aria-invalid",
      "true"
    );
    // FormField renders the error as role="alert" linked to the input (#1351) —
    // scope to dialog so the toast's "name: must not be blank" does not collide.
    await expect(
      page.getByRole("dialog").getByRole("alert").filter({ hasText: /must not be blank/i })
    ).toBeVisible();
    await expect(page.getByTestId("input-name-user")).toHaveAccessibleDescription(/must not be blank/i);
  });

  test("error toast / field error visible at 320px, 768px, and 1024px", async ({
    page,
  }) => {
    await loginAsAdmin(page);

    const serverMessage = "Role duplicado: ya existe un rol con ese nombre";
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
      await page.goto("/dashboard/administration/roles");
      await page.getByTestId("btn-new-role").click();
      await page.getByTestId("input-name-role").fill(`rol-e2e-${width}`);
      await page.getByRole("button", { name: /crear|create/i }).click();
      await expect(
        page.locator("[data-sonner-toast]").getByText(serverMessage)
      ).toBeVisible({ timeout: 10000 });
      // Dismiss toast / close dialog before next viewport iteration.
      await page.keyboard.press("Escape");
    }
  });
});
