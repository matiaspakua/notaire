/**
 * CU22 - Register a substitution (Gherkin style)
 * CU59 - View substitutions (#1546: year + notary filters)
 */
import { test, expect, type Page } from "@playwright/test";
import { GherkinSteps } from "./gherkin-helpers";
import { createPerson, createSubstitution } from "./setup/api-helpers";

test.describe("CU22 - Register a substitution", () => {
  let steps: GherkinSteps;

  test.beforeEach(async ({ page }) => {
    steps = new GherkinSteps(page);
    await steps.givenUserIsLoggedIn();
    await steps.givenDashboardIsLoaded();
    // Discovery via primary navigation (#1058) — not a hard deep link.
    await steps.whenUserOpensSidebarNav("suplencias");
  });

  test("CU22-GW01: Given the substitutions page, When new substitution is clicked, Then the modal opens", async () => {
    // Given
    await steps.givenModuleIsVisible("Suplencias");

    // When
    await steps.whenUserClicksButton("nueva suplencia");

    // Then — modal uses IDs for notary and substitute (numeric inputs), and a start and end date range
    await steps.thenModalIsVisible("Nueva suplencia");
    await expect(steps.page.getByTestId("input-notary-id")).toBeVisible();
    await expect(steps.page.getByTestId("input-substitute-id")).toBeVisible();
    await expect(steps.page.getByTestId("input-start-date")).toBeVisible();
    await expect(steps.page.getByTestId("input-end-date")).toBeVisible();
  });

  test("CU22-GW02: Given the form is open, When it is filled and submitted, Then the substitution is created", async () => {
    // Given
    await steps.whenUserClicksButton("nueva suplencia");
    await steps.thenModalIsVisible();

    // When — fill notary ID, substitute ID, and the date range via testids
    await steps.page.getByTestId("input-notary-id").fill("1");
    await steps.page.getByTestId("input-substitute-id").fill("1");
    await steps.page.getByTestId("input-start-date").fill("2026-05-01");
    await steps.page.getByTestId("input-end-date").fill("2026-05-31");
    await steps.page.getByTestId("btn-save-substitution").click();

    // Then — wait for modal to close, then verify success and table
    await steps.thenShowsSuccessMessage("registrada");
    await expect(steps.page.getByRole("dialog")).not.toBeVisible({ timeout: 5000 });
    await steps.thenTableIsVisible();
  });
});

test.describe("CU59 - Look up substitutions", () => {
  let steps: GherkinSteps;

  test.beforeEach(async ({ page }) => {
    steps = new GherkinSteps(page);
    await steps.givenUserIsLoggedIn();
    await steps.givenDashboardIsLoaded();
    await steps.whenUserOpensSidebarNav("suplencias");
  });

  async function seedTwo(page: Page) {
    const a = await createPerson(page);
    const b = await createPerson(page);
    const helper = await createPerson(page);
    const [idA, idB, idH] = [a.data!.personId!, b.data!.personId!, helper.data!.personId!];
    // A spans Dec 2019 → Jan 2020 (overlaps both years); B is in 2018.
    const sa = await createSubstitution(page, idH, idA, { dateStart: "2019-12-20", dateEnd: "2020-01-05" });
    const sb = await createSubstitution(page, idH, idB, { dateStart: "2018-03-01", dateEnd: "2018-03-05" });
    expect(sa.ok && sb.ok, sa.error ?? sb.error).toBe(true);
    return { lastA: a.data!.lastName!, lastB: b.data!.lastName! };
  }

  test("CU59-GW01 (#1546): Given substitutions in different years, When filtered by year and notary, Then only the overlapping ones are shown", async ({ page }) => {
    const { lastA, lastB } = await seedTwo(page);
    const rows = page.locator("table tbody tr");

    await steps.givenUserIsOnPage("/dashboard/substitutions");
    await page.getByTestId("input-substitutions-notary").fill(lastA);
    await expect(page).toHaveURL(new RegExp(`notary=${lastA}`));
    await page.getByTestId("select-substitutions-year").selectOption("2020");
    await expect(page).toHaveURL(/year=2020/);
    await expect(rows).toHaveCount(1);
    await expect(rows.first()).toContainText(lastA);

    // The Dec→Jan substitution also belongs to 2019; reload keeps the filters.
    await page.getByTestId("select-substitutions-year").selectOption("2019");
    await page.reload();
    await expect(rows).toHaveCount(1);
    await expect(page.getByTestId("substitutions-count")).toContainText("1");

    // B's notary in 2020: empty state.
    await page.getByTestId("input-substitutions-notary").fill(lastB);
    await page.getByTestId("select-substitutions-year").selectOption("2020");
    await expect(page.getByText("No hay suplencias para 2020").first()).toBeVisible();
  });

  test("CU59-GW02 (#1546): Given a substitution, When its row is opened, Then the details are shown in the dialog", async ({ page }) => {
    // The edit dialog is the detail view (dates and both notaries); no separate "ver" action.
    const { lastA } = await seedTwo(page);
    await steps.givenUserIsOnPage(`/dashboard/substitutions?notary=${lastA}`);
    const row = page.locator("table tbody tr").filter({ hasText: lastA });
    await row.getByRole("button", { name: /editar/i }).click();
    const dialog = page.getByRole("dialog");
    await expect(dialog).toBeVisible();
    await expect(dialog.getByTestId("input-start-date")).toHaveValue("2019-12-20");
    await expect(dialog.getByTestId("input-end-date")).toHaveValue("2020-01-05");
  });

  test("CU59-GW03 (#1546): the filters work at 390px", async ({ page }) => {
    const { lastA } = await seedTwo(page);
    await page.setViewportSize({ width: 390, height: 844 });
    await steps.givenUserIsOnPage(`/dashboard/substitutions?year=2020&notary=${lastA}`);
    await expect(page.getByTestId("substitutions-count")).toContainText("1");
    const overflow = await page.evaluate(() => document.documentElement.scrollWidth - window.innerWidth);
    expect(overflow).toBeLessThanOrEqual(1);
  });
});
