/**
 * CU07/CU08 - Generate and verify a testimony (issue #832)
 *
 * Golden path: generate a testimony from a signed deed, verify it
 * without notes, then issue a copy. Edge paths: verify with notes;
 * issue a copy before verification (blocked, per design.md — Playwright
 * Strategy).
 */
import { test, expect } from "@playwright/test";
import { GherkinSteps } from "./gherkin-helpers";
import { apiGet, apiPost, uniqueId } from "./setup/api-helpers";
import { chooseDeed } from "./setup/deed-picker";

interface TestimonyApiResult {
  idTestimony: number;
  deed?: { number?: number };
}

async function seedSignedDeed(page: import("@playwright/test").Page): Promise<{ idDeed: number; number: number }> {
  const number = uniqueId() % 1_000_000;
  const seeded = await apiPost<{ idDeed: number }>(page, "/deeds", {
    number: number,
    dateDeedrecording: new Date().toISOString().split("T")[0],
    body: `E2E content ${number}`,
    status: "Firmada",
  });
  return { idDeed: seeded.data!.idDeed, number };
}

test.describe("CU07/CU08 - Generate and verify a testimony", () => {
  let steps: GherkinSteps;

  test.beforeEach(async ({ page }) => {
    steps = new GherkinSteps(page);
    await steps.givenUserIsLoggedIn();
  });

  test("Golden path: generate a testimony, verify it without notes, and issue a copy", async ({ page }) => {
    const { number } = await seedSignedDeed(page);

    await steps.givenUserIsOnPage("/dashboard/testimonies");
    await page.getByTestId("btn-generate-testimony").click();
    await steps.thenModalIsVisible();

    await chooseDeed(page, "select-deed-testimony", number);
    await page.getByTestId("btn-save-testimony").click();
    await steps.thenShowsSuccessMessage("generado");
    await steps.thenModalIsNotVisible();

    const row = page.getByRole("row", { name: new RegExp(String(number)) });
    await expect(row).toBeVisible({ timeout: 10000 });
    const list = await apiGet<TestimonyApiResult[]>(page, "/testimonies");
    const idTestimony = list.data!.find((te) => te.deed?.number === number)!.idTestimony;

    await page.getByTestId(`btn-verify-testimony-${idTestimony}`).click();
    await steps.thenModalIsVisible();
    await page.getByTestId("btn-confirm-verify-testimony").click();
    await steps.thenShowsSuccessMessage("verificado");

    await expect(row).toContainText(/verificado/i);
    const downloadPromise = page.waitForEvent("download");
    await page.getByTestId(`btn-issue-copy-${idTestimony}`).click();
    const download = await downloadPromise;
    expect(download.suggestedFilename()).toMatch(/^testimonio_\d+_copia\.pdf$/);
  });

  test("Edge: verifying with notes shows the testimony as Observado", async ({ page }) => {
    const { idDeed, number } = await seedSignedDeed(page);
    const generated = await apiPost<{ idTestimony: number }>(page, `/testimonies/${idDeed}/generate`, {});
    const idTestimony = generated.data!.idTestimony;

    await steps.givenUserIsOnPage("/dashboard/testimonies");
    const row = page.getByRole("row", { name: new RegExp(String(number)) });
    await expect(row).toBeVisible({ timeout: 10000 });

    await page.getByTestId(`btn-verify-testimony-${idTestimony}`).click();
    await steps.thenModalIsVisible();
    await page.getByTestId("checkbox-observed-testimony").click();
    await page.getByTestId("input-notes-testimony").fill("Falta firma del otorgante");
    await page.getByTestId("btn-confirm-verify-testimony").click();
    await steps.thenShowsSuccessMessage("verificado");

    await expect(row).toContainText(/observado/i);
  });

  test("Edge: issuing a copy stays blocked until the testimony is verified", async ({ page }) => {
    const { idDeed, number } = await seedSignedDeed(page);
    const generated = await apiPost<{ idTestimony: number }>(page, `/testimonies/${idDeed}/generate`, {});
    const idTestimony = generated.data!.idTestimony;

    await steps.givenUserIsOnPage("/dashboard/testimonies");
    const row = page.getByRole("row", { name: new RegExp(String(number)) });
    await expect(row).toBeVisible({ timeout: 10000 });

    await expect(page.getByTestId(`btn-issue-copy-${idTestimony}`)).toHaveCount(0);
    await expect(page.getByTestId(`btn-verify-testimony-${idTestimony}`)).toBeVisible();
  });

  for (const viewport of [
    { width: 320, height: 568, label: "320px (mobile)" },
    { width: 768, height: 1024, label: "768px (tablet)" },
    { width: 1024, height: 768, label: "1024px (desktop)" },
  ]) {
    test(`testimonies screen has no horizontal overflow at ${viewport.label}`, async ({ page }) => {
      await page.setViewportSize({ width: viewport.width, height: viewport.height });
      await steps.givenUserIsOnPage("/dashboard/testimonies");
      await page.waitForLoadState("networkidle");
      await steps.thenHasNoHorizontalOverflow();
    });
  }
});
