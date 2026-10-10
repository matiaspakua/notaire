/**
 * Playwright E2E — the audit log is paginated server-side (#1340, RF-44).
 * The list used to request size=1000 and silently hide every older record.
 * It now shows 20 rows, the real total, and reaches the oldest entry.
 */
import { test, expect } from "@playwright/test";
import { establishAdminBrowserSession } from "./setup/auth";
import { apiGet } from "./setup/api-helpers";

interface AuditPage {
  content: { idAuditRecord: number }[];
  totalElements: number;
}

test.describe("Audit log pagination (#1340)", () => {
  test("shows a page of 20 with the total and reaches the oldest record", async ({ page }) => {
    await establishAdminBrowserSession(page);
    const first = await apiGet<AuditPage>(page, "/audit-log?page=0&size=20&sort=date,desc");
    expect(first.ok).toBe(true);
    const oldest = await apiGet<AuditPage>(page, "/audit-log?page=0&size=1&sort=date,asc");
    const oldestId = oldest.data!.content[0].idAuditRecord;

    const requested: string[] = [];
    page.on("request", (r) => {
      if (r.url().includes("/audit-log")) requested.push(r.url());
    });

    await page.goto("/dashboard/auditoria");
    await page.waitForLoadState("networkidle");
    const table = page.getByRole("table");
    await expect(table.locator("tbody tr")).toHaveCount(20);
    const nav = page.getByRole("navigation", { name: /paginaci[oó]n|pagination/i });
    const status = nav.getByTestId("pagination-status");
    await expect(status).toContainText(/1[–-]20/);
    const shownTotal = Number((await status.innerText()).replace(/\D+$/, "").split(/\D+/).pop());
    expect(shownTotal).toBeGreaterThanOrEqual(first.data!.totalElements);
    expect(requested.some((u) => u.includes("size=1000"))).toBe(false);

    await nav.getByRole("button", { name: /última|last/i }).click();
    await expect(page).toHaveURL(/[?&]page=\d+/);
    await expect(table.getByRole("cell", { name: String(oldestId), exact: true })).toBeVisible({ timeout: 15000 });

    await nav.getByRole("button", { name: /primera|first/i }).click();
    await expect(status).toContainText(/1[–-]20/);
  });

  test("the module filter is applied by the server and keeps paging", async ({ page }) => {
    await establishAdminBrowserSession(page);
    const people = await apiGet<AuditPage>(page, "/audit-log?page=0&size=1&module=People");
    test.skip(!people.ok || people.data!.totalElements === 0, "no People audit records in this database");

    await page.goto("/dashboard/auditoria");
    await page.waitForLoadState("networkidle");
    const moduleSelect = page.getByLabel(/m[oó]dulo|module/i);
    await moduleSelect.selectOption("People");
    await expect(page).toHaveURL(/module=People/);
    const status = page.getByTestId("pagination-status");
    await expect(status).toContainText(String(people.data!.totalElements));
    const badges = page.getByRole("table").locator("tbody tr td:nth-child(4)");
    for (const text of await badges.allInnerTexts()) expect(text).toBe("People");
  });
});
