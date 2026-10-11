/**
 * TS-0126 - Full management edit (issue #1360)
 * CU53 - Modificar gestión; CU22/CU59 - substitution redirect on edit
 *
 * "Editar" used to show only Número and PUT { number }. It now prefills every field from
 * GET /managements/{id}/complete-case and saves through PUT /managements/{id}/complete-case.
 * The status is read-only there: status changes go through the workflow transition (CU83).
 */
import { type Page, test, expect } from "@playwright/test";
import { authenticateAsAdmin } from "./setup/auth";
import {
  apiGet,
  createCompleteCaseManagement,
  createPerson,
  createBudget,
  createSubstitution,
  uniqueId,
} from "./setup/api-helpers";

async function seedCase(page: Page, notaryLastName: string) {
  const notary = await createPerson(page, { lastName: notaryLastName });
  const client = await createPerson(page, { lastName: `Cli1360${uniqueId() % 100_000}` });
  const budget = await createBudget(page, client.data!.personId);
  const created = await createCompleteCaseManagement(page, {
    budgetId: budget.data!.idBudget,
    notaryId: notary.data!.personId,
    notes: "kept by the edit",
  });
  expect(created.ok).toBe(true);
  return { notary, client, budget, management: created.data! };
}

async function openEdit(page: Page, idManagement: number) {
  await page.goto("/dashboard/managements");
  await page.waitForLoadState("domcontentloaded");
  await page.getByTestId(`btn-edit-management-${idManagement}`).click();
  const dialog = page.getByRole("dialog");
  await expect(dialog).toBeVisible();
  return dialog;
}

async function pickPerson(page: Page, testId: string, lastName: string) {
  const input = page.getByTestId(testId);
  await input.click();
  await input.fill(lastName);
  const option = page.getByRole("option", { name: new RegExp(lastName, "i") });
  await expect(option).toBeVisible({ timeout: 8000 });
  await option.click();
}

test.describe("TS-0126 full management edit (#1360)", () => {
  test.beforeEach(async ({ page }) => {
    await authenticateAsAdmin(page);
  });

  test("the edit dialog prefills every field and the status is read-only", async ({ page }) => {
    const notaryLast = `Esc1360${uniqueId() % 100_000}`;
    const { client, management } = await seedCase(page, notaryLast);
    await openEdit(page, management.idManagement);

    await expect(page.getByTestId("input-management-number")).toHaveValue(String(management.number));
    await expect(page.getByTestId("select-notary-management")).toHaveValue(new RegExp(notaryLast, "i"), { timeout: 8000 });
    await expect(page.getByTestId("select-budget-management")).toHaveValue(new RegExp(client.data!.lastName!, "i"), { timeout: 8000 });
    await expect(page.getByTestId("select-procedure-type-management")).not.toHaveText(/selec/i);
    await expect(page.getByTestId("select-status-management")).toBeDisabled();
  });

  test("changing the notary persists and keeps the notes", async ({ page }) => {
    const { management } = await seedCase(page, `EscA1360${uniqueId() % 100_000}`);
    const other = await createPerson(page, { lastName: `EscB1360${uniqueId() % 100_000}` });
    await openEdit(page, management.idManagement);
    await expect(page.getByTestId("select-notary-management")).toHaveValue(/EscA1360/i, { timeout: 8000 });

    await pickPerson(page, "select-notary-management", other.data!.lastName!);
    await page.getByTestId("btn-save-management").click();
    await expect(page.locator("[data-sonner-toast]").getByText(/actualizada/i)).toBeVisible({ timeout: 8000 });

    const after = await apiGet(page, `/managements/${management.idManagement}/complete-case`);
    expect(after.ok).toBe(true);
    expect(after.data.notaryId).toBe(other.data!.personId);
    expect(after.data.notes).toBe("kept by the edit");
  });

  test("editing to a notary with an active substitution shows the redirect toast", async ({ page }) => {
    const { management } = await seedCase(page, `EscC1360${uniqueId() % 100_000}`);
    const absent = await createPerson(page, { lastName: `Ausente1360${uniqueId() % 100_000}` });
    const substitute = await createPerson(page, { lastName: `Suplente1360${uniqueId() % 100_000}` });
    const today = new Date();
    const tomorrow = new Date(today.getTime() + 24 * 60 * 60 * 1000);
    // Substitution coverage is checked against the management start date (today).
    const yesterday = new Date(today.getTime() - 24 * 60 * 60 * 1000);
    const sub = await createSubstitution(page, substitute.data!.personId, absent.data!.personId, {
      dateStart: yesterday.toISOString().split("T")[0],
      dateEnd: tomorrow.toISOString().split("T")[0],
    });
    expect(sub.ok).toBe(true);

    await openEdit(page, management.idManagement);
    await expect(page.getByTestId("select-notary-management")).toHaveValue(/EscC1360/i, { timeout: 8000 });
    await pickPerson(page, "select-notary-management", absent.data!.lastName!);
    await page.getByTestId("btn-save-management").click();

    await expect(page.locator("[data-sonner-toast]").getByText(/actualizada/i)).toBeVisible({ timeout: 8000 });
    await expect(page.locator("[data-sonner-toast]").getByText(/suplente/i).first()).toBeVisible({ timeout: 8000 });
    const after = await apiGet(page, `/managements/${management.idManagement}/complete-case`);
    expect(after.data.notaryId).toBe(substitute.data!.personId);
  });
});
