/**
 * #1364 (CU07/CU44) - Testimony edit/delete and movement history on /dashboard/testimonies.
 *
 * A mistaken testimony can be corrected (number, notes) or deleted with confirmation.
 * A testimony with registry movements is part of the deed's history: delete answers 409 and
 * the page says why, and "Ver movimientos" shows its withdrawal with the card number.
 * Movements are recorded through the movement actions (API here); their manual CRUD stays
 * API-only (Owner decision 2026-10-09).
 */
import { test, expect, type Page } from "@playwright/test";
import { GherkinSteps } from "./gherkin-helpers";
import { apiGet, apiPost, uniqueId } from "./setup/api-helpers";

async function seedVerifiedTestimony(page: Page): Promise<{ idTestimony: number; number: number }> {
  const deedNumber = uniqueId() % 1_000_000;
  const deed = await apiPost<{ idDeed: number }>(page, "/deeds", {
    number: deedNumber,
    dateDeedrecording: new Date().toISOString().split("T")[0],
    body: `E2E content ${deedNumber}`,
    status: "Firmada",
  });
  const number = uniqueId() % 1_000_000;
  const testimony = await apiPost<{ idTestimony: number }>(page, "/testimonies", {
    deed: { idDeed: deed.data!.idDeed, number: deedNumber },
    number,
    flagged: false,
    verified: true,
  });
  return { idTestimony: testimony.data!.idTestimony, number };
}

async function withdrawnWithCard(page: Page, idTestimony: number, cardNumber: number) {
  await apiPost(page, `/testimony-movements/${idTestimony}/enter-registration`, {});
  await apiPost(page, `/testimony-movements/${idTestimony}/register-inscription`, {});
  await apiPost(page, `/testimony-movements/${idTestimony}/withdraw`, { cardNumber });
}

test.describe("#1364 - Testimony edit, delete and movement history", () => {
  let steps: GherkinSteps;

  test.beforeEach(async ({ page }) => {
    steps = new GherkinSteps(page);
    await steps.givenUserIsLoggedIn();
  });

  test("Golden path: edit number and notes, then delete a testimony without movements", async ({ page }) => {
    const { idTestimony, number } = await seedVerifiedTestimony(page);
    const corrected = (number % 900_000) + 100_000 + 1;

    await steps.givenUserIsOnPage("/dashboard/testimonies");
    await expect(page.getByRole("row", { name: new RegExp(String(number)) })).toBeVisible({ timeout: 15000 });

    await page.getByTestId(`btn-edit-testimony-${idTestimony}`).click();
    await steps.thenModalIsVisible();
    await page.getByTestId("input-edit-testimony-number").fill(String(corrected));
    await page.getByTestId("input-edit-testimony-notes").fill("Corrected number E2E");
    await page.getByTestId("btn-save-edit-testimony").click();
    await steps.thenShowsSuccessMessage("Testimonio actualizado");
    await expect(page.getByRole("row", { name: new RegExp(String(corrected)) })).toBeVisible();

    const stored = await apiGet<{ number: number; notes: string }>(page, `/testimonies/${idTestimony}`);
    expect(stored.data).toMatchObject({ number: corrected, notes: "Corrected number E2E" });

    await page.getByTestId(`btn-delete-testimony-${idTestimony}`).click();
    await page.getByRole("alertdialog").getByRole("button", { name: "Eliminar" }).click();
    await steps.thenShowsSuccessMessage("Testimonio eliminado");
    await expect(page.getByTestId(`btn-delete-testimony-${idTestimony}`)).toHaveCount(0);
    expect((await apiGet(page, `/testimonies/${idTestimony}`)).status).toBe(404);
  });

  test("Edge: a withdrawn testimony shows its movement and can't be deleted", async ({ page }) => {
    const { idTestimony, number } = await seedVerifiedTestimony(page);
    await withdrawnWithCard(page, idTestimony, 456);

    await steps.givenUserIsOnPage("/dashboard/testimonies");
    await expect(page.getByRole("row", { name: new RegExp(String(number)) })).toBeVisible({ timeout: 15000 });

    await page.getByTestId(`btn-movements-testimony-${idTestimony}`).click();
    const dialog = page.getByRole("dialog");
    await expect(dialog).toContainText(`Movimientos del testimonio N.º ${number}`);
    const items = dialog.locator('[data-testid^="testimony-movement-"]');
    await expect(items).toHaveCount(1);
    await expect(items.first()).toContainText("Retiro");
    await expect(items.first()).toContainText("Cartón N.º 456");
    await expect(items.first()).toContainText("Inscripto");
    await page.keyboard.press("Escape");

    await page.getByTestId(`btn-delete-testimony-${idTestimony}`).click();
    await page.getByRole("alertdialog").getByRole("button", { name: "Eliminar" }).click();
    await expect(page.getByText(/tiene movimientos en el Registro/)).toBeVisible();
    expect((await apiGet(page, `/testimonies/${idTestimony}`)).status).toBe(200);
  });

  test("Mobile: the history and edit actions are usable at 390px", async ({ page }) => {
    await page.setViewportSize({ width: 390, height: 844 });
    const { idTestimony, number } = await seedVerifiedTestimony(page);

    await steps.givenUserIsOnPage("/dashboard/testimonies");
    const movements = page.getByTestId(`btn-movements-testimony-${idTestimony}`);
    await expect(movements).toBeVisible({ timeout: 15000 });
    await movements.click();
    await expect(page.getByRole("dialog")).toContainText("todavía no tiene movimientos");
    await page.keyboard.press("Escape");

    await page.getByTestId(`btn-edit-testimony-${idTestimony}`).click();
    await expect(page.getByTestId("input-edit-testimony-number")).toHaveValue(String(number));
  });
});
