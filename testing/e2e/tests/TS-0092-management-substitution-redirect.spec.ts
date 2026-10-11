/**
 * TS-0092 - Practical effect of substitutions on management assignment
 * CU22 - Register a substitution
 * CU59 - Apply a substitution to a management (automatic redirect)
 * CU48 - Register a notary
 * CU51 - Update a notary
 * Issue #836
 */
import { type Page, test, expect } from "@playwright/test";
import { authenticateAsAdmin } from "./setup/auth";
import { createPerson, createBudget, createSubstitution, uniqueId } from "./setup/api-helpers";

async function choose(page: Page, triggerTestId: string, option: RegExp): Promise<void> {
  const trigger = page.getByTestId(triggerTestId);
  await trigger.click();
  // Person pickers are server-search comboboxes (#1340): type the name first.
  if ((await trigger.evaluate((el) => el.tagName)) === "INPUT") await trigger.fill(option.source);
  const choice = page.getByRole("option", { name: option });
  await expect(choice).toBeVisible({ timeout: 8000 });
  await choice.click();
}

async function chooseFirst(page: Page, triggerTestId: string): Promise<void> {
  await page.getByTestId(triggerTestId).click();
  await page.getByRole("listbox").getByRole("option").first().click();
}

// ---------------------------------------------------------------------------
// CU48/CU51 - Notary registration on the people form
// ---------------------------------------------------------------------------

test.describe("CU48/CU51 - Notary registration on people", () => {
  test.beforeEach(async ({ page }) => {
    await authenticateAsAdmin(page);
    await page.goto("/dashboard/people");
    await page.waitForLoadState("domcontentloaded");
  });

  test("CU48-GW01: registering a person with a notary registration number is saved", async ({ page }) => {
    const lastName = `Notary${uniqueId() % 100_000}`;
    const dni = `48${uniqueId() % 10_000_000}`;

    await page.getByTestId("btn-new-person").click();
    const dialog = page.getByRole("dialog");
    await expect(dialog).toBeVisible();

    await page.getByTestId("input-firstName").fill("Juan");
    await page.getByTestId("input-lastName").fill(lastName);
    await dialog.getByTestId("input-dni").fill(dni);
    await page.getByTestId("input-notary-registration").fill("123");

    await dialog.getByRole("button", { name: /crear|guardar/i }).click();
    await expect(
      page.locator("[data-sonner-toast]").getByText(/creada/i),
    ).toBeVisible({ timeout: 8000 });
  });

  test("CU51-GW01: editing a person and changing their notary registration number", async ({ page }) => {
    const person = await createPerson(page, {
      lastName: `Edit${uniqueId() % 100_000}`,
      identificationNumber: `51${uniqueId() % 10_000_000}`,
    });
    const lastName = person.data!.lastName!;

    await page.reload();
    await page.waitForLoadState("domcontentloaded");
    await page.getByTestId("input-search-lastName").fill(lastName);
    const row = page.getByRole("row", { name: new RegExp(lastName, "i") });
    await expect(row).toBeVisible({ timeout: 10000 });
    await row.getByRole("button").first().click();

    const dialog = page.getByRole("dialog");
    await expect(dialog).toBeVisible();
    await page.getByTestId("input-notary-registration").fill("456");
    await dialog.getByRole("button", { name: /actualizar|guardar/i }).click();

    await expect(
      page.locator("[data-sonner-toast]").getByText(/actualizada/i),
    ).toBeVisible({ timeout: 8000 });
  });
});

// ---------------------------------------------------------------------------
// CU22/CU59 - Automatic redirect of the management to the substitute notary
// ---------------------------------------------------------------------------

test.describe("CU22/CU59 - Management redirect when a substitution is active", () => {
  test.beforeEach(async ({ page }) => {
    await authenticateAsAdmin(page);
  });

  test("GW01: a management created for a notary with an active substitution is redirected to the substitute", async ({ page }) => {
    const notary = await createPerson(page, {
      lastName: `Substituted${uniqueId() % 100_000}`,
    });
    const substitute = await createPerson(page, {
      lastName: `Substitute${uniqueId() % 100_000}`,
    });
    const client = await createPerson(page, {
      lastName: `Client${uniqueId() % 100_000}`,
    });
    const budget = await createBudget(page, client.data!.personId);

    const today = new Date();
    const tomorrow = new Date(today.getTime() + 24 * 60 * 60 * 1000);
    const substitution = await createSubstitution(page, substitute.data!.personId, notary.data!.personId, {
      dateStart: today.toISOString().split("T")[0],
      dateEnd: tomorrow.toISOString().split("T")[0],
    });
    expect(substitution.ok).toBe(true);

    await page.goto("/dashboard/managements");
    await page.waitForLoadState("domcontentloaded");
    await page.getByTestId("btn-new-management").click();
    const dialog = page.getByRole("dialog");
    await expect(dialog).toBeVisible();

    await page.getByTestId("input-management-number").fill(String(uniqueId() % 1_000_000));
    await choose(page, "select-budget-management", new RegExp(client.data!.lastName!, "i"));
    await choose(page, "select-notary-management", new RegExp(notary.data!.lastName!, "i"));
    await chooseFirst(page, "select-status-management");
    await chooseFirst(page, "select-procedure-type-management");

    await page.getByTestId("btn-save-management").click();

    await expect(
      page.locator("[data-sonner-toast]").getByText(/creada/i),
    ).toBeVisible({ timeout: 8000 });
    await expect(
      page.locator("[data-sonner-toast]").getByText(/suplente/i).first(),
    ).toBeVisible({ timeout: 8000 });
  });

  test("EDGE: a management created for a notary without an active substitution does not show a redirect notice", async ({ page }) => {
    const notary = await createPerson(page, {
      lastName: `NoSubstitution${uniqueId() % 100_000}`,
    });
    const client = await createPerson(page, {
      lastName: `ClientNorm${uniqueId() % 100_000}`,
    });
    await createBudget(page, client.data!.personId);

    await page.goto("/dashboard/managements");
    await page.waitForLoadState("domcontentloaded");
    await page.getByTestId("btn-new-management").click();
    const dialog = page.getByRole("dialog");
    await expect(dialog).toBeVisible();

    await page.getByTestId("input-management-number").fill(String(uniqueId() % 1_000_000));
    await choose(page, "select-budget-management", new RegExp(client.data!.lastName!, "i"));
    await choose(page, "select-notary-management", new RegExp(notary.data!.lastName!, "i"));
    await chooseFirst(page, "select-status-management");
    await chooseFirst(page, "select-procedure-type-management");

    await page.getByTestId("btn-save-management").click();

    await expect(
      page.locator("[data-sonner-toast]").getByText(/creada/i),
    ).toBeVisible({ timeout: 8000 });
    await expect(
      page.locator("[data-sonner-toast]").getByText(/suplente/i),
    ).not.toBeVisible();
  });
});
