/**
 * TS-0151 - Workflow editor: validation on the canvas, activation blocked while invalid
 *
 * Covers: CU70 (define a management-status workflow)
 * Issue: #1577 (epic #1573)
 *
 * Golden path: a workflow with an isolated status shows that box marked on the canvas with a
 * Spanish message and lists the problem; trying to activate it from the workflows list is
 * blocked with the errors; once the isolated status is removed it can be activated.
 */
import { test, expect, type Page } from "@playwright/test";
import { authenticateAsAdmin } from "./setup/auth";
import { apiGet, apiPost, apiDelete } from "./setup/api-helpers";

type Status = { idManagementStatus: number };

async function cleanup(page: Page, wfId: number) {
  const trs = await apiGet<Array<{ id: number }>>(page, `/workflow-transition/by-workflow/${wfId}`);
  for (const t of trs.data ?? []) await apiDelete(page, `/workflow-transition/${t.id}`);
  const nodes = await apiGet<Array<{ id: number }>>(page, `/workflow-node/by-workflow/${wfId}`);
  for (const n of nodes.data ?? []) await apiDelete(page, `/workflow-node/${n.id}`);
  await apiDelete(page, `/workflow-definition/${wfId}`);
}

test.describe("TS-0151 - Workflow canvas validation (#1577)", () => {
  test.beforeEach(async ({ page }) => {
    await authenticateAsAdmin(page);
  });

  test("an isolated status is marked and blocks activation", async ({ page }) => {
    const [s1, s2, s3] = (await apiGet<Status[]>(page, "/management-statuses")).data!;
    const name = `TS-0151 ${Date.now()}`;
    const wfId = (await apiPost<{ id: number }>(page, "/workflow-definition", { name, active: false })).data!.id;
    try {
      const node = async (statusId: number, type: string) => {
        await apiPost(page, "/workflow-node", { workflowDefinitionId: wfId, statusManagementId: statusId, type });
        const all = await apiGet<Array<{ id: number; statusManagementId: number }>>(page, `/workflow-node/by-workflow/${wfId}`);
        return all.data!.find((n) => n.statusManagementId === statusId)!.id;
      };
      const start = await node(s1.idManagementStatus, "INITIAL");
      const end = await node(s2.idManagementStatus, "FINAL");
      const isolated = await node(s3.idManagementStatus, "INTERMEDIATE");
      await apiPost(page, "/workflow-transition", { workflowDefinitionId: wfId, originNodeId: start, destinationNodeId: end });

      await page.goto(`/dashboard/administration/workflows/${wfId}`);
      await expect(page.getByTestId(`workflow-node-${isolated}`)).toHaveAttribute("data-invalid", "true", { timeout: 15000 });
      await expect(page.getByTestId(`workflow-node-problem-${isolated}`)).toContainText("No alcanzable");
      await expect(page.getByTestId(`workflow-node-${start}`)).toHaveAttribute("data-invalid", "false");
      await expect(page.getByTestId("workflow-problems")).toContainText("no se alcanzan desde el inicio");

      // activation is blocked while invalid
      await page.goto("/dashboard/administration/workflows");
      await page.getByTestId(`btn-edit-wf-${wfId}`).click();
      await page.getByTestId("checkbox-active-workflow").check();
      await page.getByTestId("btn-save-workflow").click();
      await expect(page.getByTestId("workflow-activate-errors")).toBeVisible();
      const still = await apiGet<{ active: boolean }>(page, `/workflow-definition/${wfId}`);
      expect(still.data!.active).toBe(false);

      // fixed: it can be activated
      await apiDelete(page, `/workflow-node/${isolated}`);
      await page.getByTestId("btn-save-workflow").click();
      await expect(page.getByTestId("workflow-activate-errors")).toBeHidden();
      await expect.poll(async () => (await apiGet<{ active: boolean }>(page, `/workflow-definition/${wfId}`)).data!.active).toBe(true);
    } finally {
      await cleanup(page, wfId);
    }
  });
});
