import { test, expect } from "@playwright/test";

async function loginAsAdmin(page: import("@playwright/test").Page) {
  await page.goto("/login");
  await page.getByTestId("input-username").fill("admin");
  await page.getByTestId("input-password").fill("admin");
  await page.getByTestId("btn-sign-in").click();
  await expect(page).toHaveURL(/\/dashboard/, { timeout: 10000 });
}

test.describe("CU431 — Role and permission management", () => {
  test.beforeEach(async ({ page }) => {
    await loginAsAdmin(page);
  });

  test("roles page is accessible from administracion", async ({ page }) => {
    await page.goto("/dashboard/administration/roles");
    await expect(page.getByRole("heading", { name: "Roles y Permisos" })).toBeVisible({ timeout: 10000 });
  });

  test("new role button opens modal", async ({ page }) => {
    await page.goto("/dashboard/administration/roles");
    await page.getByTestId("btn-new-role").click();
    await expect(page.getByTestId("input-name-role")).toBeVisible();
  });

  test("users page shows the role column", async ({ page }) => {
    await page.goto("/dashboard/administration/users");
    await expect(page.getByText("Role")).toBeVisible({ timeout: 10000 });
  });

  test("user edit modal shows the role selector", async ({ page }) => {
    await page.goto("/dashboard/administration/users");
    await page.getByTestId("btn-new-user").click();
    await expect(page.getByTestId("select-role-user")).toBeVisible();
  });
});
