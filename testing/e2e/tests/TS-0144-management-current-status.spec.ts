/**
 * TS-0144 - CU14 (#1553): the case summary shows the management's current status,
 * read from GET /managements/{id}/current-status, and follows a status change (CU83).
 */
import { test, expect } from "@playwright/test";
import { establishAdminBrowserSession } from "./setup/auth";
import { apiPost, createPerson, createBudget, seedManagementWithWorkflow } from "./setup/api-helpers";

for (const vp of [{ name: "desktop", width: 1440, height: 900 }, { name: "390px", width: 390, height: 844 }]) {
  test(`CU14 (#1553, ${vp.name}): the case summary shows the current status after a transition`, async ({ page }) => {
    await page.setViewportSize({ width: vp.width, height: vp.height });
    await establishAdminBrowserSession(page);
    const person = await createPerson(page);
    expect(person.ok, person.error ?? "createPerson failed").toBe(true);
    const budget = await createBudget(page, person.data!.personId);
    expect(budget.ok, budget.error ?? "createBudget failed").toBe(true);
    const seeded = await seedManagementWithWorkflow(page, budget.data!.idBudget);

    // Given the management in its initial status, the summary shows it
    await page.goto("/dashboard/managements");
    await page.getByTestId(`btn-summary-case-${seeded.idManagement}`).click();
    const card = page.getByTestId("dialog-summary-case").getByTestId("management-current-status");
    await expect(card).toContainText(seeded.initialStatus, { timeout: 15000 });
    await page.keyboard.press("Escape");

    // When it moves to the final status (CU83)
    const moved = await apiPost(page, `/managements/${seeded.idManagement}/transition`, {
      statusDestination: seeded.finalStatus,
    });
    expect(moved.ok, moved.error ?? "transition failed").toBe(true);

    // Then the summary shows the new status, fitting the viewport
    await page.reload();
    await page.getByTestId(`btn-summary-case-${seeded.idManagement}`).click();
    await expect(card).toContainText(seeded.finalStatus, { timeout: 15000 });
    const overflow = await page.evaluate(() => document.documentElement.scrollWidth - window.innerWidth);
    expect(overflow).toBeLessThanOrEqual(1);
  });
}
