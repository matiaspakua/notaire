/**
 * TS-0105 - Delete failures show why (issue #1345)
 *
 * Covers: CU76 (RF-38 Modificación de clientes, RF-53 Administrar tablas base)
 * Issue: #1345 — delete handlers toasted a generic "Error al eliminar" and hid
 * the backend's 409 "in use" reason.
 *
 * Approach: stub the list and the DELETE so the person row and the 409 are
 * stable (no seed-data dependency, as in TS-0095).
 */
import { test, expect, type Page } from "@playwright/test";

const PERSON_ID = 990_001;
const SERVER_REASON = "Cannot delete: person is referenced by other records.";

async function loginAsAdmin(page: Page) {
  await page.goto("/login");
  await page.getByTestId("input-usuario").fill("admin");
  await page.getByTestId("input-contrasenia").fill("admin");
  await page.getByTestId("btn-ingresar").click();
  await expect(page).toHaveURL(/\/dashboard/, { timeout: 15000 });
}

async function stubPeople(page: Page, deleteStatus: number, deleteBody: string) {
  await page.route("**/api/v1/people?**", async (route) => {
    if (route.request().method() !== "GET") return route.continue();
    await route.fulfill({
      status: 200,
      contentType: "application/json",
      body: JSON.stringify({
        content: [{ personId: PERSON_ID, firstName: "Referenciada", lastName: "TS0105", identificationNumber: "99000001", isClient: true }],
        totalElements: 1, totalPages: 1, number: 0, size: 1000,
      }),
    });
  });
  await page.route(`**/api/v1/people/${PERSON_ID}`, async (route) => {
    if (route.request().method() !== "DELETE") return route.continue();
    await route.fulfill({ status: deleteStatus, contentType: "text/plain", body: deleteBody });
  });
}

async function deleteTheRow(page: Page) {
  await page.goto("/dashboard/personas");
  const row = page.getByRole("row").filter({ hasText: "TS0105" });
  await row.getByRole("button", { name: /eliminar|delete/i }).click();
  await page.getByRole("alertdialog").getByRole("button", { name: /eliminar|delete/i }).click();
}

test.describe("TS-0105 - delete failures keep the backend reason (#1345)", () => {
  test("409 on a referenced person shows the in-use warning with the server reason", async ({ page }) => {
    await loginAsAdmin(page);
    await stubPeople(page, 409, SERVER_REASON);
    await deleteTheRow(page);

    const toast = page.locator("[data-sonner-toast]").filter({ hasText: /No se puede eliminar|Cannot be deleted/ });
    await expect(toast).toBeVisible({ timeout: 10000 });
    await expect(toast).toHaveAttribute("data-type", "warning");
    await expect(toast).toContainText(SERVER_REASON);
    await expect(page.locator("[data-sonner-toast]").filter({ hasText: "Error al eliminar la persona" })).toHaveCount(0);
  });

  test("404 on delete says the record no longer exists", async ({ page }) => {
    await loginAsAdmin(page);
    await stubPeople(page, 404, "");
    await deleteTheRow(page);

    await expect(
      page.locator("[data-sonner-toast]").filter({ hasText: /ya no existe|no longer exists/ })
    ).toBeVisible({ timeout: 10000 });
  });
});
