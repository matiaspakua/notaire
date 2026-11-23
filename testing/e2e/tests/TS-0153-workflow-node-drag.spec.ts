/**
 * TS-0153 - Workflow editor: dragging a node keeps its position
 *
 * Covers: CU70 (define a management-status workflow)
 * Issue: #2441 (@xyflow/react 13 changed how node changes are applied)
 *
 * In edit mode an admin drags a node; the canvas shows it at the new place and the editor
 * saves the new position, so it survives a reload.
 */
import { test, expect, type Page } from "@playwright/test";
import { authenticateAsAdmin } from "./setup/auth";
import { apiGet, apiPost, apiDelete } from "./setup/api-helpers";

type Status = { idManagementStatus: number };
type WfNode = { id: number; statusManagementId?: number; positionX?: number; positionY?: number };

async function cleanup(page: Page, wfId: number) {
  const nodes = await apiGet<WfNode[]>(page, `/workflow-node/by-workflow/${wfId}`);
  for (const n of nodes.data ?? []) await apiDelete(page, `/workflow-node/${n.id}`);
  await apiDelete(page, `/workflow-definition/${wfId}`);
}

async function savedNode(page: Page, wfId: number): Promise<WfNode> {
  const nodes = await apiGet<WfNode[]>(page, `/workflow-node/by-workflow/${wfId}`);
  return nodes.data![0]!;
}

test.describe("TS-0153 - Workflow node drag (#2441)", () => {
  test.beforeEach(async ({ page }) => {
    await authenticateAsAdmin(page);
  });

  test("a dragged node stays where it was dropped", async ({ page }) => {
    const status = (await apiGet<Status[]>(page, "/management-statuses")).data![0]!;
    const created = await apiPost<{ id: number }>(page, "/workflow-definition", {
      name: `TS-0153 ${Date.now()}`,
      description: "e2e node drag",
      active: false,
    });
    const wfId = created.data!.id;
    try {
      await apiPost(page, "/workflow-node", {
        workflowDefinitionId: wfId,
        statusManagementId: status.idManagementStatus,
        type: "INITIAL",
        positionX: 100,
        positionY: 100,
      });
      await page.goto(`/dashboard/administration/workflows/${wfId}`);
      await page.getByTestId("btn-toggle-edit").click();
      const node = page.locator(".react-flow__node").first();
      await expect(node).toBeVisible();
      const before = (await savedNode(page, wfId)).positionX ?? 0;
      const box = (await node.boundingBox())!;

      await page.mouse.move(box.x + box.width / 2, box.y + box.height / 2);
      await page.mouse.down();
      await page.mouse.move(box.x + box.width / 2 + 80, box.y + box.height / 2 + 40, { steps: 10 });
      await page.mouse.move(box.x + box.width / 2 + 160, box.y + box.height / 2 + 80, { steps: 10 });
      await page.mouse.up();

      const moved = (await node.boundingBox())!;
      expect(moved.x).toBeGreaterThan(box.x + 40);
      await expect(async () => {
        expect((await savedNode(page, wfId)).positionX ?? 0).toBeGreaterThan(before + 20);
      }).toPass({ timeout: 10000 });

      await page.reload();
      await expect(page.locator(".react-flow__node").first()).toBeVisible();
      expect((await savedNode(page, wfId)).positionX ?? 0).toBeGreaterThan(before + 20);
    } finally {
      await cleanup(page, wfId);
    }
  });
});
