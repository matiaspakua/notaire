/**
 * E2E tests — Workflow assignment to ProcedureType
 * CU73: Assign a workflow to a procedure type
 * Requires: backend running at localhost:8080, frontend at localhost:3000
 *
 * Arranges its own procedure type via API helpers (#1066) — never skips when the table is empty.
 */
import { test, expect, type Page } from "@playwright/test";
import { createProcedureType } from "./setup/api-helpers";

async function loginAsAdmin(page: Page) {
  await page.goto("/login");
  await page.getByTestId("input-username").fill("admin");
  await page.getByTestId("input-password").fill("admin");
  await page.getByTestId("btn-sign-in").click();
  await expect(page).toHaveURL(/\/dashboard/, { timeout: 10000 });
}

/** Create a procedure type; fail the test if arrange/seed fails (do not skip). */
async function arrangeTypeProcedure(page: Page): Promise<number> {
  const created = await createProcedureType(page, {
    name: `Assignment E2E ${Date.now()}`,
  });
  expect(
    created.ok && created.data?.idProcedureType != null,
    `arrange procedure type failed: ${created.status} ${created.error ?? JSON.stringify(created.data)}`,
  ).toBe(true);
  return created.data!.idProcedureType;
}

test.describe("CU73 - Workflow assignment to ProcedureType", () => {
  test.beforeEach(async ({ page }) => {
    await loginAsAdmin(page);
    await arrangeTypeProcedure(page);
    await page.goto("/dashboard/administration/procedures");
    await expect(page.locator("table tbody tr").first()).toBeVisible({ timeout: 10000 });
  });

  test("procedures page shows the workflow column", async ({ page }) => {
    await expect(page.getByRole("columnheader", { name: "Workflow" })).toBeVisible();
    await expect(page.getByTestId("btn-new-procedure-type")).toBeVisible();
  });

  test("edit modal shows workflow selector", async ({ page }) => {
    await page.locator("table tbody tr").first().getByTestId("btn-edit-procedure").click();
    await expect(page.getByTestId("select-workflow-procedure")).toBeVisible({ timeout: 5000 });
  });
});
