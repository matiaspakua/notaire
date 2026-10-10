/**
 * Playwright E2E — budget pickers search the server (#1340, CU02/CU15).
 * The payment and management forms listed GET /presupuestos?size=1000 (newest
 * first), so on a database with more budgets the older ones could not be chosen.
 */
import { test, expect, type Page } from "@playwright/test";
import { establishAdminBrowserSession } from "./setup/auth";
import { apiGet, createPersona, createPresupuesto } from "./setup/api-helpers";

function trackBudgetRequests(page: Page): string[] {
  const requested: string[] = [];
  page.on("request", (r) => {
    if (/\/api\/v1\/presupuestos/.test(r.url())) requested.push(r.url());
  });
  return requested;
}

test.describe("Budget picker (#1340)", () => {
  test("the payment form finds the oldest budget by its number and shows its balance", async ({ page }) => {
    await establishAdminBrowserSession(page);
    const oldest = await apiGet<{ content: { idBudget: number }[] }>(page, "/presupuestos?page=0&size=1&sort=idBudget,asc");
    expect(oldest.ok, oldest.error).toBe(true);
    const id = oldest.data!.content[0].idBudget;
    const requested = trackBudgetRequests(page);

    await page.goto("/dashboard/pagos");
    await page.waitForLoadState("networkidle");
    await page.getByTestId("btn-nuevo-pago").click();
    const dialog = page.getByRole("dialog");
    const picker = dialog.getByRole("combobox", { name: /presupuesto|budget/i });
    await expect(picker).toHaveAttribute("aria-expanded", "false");
    await picker.click();
    await expect(picker).toHaveAttribute("aria-expanded", "true");
    await picker.fill(String(id));
    const option = dialog.getByRole("option", { name: new RegExp(`^#${id} `) });
    await expect(option).toBeVisible({ timeout: 8000 });
    await picker.press("ArrowDown");
    await picker.press("Enter");
    await expect(picker).toHaveValue(new RegExp(`^#${id} `));
    await expect(picker).toHaveAttribute("aria-expanded", "false");
    await expect(dialog.getByText(/saldo pendiente/i)).toBeVisible({ timeout: 8000 });
    expect(requested.some((u) => u.includes("size=1000"))).toBe(false);
  });

  test("the management form finds a new client's budget by the client's name", async ({ page }) => {
    await establishAdminBrowserSession(page);
    const person = await createPersona(page, { isClient: true });
    expect(person.ok, person.error).toBe(true);
    const budget = await createPresupuesto(page, person.data!.personId);
    expect(budget.ok, budget.error).toBe(true);
    const lastName = person.data!.lastName!;
    const requested = trackBudgetRequests(page);

    await page.goto("/dashboard/gestiones");
    await page.waitForLoadState("networkidle");
    await page.getByTestId("btn-nueva-gestion").click();
    const dialog = page.getByRole("dialog");
    const picker = dialog.getByTestId("select-presupuesto-gestion");
    await expect(picker).toHaveAttribute("role", "combobox");
    await picker.click();
    await picker.fill(lastName);
    const option = dialog.getByRole("option", { name: new RegExp(`^#${budget.data!.idBudget} .*${lastName}`) });
    await expect(option).toBeVisible({ timeout: 8000 });
    await option.click();
    await expect(picker).toHaveValue(new RegExp(lastName));
    await expect(dialog).toBeVisible();
    expect(requested.some((u) => new RegExp(`/presupuestos/persona/${person.data!.personId}$`).test(u))).toBe(true);
    expect(requested.some((u) => u.includes("size=1000"))).toBe(false);
  });
});
