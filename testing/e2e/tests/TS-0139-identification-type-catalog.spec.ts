/**
 * TS-0139 - Identification type admin catalog
 *
 * Covers: CU17 (identification types), RF-53 (administer base tables)
 * Issue: #1361 (slice 2)
 *
 * Golden path: ADMIN opens Administration → Identification types, creates a
 * type, edits its number length and deletes it.
 * Edges: a type people use is not deleted (the person stays), a name that
 * already exists in another letter case is rejected, and a non-admin is sent
 * away from the page.
 */
import { test, expect, type Page } from "@playwright/test";
import { authenticateAsAdmin } from "./setup/auth";
import { apiDelete, apiGet, apiPost, createPerson, createUser, uniqueId } from "./setup/api-helpers";

const PAGE = "/dashboard/administration/identification-types";

function rowFor(page: Page, name: string) {
  return page.getByRole("row").filter({ has: page.getByRole("cell", { name, exact: true }) });
}

async function openCatalog(page: Page) {
  await page.goto(PAGE);
  await expect(page.getByRole("heading", { level: 1 })).toBeVisible({ timeout: 15000 });
  await expect(rowFor(page, "DNI")).toBeVisible({ timeout: 15000 });
}

test.describe("TS-0139 - Identification type catalog (#1361)", () => {
  test.beforeEach(async ({ page }) => {
    await authenticateAsAdmin(page);
  });

  test("the administration index links the catalog", async ({ page }) => {
    await page.goto("/dashboard/administration");
    await page.getByRole("link", { name: /tipos de identificación|identification types/i }).click();
    await expect(page).toHaveURL(new RegExp(`${PAGE}$`), { timeout: 15000 });
    await expect(rowFor(page, "DNI")).toBeVisible({ timeout: 15000 });
  });

  test("row actions show their icons (not squeezed to zero width)", async ({ page }) => {
    await openCatalog(page);
    const row = rowFor(page, "DNI");
    for (const id of ["btn-edit-identification-type", "btn-delete-identification-type"]) {
      const box = await row.getByTestId(id).locator("img").boundingBox();
      expect(box?.width ?? 0, `${id} icon width`).toBeGreaterThanOrEqual(12);
    }
  });

  test("admin creates, edits and deletes a type", async ({ page }) => {
    const name = `Doc E2E ${uniqueId()}`;
    await openCatalog(page);

    // WHEN: the admin creates a type
    await page.getByTestId("btn-new-identification-type").click();
    let dialog = page.getByRole("dialog");
    await dialog.getByTestId("input-identification-type-name").fill(name);
    await dialog.getByTestId("input-identification-type-characters").fill("10");
    await dialog.getByTestId("btn-save-identification-type").click();

    // THEN: it is listed with its length
    await expect(dialog).toBeHidden();
    const row = rowFor(page, name);
    await expect(row).toBeVisible({ timeout: 10000 });
    await expect(row.getByRole("cell", { name: "10", exact: true })).toBeVisible();

    // WHEN: the admin changes the length
    await row.getByTestId("btn-edit-identification-type").click();
    dialog = page.getByRole("dialog");
    await expect(dialog.getByTestId("input-identification-type-name")).toHaveValue(name);
    await dialog.getByTestId("input-identification-type-characters").fill("12");
    await dialog.getByTestId("btn-save-identification-type").click();
    await expect(dialog).toBeHidden();
    await expect(rowFor(page, name).getByRole("cell", { name: "12", exact: true })).toBeVisible({ timeout: 10000 });

    // WHEN: the admin deletes it
    await rowFor(page, name).getByTestId("btn-delete-identification-type").click();
    await page.getByRole("alertdialog").getByRole("button", { name: /confirmar|confirm|eliminar|delete/i }).click();

    // THEN: it is gone
    await expect(rowFor(page, name)).toHaveCount(0, { timeout: 10000 });
  });

  test("a type people use is not deleted and the person stays", async ({ page }) => {
    const name = `Used E2E ${uniqueId()}`;
    const type = await apiPost<{ idIdentificationType: number }>(page, "/identification-types", { name, characters: "9" });
    expect(type.ok, `create type: ${type.error}`).toBe(true);
    const typeId = type.data!.idIdentificationType;
    const person = await createPerson(page, { identificationTypeId: typeId });
    expect(person.ok, `create person: ${person.error}`).toBe(true);

    await openCatalog(page);
    await rowFor(page, name).getByTestId("btn-delete-identification-type").click();
    await page.getByRole("alertdialog").getByRole("button", { name: /confirmar|confirm|eliminar|delete/i }).click();

    await expect(page.getByText(/en uso|still used/i).first()).toBeVisible({ timeout: 10000 });
    await expect(rowFor(page, name)).toBeVisible();
    const stillThere = await apiGet(page, `/people/${person.data!.personId}`);
    expect(stillThere.status).toBe(200);

    // cleanup
    await apiDelete(page, `/people/${person.data!.personId}`);
    await apiDelete(page, `/identification-types/${typeId}`);
  });

  test("a name that already exists in another letter case is rejected", async ({ page }) => {
    await openCatalog(page);
    await page.getByTestId("btn-new-identification-type").click();
    const dialog = page.getByRole("dialog");
    await dialog.getByTestId("input-identification-type-name").fill(" dni ");
    await dialog.getByTestId("input-identification-type-characters").fill("8");
    await dialog.getByTestId("btn-save-identification-type").click();

    await expect(page.getByText(/ya existe|already exists/i).first()).toBeVisible({ timeout: 10000 });
    await expect(dialog).toBeVisible();
    const all = await apiGet<{ name: string }[]>(page, "/identification-types");
    expect(all.data!.filter((t) => t.name.trim().toUpperCase() === "DNI")).toHaveLength(1);
  });

  test("a non-admin is sent away from the catalog", async ({ page }) => {
    const username = `emp1361-${Date.now()}`;
    const password = "Test1234!";
    const created = await createUser(page, undefined, { name: username, password, type: "EMPLEADO", active: true });
    expect(created.ok, `createUser failed: ${created.status} ${created.error}`).toBe(true);

    await page.context().clearCookies();
    await page.goto("/login");
    await page.evaluate(() => localStorage.clear());
    await page.getByTestId("input-username").fill(username);
    await page.getByTestId("input-password").fill(password);
    await page.getByTestId("btn-sign-in").click();
    await expect(page).toHaveURL(/\/dashboard/, { timeout: 15000 });

    await page.goto(PAGE);
    await expect(page).toHaveURL(/\/dashboard\?forbidden=1/, { timeout: 15000 });
    await expect(page.getByTestId("btn-new-identification-type")).toHaveCount(0);
  });
});
