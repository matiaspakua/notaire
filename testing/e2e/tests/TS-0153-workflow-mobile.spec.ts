/**
 * TS-0153 - Workflow editor on a phone (390px)
 *
 * Covers: CU70 (define a management-status workflow)
 * Issue: #1580 (epic #1573)
 *
 * Golden path: at 390px the page does not overflow horizontally, the editor opens in the list
 * view, a transition can be added there, and the diagram is read-only (no dragging).
 */
import { test, expect, type Page } from "@playwright/test";
import { authenticateAsAdmin } from "./setup/auth";
import { apiGet, apiPost, apiDelete } from "./setup/api-helpers";

type Status = { idManagementStatus: number; name: string };

async function cleanup(page: Page, wfId: number) {
  const trs = await apiGet<Array<{ id: number }>>(page, `/workflow-transition/by-workflow/${wfId}`);
  for (const t of trs.data ?? []) await apiDelete(page, `/workflow-transition/${t.id}`);
  const nodes = await apiGet<Array<{ id: number }>>(page, `/workflow-node/by-workflow/${wfId}`);
  for (const n of nodes.data ?? []) await apiDelete(page, `/workflow-node/${n.id}`);
  await apiDelete(page, `/workflow-definition/${wfId}`);
}

/** No horizontal page scroll, and every header action fits inside the viewport. */
const noOverflow = (page: Page) =>
  page.evaluate(() => {
    if (document.documentElement.scrollWidth > window.innerWidth + 1) return false;
    return Array.from(document.querySelectorAll("header button, header a")).every((el) => {
      const r = el.getBoundingClientRect();
      return r.right <= window.innerWidth + 1 && r.left >= -1;
    });
  });

test.describe("TS-0153 - Workflow editor on a phone (#1580)", () => {
  test.use({ viewport: { width: 390, height: 844 } });

  test.beforeEach(async ({ page }) => {
    await authenticateAsAdmin(page);
  });

  test("list view by default, no overflow, read-only graph", async ({ page }) => {
    const [s1, s2] = (await apiGet<Status[]>(page, "/management-statuses")).data!;
    const wfId = (await apiPost<{ id: number }>(page, "/workflow-definition", { name: `TS-0153 ${Date.now()}`, active: false })).data!.id;
    try {
      for (const [s, type] of [[s1, "INITIAL"], [s2, "FINAL"]] as const) {
        await apiPost(page, "/workflow-node", { workflowDefinitionId: wfId, statusManagementId: s.idManagementStatus, type });
      }
      await page.goto(`/dashboard/administration/workflows/${wfId}`);
      await expect(page.getByTestId("wf-list-view")).toBeVisible({ timeout: 15000 });
      await expect(page.getByTestId("wf-tab-list")).toHaveAttribute("aria-selected", "true");
      expect(await noOverflow(page)).toBe(true);

      await page.getByTestId("btn-toggle-edit").click();
      expect(await noOverflow(page)).toBe(true);
      await page.getByTestId("wf-list-add-from").selectOption({ label: s1.name });
      await page.getByTestId("wf-list-add-to").selectOption({ label: s2.name });
      await page.getByTestId("wf-list-add-transition").click();
      await expect(page.getByRole("rowheader", { name: `${s1.name} → ${s2.name}` })).toBeVisible();
      await expect(page.getByTestId("workflow-validity")).toBeVisible();
      expect(await noOverflow(page)).toBe(true);

      // the diagram is read-only on a phone, even in edit mode
      await page.getByTestId("wf-tab-graph").click();
      await expect(page.getByTestId("wf-mobile-readonly")).toBeVisible();
      await expect(page.locator(".react-flow__node").first()).toBeAttached({ timeout: 15000 });
      await expect(page.locator(".react-flow__node.draggable")).toHaveCount(0);
      expect(await noOverflow(page)).toBe(true);
    } finally {
      await cleanup(page, wfId);
    }
  });
});
