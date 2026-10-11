/**
 * E2E tests — Manage folio notebooks (CU80).
 */
import { type Page, test, expect } from "@playwright/test";
import { authenticateAsAdmin } from "./setup/auth";
import { createPerson, createFolio, uniqueId } from "./setup/api-helpers";

async function seedConsecutiveFolios(
  page: Page,
  notaryId: number,
  count: number,
  overrides: { statuses?: string[] } = {},
) {
  const base = (uniqueId() % 900000) * 10 + 100000;
  const folios = [];
  for (let i = 0; i < count; i++) {
    const number = base + i;
    const status = overrides.statuses?.[i] ?? "Nuevo";
    const folio = await createFolio(page, notaryId, { number: number, status: status });
    folios.push({ idFolio: folio.data!.idFolio, number });
  }
  return folios;
}

test.describe("CU80 - Manage folio notebooks", () => {
  test.beforeEach(async ({ page }) => {
    await authenticateAsAdmin(page);
  });

  test("golden path: creating a notebook from 10 consecutive folios shows it in the list", async ({ page }) => {
    const person = await createPerson(page);
    const notaryId = person.data!.personId;
    const folios = await seedConsecutiveFolios(page, notaryId, 10);

    await page.goto("/dashboard/protocol/notebooks");
    await page.waitForLoadState("domcontentloaded");
    await page.getByRole("button", { name: /nuevo cuaderno/i }).click();

    const dialog = page.getByRole("dialog");
    await expect(dialog).toBeVisible();
    for (const folio of folios) {
      await dialog.getByTestId(`checkbox-folio-${folio.idFolio}`).click();
    }

    await dialog.getByRole("button", { name: /crear/i }).click();
    await expect(page.getByText(/cuaderno creado/i)).toBeVisible({ timeout: 10000 });
    await expect(dialog).not.toBeVisible();
    await expect(page.getByRole("button", { name: /descargar pdf/i }).first()).toBeVisible({ timeout: 10000 });
  });

  test("checkbox selection toggles independently for each folio (regression for double-toggle bug)", async ({
    page,
  }) => {
    const person = await createPerson(page);
    const folios = await seedConsecutiveFolios(page, person.data!.personId, 3);

    await page.goto("/dashboard/protocol/notebooks");
    await page.waitForLoadState("domcontentloaded");
    await page.getByRole("button", { name: /nuevo cuaderno/i }).click();

    const dialog = page.getByRole("dialog");
    const checkboxes = folios.map((f) => dialog.getByTestId(`checkbox-folio-${f.idFolio}`));

    for (const checkbox of checkboxes) {
      await checkbox.click();
      await expect(checkbox).toBeChecked();
    }

    await checkboxes[1].click();
    await expect(checkboxes[1]).not.toBeChecked();
    await expect(checkboxes[0]).toBeChecked();
    await expect(checkboxes[2]).toBeChecked();
  });

  test("edge path: selecting a count that is not a multiple of ten shows a validation error", async ({ page }) => {
    const person = await createPerson(page);
    const folios = await seedConsecutiveFolios(page, person.data!.personId, 3);

    await page.goto("/dashboard/protocol/notebooks");
    await page.waitForLoadState("domcontentloaded");
    await page.getByRole("button", { name: /nuevo cuaderno/i }).click();

    const dialog = page.getByRole("dialog");
    for (const folio of folios) {
      await dialog.getByTestId(`checkbox-folio-${folio.idFolio}`).click();
    }
    await dialog.getByRole("button", { name: /crear/i }).click();

    await expect(page.getByText(/exact multiple of 10/i)).toBeVisible({ timeout: 10000 });
    await expect(dialog).toBeVisible();
  });

  test("edge path: including a damaged folio without notes shows a validation error", async ({ page }) => {
    const person = await createPerson(page);
    const folios = await seedConsecutiveFolios(page, person.data!.personId, 10, {
      statuses: ["Errose"],
    });

    await page.goto("/dashboard/protocol/notebooks");
    await page.waitForLoadState("domcontentloaded");
    await page.getByRole("button", { name: /nuevo cuaderno/i }).click();

    const dialog = page.getByRole("dialog");
    for (const folio of folios) {
      await dialog.getByTestId(`checkbox-folio-${folio.idFolio}`).click();
    }
    await dialog.getByRole("button", { name: /crear/i }).click();

    await expect(page.getByText(/requires a justification/i)).toBeVisible({ timeout: 10000 });
    await expect(dialog).toBeVisible();

    await dialog.getByLabel(/observaciones/i).fill("Folio damaged during printing");
    await dialog.getByRole("button", { name: /crear/i }).click();
    await expect(page.getByText(/cuaderno creado/i)).toBeVisible({ timeout: 10000 });
  });
});
