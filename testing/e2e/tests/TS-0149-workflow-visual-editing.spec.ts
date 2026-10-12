/**
 * TS-0149 - Workflow editor: visual editing with explicit controls
 *
 * Covers: CU70 (define a management-status workflow)
 * Issue: #1576 (epic #1573)
 *
 * Golden path: on an empty workflow an admin builds start → mid → final using only visible
 * controls (add node dialog, side panel "Add transition"), edits a node's type, and deletes a
 * transition and a node, each after a confirmation. Double-click no longer deletes anything,
 * and duplicate transitions are rejected with a message.
 */
import { test, expect, type Page } from "@playwright/test";
import { authenticateAsAdmin } from "./setup/auth";
import { apiGet, apiPost, apiDelete } from "./setup/api-helpers";

type Status = { idManagementStatus: number; name: string };
type WfNode = { id: number; statusManagementId?: number; type: string };
type WfTransition = { id: number };

async function cleanup(page: Page, wfId: number) {
  const trs = await apiGet<WfTransition[]>(page, `/workflow-transition/by-workflow/${wfId}`);
  for (const t of trs.data ?? []) await apiDelete(page, `/workflow-transition/${t.id}`);
  const nodes = await apiGet<WfNode[]>(page, `/workflow-node/by-workflow/${wfId}`);
  for (const n of nodes.data ?? []) await apiDelete(page, `/workflow-node/${n.id}`);
  await apiDelete(page, `/workflow-definition/${wfId}`);
}

async function addNode(page: Page, statusId: number, type: string) {
  await page.getByTestId("btn-add-node").click();
  await page.getByTestId("select-status-node").selectOption(String(statusId));
  await page.getByTestId("select-node-type").selectOption(type);
  await page.getByTestId("btn-confirm-node").click();
}

async function nodeIdFor(page: Page, wfId: number, statusId: number) {
  let id = 0;
  await expect(async () => {
    const nodes = await apiGet<WfNode[]>(page, `/workflow-node/by-workflow/${wfId}`);
    id = nodes.data!.find((n) => n.statusManagementId === statusId)?.id ?? 0;
    expect(id).toBeGreaterThan(0);
  }).toPass({ timeout: 10000 });
  return id;
}

test.describe("TS-0149 - Workflow visual editing (#1576)", () => {
  test.beforeEach(async ({ page }) => {
    await authenticateAsAdmin(page);
  });

  test("build, edit and delete through visible controls", async ({ page }) => {
    const statuses = (await apiGet<Status[]>(page, "/management-statuses")).data!.slice(0, 3);
    expect(statuses).toHaveLength(3);
    const created = await apiPost<{ id: number }>(page, "/workflow-definition", {
      name: `TS-0149 ${Date.now()}`,
      description: "e2e visual editing",
      active: false,
    });
    const wfId = created.data!.id;
    try {
      await page.goto(`/dashboard/administration/workflows/${wfId}`);
      await page.getByTestId("btn-toggle-edit").click();
      await expect(page.getByTestId("wf-panel-empty")).toBeVisible();

      const [s1, s2, s3] = statuses;
      await addNode(page, s1.idManagementStatus, "INITIAL");
      const n1 = await nodeIdFor(page, wfId, s1.idManagementStatus);
      await addNode(page, s2.idManagementStatus, "INTERMEDIATE");
      const n2 = await nodeIdFor(page, wfId, s2.idManagementStatus);
      await addNode(page, s3.idManagementStatus, "INTERMEDIATE");
      const n3 = await nodeIdFor(page, wfId, s3.idManagementStatus);
      await expect(page.locator(".react-flow__node")).toHaveCount(3);

      // start → mid, from the panel
      await page.getByTestId(`workflow-node-${n1}`).click();
      await page.getByTestId("wf-panel-add-to").selectOption(String(n2));
      await page.getByTestId("wf-panel-add-desc").fill("iniciar");
      await page.getByTestId("wf-panel-add-transition").click();
      await expect(page.locator(".react-flow__edge")).toHaveCount(1);

      // a duplicate is rejected with a message
      await page.getByTestId("wf-panel-add-to").selectOption(String(n2));
      await page.getByTestId("wf-panel-add-transition").click();
      await expect(page.getByTestId("wf-panel-add-error")).toContainText("ya existe");

      // mid → final
      await page.getByTestId(`workflow-node-${n2}`).click();
      await page.getByTestId("wf-panel-add-to").selectOption(String(n3));
      await page.getByTestId("wf-panel-add-transition").click();
      await expect(page.locator(".react-flow__edge")).toHaveCount(2);

      // edit a node type
      await page.getByTestId(`workflow-node-${n3}`).click();
      await page.getByTestId("wf-panel-type").selectOption("FINAL");
      await expect(page.getByTestId(`workflow-node-badge-${n3}`)).toHaveText("Final");

      // double-click no longer deletes
      await page.getByTestId(`workflow-node-${n3}`).dblclick();
      await expect(page.locator(".react-flow__node")).toHaveCount(3);

      // delete a transition, with confirmation
      await page.locator(".react-flow__edge").last().click({ force: true });
      await expect(page.getByTestId("wf-panel-edge")).toBeVisible();
      await page.getByTestId("wf-panel-delete-edge").click();
      await page.getByTestId("confirm-dialog-confirm").click();
      await expect(page.locator(".react-flow__edge")).toHaveCount(1);

      // delete the now unlinked final node, with confirmation
      await page.getByTestId(`workflow-node-${n3}`).click();
      await page.getByTestId("wf-panel-delete-node").click();
      await page.getByTestId("confirm-dialog-confirm").click();
      await expect(page.locator(".react-flow__node")).toHaveCount(2);
    } finally {
      await cleanup(page, wfId);
    }
  });
});
