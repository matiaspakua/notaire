/**
 * TS-0150 - Workflow editor: zoom, pan and fit-to-view
 *
 * Covers: CU70/CU71 (workflow graph)
 * Issue: #1579 (epic #1573)
 *
 * Golden path: the canvas controls have Spanish accessible names; after zooming in, "Ajustar a
 * la vista" brings every box back inside the canvas (also on a phone); the MiniMap draws every
 * box and is hidden on a phone.
 */
import { test, expect, type Page } from "@playwright/test";
import { authenticateAsAdmin } from "./setup/auth";
import { apiGet } from "./setup/api-helpers";

async function allInside(page: Page, ids: number[]) {
  const canvas = (await page.getByTestId("workflow-editor").boundingBox())!;
  for (const id of ids) {
    const b = (await page.getByTestId(`workflow-node-${id}`).boundingBox())!;
    if (b.x < canvas.x - 1 || b.x + b.width > canvas.x + canvas.width + 1) return false;
    if (b.y < canvas.y - 1 || b.y + b.height > canvas.y + canvas.height + 1) return false;
  }
  return true;
}

test.describe("TS-0150 - Workflow zoom and fit (#1579)", () => {
  test.beforeEach(async ({ page }) => {
    await authenticateAsAdmin(page);
  });

  test("zoom in, then fit-to-view shows every box", async ({ page }) => {
    const nodes = await apiGet<Array<{ id: number }>>(page, "/workflow-node/by-workflow/1");
    const ids = nodes.data!.map((n) => n.id);
    await page.goto("/dashboard/administration/workflows/1");
    await expect(page.locator(".react-flow__node")).toHaveCount(ids.length, { timeout: 15000 });

    const zoomIn = page.getByRole("button", { name: "Acercar" });
    await expect(zoomIn).toBeVisible();
    await expect(page.getByRole("button", { name: "Alejar" })).toBeVisible();
    for (let i = 0; i < 5; i++) await zoomIn.click();
    await page.waitForTimeout(400);
    expect(await allInside(page, ids)).toBe(false);

    await page.getByRole("button", { name: "Ajustar a la vista" }).click();
    await expect.poll(() => allInside(page, ids), { timeout: 5000 }).toBe(true);
  });

  test("the MiniMap draws every box", async ({ page }) => {
    const nodes = await apiGet<Array<{ id: number }>>(page, "/workflow-node/by-workflow/1");
    await page.goto("/dashboard/administration/workflows/1");
    await expect(page.locator(".react-flow__minimap-node")).toHaveCount(nodes.data!.length, { timeout: 15000 });
  });

  test("fit-to-view shows every box on a phone", async ({ page }) => {
    await page.setViewportSize({ width: 390, height: 844 });
    const nodes = await apiGet<Array<{ id: number }>>(page, "/workflow-node/by-workflow/1");
    const ids = nodes.data!.map((n) => n.id);
    await page.goto("/dashboard/administration/workflows/1");
    // #1580: a phone opens in the list view; the diagram is one tab away.
    await page.getByTestId("wf-tab-graph").click();
    await expect(page.locator(".react-flow__node")).toHaveCount(ids.length, { timeout: 15000 });
    await page.getByRole("button", { name: "Ajustar a la vista" }).click();
    await expect.poll(() => allInside(page, ids), { timeout: 5000 }).toBe(true);
  });

  test("the MiniMap is hidden on a phone", async ({ page }) => {
    await page.setViewportSize({ width: 390, height: 844 });
    await page.goto("/dashboard/administration/workflows/1");
    await page.getByTestId("wf-tab-graph").click();
    await expect(page.locator(".react-flow__node").first()).toBeAttached({ timeout: 15000 });
    await expect(page.locator(".react-flow__minimap")).toBeHidden();
  });
});
