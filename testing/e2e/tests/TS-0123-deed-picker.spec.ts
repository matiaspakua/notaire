/**
 * Playwright E2E — deed pickers search the server (#1340 slice 8b, CU07/CU08/CU87).
 * The testimony, registration-draft and folio forms filtered
 * GET /deeds?size=1000 to signed deeds in the browser, so once the table
 * passed 1000 deeds a newly signed deed could not be chosen.
 */
import { test, expect, type Page } from "@playwright/test";
import { establishAdminBrowserSession } from "./setup/auth";
import { apiPost, createFolio, uniqueId } from "./setup/api-helpers";
import { chooseDeed, searchDeed } from "./setup/deed-picker";

async function seedDeed(page: Page, status: string): Promise<{ id: number; number: number }> {
  const number = uniqueId();
  const seeded = await apiPost<{ idDeed: number }>(page, "/deeds", {
    number,
    dateDeedrecording: new Date().toISOString().split("T")[0],
    body: `TS-0123 deed picker seed ${number}`,
    status,
    // Non-sequential numbers need this justification (CU86).
    notes: "Non-sequential numbering: isolated E2E data seed (CU86)",
  });
  expect(seeded.ok, seeded.error).toBe(true);
  return { id: seeded.data!.idDeed, number };
}

function trackDeedRequests(page: Page): string[] {
  const requested: string[] = [];
  page.on("request", (r) => {
    if (/\/api\/v1\/deeds/.test(r.url())) requested.push(r.url());
  });
  return requested;
}

test.describe("Deed picker (#1340)", () => {
  test.beforeEach(async ({ page }) => {
    await establishAdminBrowserSession(page);
  });

  test("the testimony form finds a signed deed by number and offers no unsigned one", async ({ page }) => {
    const signed = await seedDeed(page, "Firmada");
    const unsigned = await seedDeed(page, "Sin Firmar");
    const requested = trackDeedRequests(page);

    await page.goto("/dashboard/testimonies");
    await page.waitForLoadState("networkidle");
    await page.getByTestId("btn-generate-testimony").click();
    const picker = page.getByRole("dialog").getByTestId("select-deed-testimony");
    await expect(picker).toHaveAttribute("aria-autocomplete", "list");

    const none = await searchDeed(page, "select-deed-testimony", unsigned.number);
    await expect(page.getByText(/^(sin resultados|no results)$/i)).toBeVisible({ timeout: 8000 });
    await expect(none).toHaveCount(0);
    await chooseDeed(page, "select-deed-testimony", signed.number);
    await expect(page.getByTestId("btn-save-testimony")).toBeEnabled();
    expect(requested.some((u) => u.includes("size=1000"))).toBe(false);
    expect(requested.some((u) => u.includes("status=Firmada"))).toBe(true);
  });

  test("the registration-draft form finds a signed deed by number", async ({ page }) => {
    const signed = await seedDeed(page, "Firmada");
    const requested = trackDeedRequests(page);

    await page.goto("/dashboard/registration-drafts");
    await page.waitForLoadState("networkidle");
    await chooseDeed(page, "select-deed-registration-draft", signed.number);
    await expect(page.getByTestId("btn-generate-registration-draft")).toBeEnabled();
    expect(requested.some((u) => u.includes("size=1000"))).toBe(false);
  });

  test("the folio form hides a deed already linked to another folio", async ({ page }) => {
    const linked = await seedDeed(page, "Firmada");
    const free = await seedDeed(page, "Firmada");
    const folio = await createFolio(page, 1, { status: "Nuevo", deedId: linked.id });
    expect(folio.ok, folio.error).toBe(true);
    const requested = trackDeedRequests(page);

    await page.goto("/dashboard/administration/folios");
    await page.waitForLoadState("networkidle");
    await page.getByTestId("btn-new-folio").click();
    const none = await searchDeed(page, "select-deed-folio", linked.number);
    await expect(page.getByText(/^(sin resultados|no results)$/i)).toBeVisible({ timeout: 8000 });
    await expect(none).toHaveCount(0);
    await chooseDeed(page, "select-deed-folio", free.number);
    expect(requested.some((u) => u.includes("size=1000"))).toBe(false);
  });
});
