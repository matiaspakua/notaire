/**
 * TS-0146 - Register a testimony manually (CU07)
 *
 * Covers: CU07 (registrar testimonio), POST /api/v1/testimonies
 * Issue: #1555
 *
 * Golden path: on /dashboard/testimonies "Registrar testimonio" takes a signed deed, a number
 * and notes; the new testimony appears in the list and is stored as sent.
 * Edge: without a deed or number the dialog shows inline errors and sends nothing.
 * Mobile: the same flow works at 390px.
 */
import { test, expect, type Page } from "@playwright/test";
import { GherkinSteps } from "./gherkin-helpers";
import { apiDelete, apiGet, apiPost, uniqueId } from "./setup/api-helpers";
import { chooseDeed } from "./setup/deed-picker";

async function seedSignedDeed(page: Page): Promise<{ idDeed: number; number: number }> {
  const number = uniqueId() % 1_000_000;
  const deed = await apiPost<{ idDeed: number }>(page, "/deeds", {
    number,
    dateDeedrecording: new Date().toISOString().split("T")[0],
    body: `E2E manual testimony ${number}`,
    status: "Firmada",
  });
  expect(deed.ok, "deed created").toBe(true);
  return { idDeed: deed.data!.idDeed, number };
}

async function registerManually(page: Page, steps: GherkinSteps) {
  const deed = await seedSignedDeed(page);
  const testimonyNumber = (uniqueId() % 900_000) + 100_000;
  await steps.givenUserIsOnPage("/dashboard/testimonies");
  await page.getByTestId("btn-register-testimony").click();
  await steps.thenModalIsVisible();
  await chooseDeed(page, "select-deed-register-testimony", deed.number);
  await page.getByTestId("input-number-register-testimony").fill(String(testimonyNumber));
  await page.getByTestId("input-notes-register-testimony").fill("Testimonio histórico E2E");
  await page.getByTestId("btn-save-register-testimony").click();
  // The save can take a few seconds on a loaded backend: wait for the dialog to close, not a 5 s toast.
  await expect(page.getByRole("dialog")).toBeHidden({ timeout: 30000 });
  // Table row on desktop, card at 390px (#1356): match the number as text.
  await expect(page.getByText(String(testimonyNumber), { exact: true }).first()).toBeVisible({ timeout: 15000 });
  const list = await apiGet<Array<{ idTestimony: number; number: number; notes: string; deed?: { idDeed: number } }>>(page, "/testimonies");
  const stored = list.data!.find((te) => te.number === testimonyNumber)!;
  expect(stored).toMatchObject({ notes: "Testimonio histórico E2E", deed: { idDeed: deed.idDeed } });
  await apiDelete(page, `/testimonies/${stored.idTestimony}`);
}

test.describe("TS-0146 - Register a testimony manually (#1555)", () => {
  let steps: GherkinSteps;

  test.beforeEach(async ({ page }) => {
    steps = new GherkinSteps(page);
    await steps.givenUserIsLoggedIn();
  });

  test("Golden path: register a testimony for a signed deed and find it in the list", async ({ page }) => {
    await registerManually(page, steps);
  });

  test("Edge: deed and number are required", async ({ page }) => {
    await steps.givenUserIsOnPage("/dashboard/testimonies");
    await page.getByTestId("btn-register-testimony").click();
    await page.getByTestId("btn-save-register-testimony").click();
    await expect(page.getByTestId("register-testimony-error-deed")).toBeVisible();
    await expect(page.getByTestId("register-testimony-error-number")).toBeVisible();
    await steps.thenModalIsVisible();
  });

  test("Mobile 390px: register a testimony manually", async ({ page }) => {
    await page.setViewportSize({ width: 390, height: 844 });
    await registerManually(page, steps);
  });
});
