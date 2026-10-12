/**
 * TS-0147 - Workflow editor: no overlapping boxes, auto-layout with saved positions
 *
 * Covers: CU70/CU71 (workflow nodes), PUT /api/v1/workflow-node/{id}
 * Issue: #1574 (epic #1573)
 *
 * Golden path: the seeded workflow opens directly with every status box visible and none
 * overlapping. A workflow whose nodes were all created at (0,0) is laid out too;
 * "Auto-organizar" saves the positions, and they survive a reload.
 */
import { test, expect, type Page } from "@playwright/test";
import { authenticateAsAdmin } from "./setup/auth";
import {
  apiGet,
  createManagementStatus,
  createWorkflowDefinition,
  createWorkflowNode,
  createWorkflowTransition,
} from "./setup/api-helpers";

type Box = { x: number; y: number; width: number; height: number };
const intersects = (a: Box, b: Box) =>
  a.x < b.x + b.width && b.x < a.x + a.width && a.y < b.y + b.height && b.y < a.y + a.height;

async function expectNoOverlap(page: Page, expected: number) {
  const boxes = page.locator(".react-flow__node");
  await expect(boxes).toHaveCount(expected, { timeout: 15000 });
  const rects: Box[] = [];
  for (let i = 0; i < expected; i++) rects.push((await boxes.nth(i).boundingBox())!);
  for (let i = 0; i < rects.length; i++)
    for (let j = i + 1; j < rects.length; j++)
      expect(intersects(rects[i], rects[j]), `boxes ${i} and ${j} overlap`).toBe(false);
}

test.describe("TS-0147 - Workflow editor layout (#1574)", () => {
  test.beforeEach(async ({ page }) => {
    await authenticateAsAdmin(page);
  });

  test("seeded workflow opens directly with all boxes visible and none overlapping", async ({ page }) => {
    const nodes = await apiGet<unknown[]>(page, "/workflow-node/by-workflow/1");
    expect(nodes.ok).toBe(true);
    await page.goto("/dashboard/administration/workflows/1");
    await expectNoOverlap(page, nodes.data!.length);
  });

  test("nodes created at (0,0) are laid out; Auto-organizar saves positions across a reload", async ({ page }) => {
    const wf = await createWorkflowDefinition(page);
    const wfId = wf.data!.id;
    const ids: number[] = [];
    for (const type of ["INITIAL", "INTERMEDIATE", "FINAL"] as const) {
      const status = await createManagementStatus(page);
      const n = await createWorkflowNode(page, wfId, status.data!.idManagementStatus, type);
      ids.push(n.data!.id);
    }
    await createWorkflowTransition(page, wfId, ids[0], ids[1]);
    await createWorkflowTransition(page, wfId, ids[1], ids[2]);

    await page.goto(`/dashboard/administration/workflows/${wfId}`);
    await expectNoOverlap(page, 3);

    await page.getByTestId("btn-toggle-edit").click();
    await page.getByTestId("btn-auto-layout").click();
    await expect
      .poll(async () => {
        const saved = await apiGet<Array<{ positionX: number; positionY: number }>>(page, `/workflow-node/by-workflow/${wfId}`);
        return new Set(saved.data!.map((n) => `${n.positionX},${n.positionY}`)).size;
      }, { timeout: 15000 })
      .toBe(3);

    await page.reload();
    await expectNoOverlap(page, 3);
  });
});
