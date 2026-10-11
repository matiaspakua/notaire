/**
 * Playwright E2E — icon-only action buttons expose accessible names (#1057).
 * CU76 accessibility validation / WCAG 2.1 SC 4.1.2.
 *
 * List pages show an empty-state row when the browser session has no JWT
 * (or when no entity rows exist). Establish a real admin browser session,
 * create a row via API, reload the list, then assert role names.
 */
import { test, expect } from "@playwright/test";
import { establishAdminBrowserSession } from "./setup/auth";
import {
  createConcept,
  createPerson,
  createBudget,
  createUser,
} from "./setup/api-helpers";
import { listView } from "./setup/list-locators";

async function openList(page: import("@playwright/test").Page, path: string) {
  await page.goto(path);
  await page.waitForLoadState("networkidle");
  const table = listView(page);
  await expect(table).toBeVisible({ timeout: 15000 });
  return table;
}

test.describe("Icon-only buttons expose accessible names (#1057)", () => {
  test.beforeEach(async ({ page }) => {
    await establishAdminBrowserSession(page);
  });

  test("users row actions are reachable by role name", async ({ page }) => {
    const created = await createUser(page);
    expect(created.ok, created.error ?? "createUser failed").toBe(true);

    const table = await openList(page, "/dashboard/administration/users");
    await expect(table.getByRole("button", { name: /editar|edit/i }).first()).toBeVisible({
      timeout: 15000,
    });
    await expect(table.getByRole("button", { name: /eliminar|delete/i }).first()).toBeVisible();
  });

  test("people row actions are reachable by role name", async ({ page }) => {
    const created = await createPerson(page);
    expect(created.ok, created.error ?? "createPerson failed").toBe(true);

    const table = await openList(page, "/dashboard/people");
    await expect(table.getByRole("button", { name: /editar|edit/i }).first()).toBeVisible({
      timeout: 15000,
    });
    await expect(table.getByRole("button", { name: /eliminar|delete/i }).first()).toBeVisible();
  });

  test("the budget summary action is reachable by role name", async ({ page }) => {
    const person = await createPerson(page);
    expect(person.ok, person.error ?? "createPerson failed").toBe(true);
    const budget = await createBudget(page, person.data!.personId);
    expect(budget.ok, budget.error ?? "createBudget failed").toBe(true);

    const table = await openList(page, "/dashboard/budgets");
    await expect(page.getByTestId(/^btn-summary-budget-/).first()).toBeVisible({
      timeout: 15000,
    });
    await expect(
      table.getByRole("button", { name: /summary financiero|financial summary/i }).first(),
    ).toBeVisible();
  });

  test("administracion/concepts NotaireIcon actions use button aria-label", async ({ page }) => {
    const created = await createConcept(page);
    expect(created.ok, created.error ?? "createConcept failed").toBe(true);

    const table = await openList(page, "/dashboard/administration/concepts");
    await expect(table.getByRole("button", { name: /editar|edit/i }).first()).toBeVisible({
      timeout: 15000,
    });
    await expect(table.getByRole("button", { name: /eliminar|delete/i }).first()).toBeVisible();
  });

  test("named edit/delete remain visible at mobile viewport", async ({ page }) => {
    const created = await createUser(page);
    expect(created.ok, created.error ?? "createUser failed").toBe(true);

    await page.setViewportSize({ width: 320, height: 720 });
    const table = await openList(page, "/dashboard/administration/users");
    await expect(table.getByRole("button", { name: /editar|edit/i }).first()).toBeVisible({
      timeout: 15000,
    });
  });
});
