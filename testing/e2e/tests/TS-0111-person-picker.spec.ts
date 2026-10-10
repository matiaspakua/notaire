/**
 * Playwright E2E — person pickers search the server (#1340 slice 3, CU02/CU19/CU39).
 * The budget client, the management notary and the management client filter
 * used to list GET /people?size=1000, so on a database with more people anyone
 * created after the 1000th could not be chosen.
 */
import { test, expect } from "@playwright/test";
import { establishAdminBrowserSession } from "./setup/auth";
import { createPersona } from "./setup/api-helpers";

test.describe("Person picker (#1340)", () => {
  test("the budget form finds a new person by name, by keyboard, and saves it", async ({ page }) => {
    await establishAdminBrowserSession(page);
    const person = await createPersona(page, { isClient: true });
    expect(person.ok, person.error).toBe(true);
    const lastName = person.data!.lastName!;

    const requested: string[] = [];
    page.on("request", (r) => {
      if (/\/api\/v1\/people/.test(r.url())) requested.push(r.url());
    });

    await page.goto("/dashboard/presupuestos");
    await page.waitForLoadState("networkidle");
    await page.getByTestId("btn-nuevo-presupuesto").click();
    const dialog = page.getByRole("dialog");
    const picker = dialog.getByRole("combobox", { name: /cliente|client/i });
    await expect(picker).toHaveAttribute("aria-expanded", "false");
    await picker.click();
    await expect(picker).toHaveAttribute("aria-expanded", "true");
    await picker.fill(lastName);
    const option = dialog.getByRole("option", { name: new RegExp(lastName) });
    await expect(option).toBeVisible({ timeout: 8000 });
    await picker.press("ArrowDown");
    await expect(picker).toHaveAttribute("aria-activedescendant", (await option.getAttribute("id"))!);
    await picker.press("Enter");
    await expect(picker).toHaveValue(new RegExp(lastName));
    await expect(picker).toHaveAttribute("aria-expanded", "false");
    await expect(dialog).toBeVisible();

    await dialog.locator('input[type="date"]').fill(new Date().toISOString().split("T")[0]);
    await dialog.getByTestId("input-monto").fill("1000");
    const post = page.waitForRequest((r) => /\/api\/v1\/presupuestos$/.test(r.url()) && r.method() === "POST");
    await dialog.getByTestId("btn-guardar-presupuesto").click();
    expect((await post).postDataJSON().person.personId).toBe(person.data!.personId);

    expect(requested.some((u) => u.includes("/people/search"))).toBe(true);
    expect(requested.some((u) => u.includes("size=1000"))).toBe(false);
  });

  test("the management client filter finds a new client by name", async ({ page }) => {
    await establishAdminBrowserSession(page);
    const person = await createPersona(page, { isClient: true });
    expect(person.ok, person.error).toBe(true);

    await page.goto("/dashboard/gestiones");
    await page.waitForLoadState("networkidle");
    const filter = page.getByRole("combobox", { name: /cliente|client/i });
    await filter.click();
    await filter.fill(person.data!.lastName!);
    const filtered = page.waitForRequest((r) =>
      new RegExp(`/api/v1/gestiones/cliente/${person.data!.personId}$`).test(r.url()),
    );
    await page.getByRole("option", { name: new RegExp(person.data!.lastName!) }).click();
    await filtered;
    await expect(filter).toHaveValue(new RegExp(person.data!.lastName!));
  });
});
