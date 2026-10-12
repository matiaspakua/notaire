/**
 * TS-0148 - Workflow editor: clear status boxes and directional arrows
 *
 * Covers: CU70/CU71 (workflow graph)
 * Issue: #1575 (epic #1573)
 *
 * Golden path: the seeded workflow shows every status as a fixed-width box with a text type
 * badge (Inicial / Intermedio / Final) and every transition as an arrow with a closed head.
 * Selecting a box in edit mode highlights it.
 */
import { test, expect } from "@playwright/test";
import { authenticateAsAdmin } from "./setup/auth";
import { apiGet } from "./setup/api-helpers";

test.describe("TS-0148 - Workflow boxes and arrows (#1575)", () => {
  test.beforeEach(async ({ page }) => {
    await authenticateAsAdmin(page);
  });

  test("boxes carry a type badge and transitions are arrows", async ({ page }) => {
    const nodes = await apiGet<Array<{ id: number; type: string }>>(page, "/workflow-node/by-workflow/1");
    const transitions = await apiGet<unknown[]>(page, "/workflow-transition/by-workflow/1");
    await page.goto("/dashboard/administration/workflows/1");

    const initial = nodes.data!.find((n) => n.type === "INITIAL")!;
    const final = nodes.data!.find((n) => n.type === "FINAL")!;
    await expect(page.getByTestId(`workflow-node-badge-${initial.id}`)).toHaveText("Inicial", { timeout: 15000 });
    await expect(page.getByTestId(`workflow-node-badge-${final.id}`)).toHaveText("Final");
    await expect(page.getByTestId(`workflow-node-${initial.id}`)).toHaveAttribute("aria-label", /Inicial/);

    const paths = page.locator(".react-flow__edge-path");
    await expect(paths).toHaveCount(transitions.data!.length);
    for (let i = 0; i < transitions.data!.length; i++) {
      await expect(paths.nth(i)).toHaveAttribute("marker-end", /url\(/);
    }
  });

  test("the whole graph is fitted into the canvas on load", async ({ page }) => {
    const nodes = await apiGet<Array<{ id: number }>>(page, "/workflow-node/by-workflow/1");
    await page.goto("/dashboard/administration/workflows/1");
    await expect(page.locator(".react-flow__node")).toHaveCount(nodes.data!.length, { timeout: 15000 });
    await page.waitForTimeout(500);
    const canvas = (await page.getByTestId("workflow-editor").boundingBox())!;
    for (const n of nodes.data!) {
      const b = (await page.getByTestId(`workflow-node-${n.id}`).boundingBox())!;
      expect(b.x, `node ${n.id} left`).toBeGreaterThanOrEqual(canvas.x - 1);
      expect(b.x + b.width, `node ${n.id} right`).toBeLessThanOrEqual(canvas.x + canvas.width + 1);
    }
  });

  test("selecting a box in edit mode highlights it", async ({ page }) => {
    const nodes = await apiGet<Array<{ id: number }>>(page, "/workflow-node/by-workflow/1");
    await page.goto("/dashboard/administration/workflows/1");
    await page.getByTestId("btn-toggle-edit").click();
    const box = page.getByTestId(`workflow-node-${nodes.data![0].id}`);
    await box.click();
    await expect(box).toHaveAttribute("data-selected", "true");
  });
});
