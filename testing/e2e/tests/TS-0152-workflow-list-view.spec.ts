/**
 * TS-0152 - Workflow editor: list view, keyboard only
 *
 * Covers: CU70 (define a management-status workflow)
 * Issue: #1578 (epic #1573)
 *
 * Golden path: with the keyboard only (focus + keys, no pointer), an admin switches to "Vista de
 * lista", adds start / mid / final statuses, connects them, and the workflow becomes valid.
 * Every control in the list view has an accessible name, and canvas boxes are focusable with a
 * name that includes their outgoing transitions.
 */
import { test, expect, type Page, type Locator } from "@playwright/test";
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

/**
 * Pick an option of a native select while keeping the flow keyboard-shaped.
 *
 * The button/tab interactions in this spec use real key presses everywhere they
 * can. For the native `<select>`s the value has to be changed through
 * `selectOption`: Playwright's bundled Chromium (153) no longer performs the
 * select's default action for synthesized arrow keys (the option popup never
 * opens, no `change` fires — verified on a blank page), so `ArrowDown`/`Enter`
 * cannot drive it from automation. The controls are native, so a real keyboard
 * user gets the browser's own popup behavior; what this spec still pins with a
 * real keyboard is the surrounding flow: tab reachability, focus, and the
 * button/tab interactions that actually build the workflow.
 */
async function keySelect(select: Locator, label: string) {
  await expect(select.locator("option", { hasText: label })).toHaveCount(1);
  await select.focus();
  await select.selectOption({ label });
  await expect(select).toHaveValue((await select.locator("option", { hasText: label }).getAttribute("value")) as string);
}

async function press(button: Locator) {
  await button.focus();
  await button.press("Enter");
}

test.describe("TS-0152 - Workflow list view (#1578)", () => {
  test.beforeEach(async ({ page }) => {
    await authenticateAsAdmin(page);
  });

  test("build a workflow with the keyboard in the list view", async ({ page }) => {
    const [s1, s2, s3] = (await apiGet<Status[]>(page, "/management-statuses")).data!;
    const wfId = (await apiPost<{ id: number }>(page, "/workflow-definition", { name: `TS-0152 ${Date.now()}`, active: false })).data!.id;
    try {
      await page.goto(`/dashboard/administration/workflows/${wfId}`);
      await press(page.getByTestId("btn-toggle-edit"));
      await press(page.getByTestId("wf-tab-graph"));
      await page.getByTestId("wf-tab-graph").press("ArrowRight");
      await expect(page.getByTestId("wf-tab-list")).toBeFocused();
      await expect(page.getByTestId("wf-list-view")).toBeVisible();

      for (const [s, type] of [[s1, "Inicial"], [s2, "Intermedio"], [s3, "Final"]] as const) {
        await keySelect(page.getByTestId("wf-list-add-status"), s.name);
        await keySelect(page.getByTestId("wf-list-add-type"), type);
        await press(page.getByTestId("wf-list-add-node"));
        await expect(page.getByRole("rowheader", { name: new RegExp(s.name) })).toBeVisible();
      }

      for (const [a, b] of [[s1, s2], [s2, s3]]) {
        await keySelect(page.getByTestId("wf-list-add-from"), a.name);
        await keySelect(page.getByTestId("wf-list-add-to"), b.name);
        await press(page.getByTestId("wf-list-add-transition"));
        await expect(page.getByRole("rowheader", { name: `${a.name} → ${b.name}` })).toBeVisible();
      }
      await expect(page.getByTestId("workflow-validity")).toBeVisible();

      // every control in the list view has an accessible name
      const view = page.getByTestId("wf-list-view");
      for (const role of ["button", "combobox", "textbox"] as const) {
        for (const el of await view.getByRole(role).all()) await expect(el).toHaveAccessibleName(/\S/);
      }

      // canvas boxes are focusable and name their outgoing transitions
      await press(page.getByTestId("wf-tab-graph"));
      const start = page.locator(".react-flow__node").filter({ hasText: s1.name });
      await start.focus();
      await expect(start).toBeFocused();
      await expect(start.getByRole("group")).toHaveAccessibleName(/1 transición saliente/);
    } finally {
      await cleanup(page, wfId);
    }
  });
});
