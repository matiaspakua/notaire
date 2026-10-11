/**
 * CU11/CU12/CU44 - Testimony registration cycle (issue #832)
 *
 * Golden path: enter for registration → record the registration → withdraw.
 * Edge path: re-enter without a prior withdrawal (blocked, per design.md —
 * Playwright Strategy). Testimonies are seeded already signed, generated,
 * and verified directly via the generic CRUD endpoints (design.md — Non-Goals:
 * these endpoints stay open) so each test starts from the exact movement
 * state it exercises.
 */
import { test, expect } from "@playwright/test";
import { GherkinSteps } from "./gherkin-helpers";
import { apiPost, uniqueId } from "./setup/api-helpers";

async function seedVerifiedTestimony(page: import("@playwright/test").Page): Promise<{ idTestimony: number; number: number }> {
  const deedNumber = uniqueId() % 1_000_000;
  const deed = await apiPost<{ idDeed: number }>(page, "/deeds", {
    number: deedNumber,
    dateDeedrecording: new Date().toISOString().split("T")[0],
    body: `E2E content ${deedNumber}`,
    status: "Firmada",
  });
  const number = uniqueId() % 1_000_000;
  // DtoDeed.number is primitive int — must be included in nested object to avoid 400
  // (see TestimonyControllerIntegrationTest#testimonyBody).
  const testimony = await apiPost<{ idTestimony: number }>(page, "/testimonies", {
    deed: { idDeed: deed.data!.idDeed, number: deedNumber },
    number: number,
    flagged: false,
    verified: true,
  });
  return { idTestimony: testimony.data!.idTestimony, number };
}

test.describe("CU11/CU12/CU44 - Testimony movements", () => {
  let steps: GherkinSteps;

  test.beforeEach(async ({ page }) => {
    steps = new GherkinSteps(page);
    await steps.givenUserIsLoggedIn();
  });

  test("Golden path: submit, record registration, and withdraw", async ({ page }) => {
    const { idTestimony, number } = await seedVerifiedTestimony(page);

    await steps.givenUserIsOnPage("/dashboard/testimony-movements");
    const row = page.getByRole("row", { name: new RegExp(String(number)) });
    await expect(row).toBeVisible({ timeout: 10000 });

    await page.getByTestId(`btn-sign-in-testimony-${idTestimony}`).click();
    await steps.thenShowsSuccessMessage("ingresado");
    await expect(row).toContainText(/ingresado/i);

    await page.getByTestId(`btn-record-registration-${idTestimony}`).click();
    await steps.thenShowsSuccessMessage("inscripción registrada");
    await expect(row).toContainText(/inscripto/i);

    await page.getByTestId(`btn-withdraw-testimony-${idTestimony}`).click();
    await steps.thenModalIsVisible();
    await page.getByTestId("input-card-number").fill("123");
    await page.getByTestId("btn-confirm-withdraw").click();
    await steps.thenShowsSuccessMessage("retirado");
    await expect(row).toContainText(/retirado/i);
  });

  test("Edge: re-entry is available only after a previous withdrawal", async ({ page }) => {
    const { idTestimony, number } = await seedVerifiedTestimony(page);

    await steps.givenUserIsOnPage("/dashboard/testimony-movements");
    const row = page.getByRole("row", { name: new RegExp(String(number)) });
    await expect(row).toBeVisible({ timeout: 10000 });

    await expect(page.getByTestId(`btn-reenter-testimony-${idTestimony}`)).toHaveCount(0);
    await expect(page.getByTestId(`btn-sign-in-testimony-${idTestimony}`)).toBeVisible();

    await page.getByTestId(`btn-sign-in-testimony-${idTestimony}`).click();
    await steps.thenShowsSuccessMessage("ingresado");
    await page.getByTestId(`btn-record-registration-${idTestimony}`).click();
    await steps.thenShowsSuccessMessage("inscripción registrada");
    await page.getByTestId(`btn-withdraw-testimony-${idTestimony}`).click();
    await steps.thenModalIsVisible();
    await page.getByTestId("input-card-number").fill("456");
    await page.getByTestId("btn-confirm-withdraw").click();
    await steps.thenShowsSuccessMessage("retirado");

    await expect(page.getByTestId(`btn-reenter-testimony-${idTestimony}`)).toBeVisible();
    await page.getByTestId(`btn-reenter-testimony-${idTestimony}`).click();

    await page.getByTestId("input-reentry-carton").fill("77");
    await page.getByTestId("checkbox-reentry-observed").check();
    await expect(page.getByTestId("btn-confirm-reentry")).toBeDisabled();
    await page.getByTestId("input-reentry-notes").fill("Falta sello del registro");
    await expect(page.getByTestId("btn-confirm-reentry")).toBeEnabled();
    await page.getByTestId("btn-confirm-reentry").click();
    await steps.thenShowsSuccessMessage("reingresado");
    await expect(row).toContainText(/ingresado/i);
  });

  for (const viewport of [
    { width: 320, height: 568, label: "320px (mobile)" },
    { width: 768, height: 1024, label: "768px (tablet)" },
    { width: 1024, height: 768, label: "1024px (desktop)" },
  ]) {
    test(`testimony-movements screen has no horizontal overflow at ${viewport.label}`, async ({ page }) => {
      await page.setViewportSize({ width: viewport.width, height: viewport.height });
      await steps.givenUserIsOnPage("/dashboard/testimony-movements");
      await page.waitForLoadState("networkidle");
      await steps.thenHasNoHorizontalOverflow();
    });
  }
});
