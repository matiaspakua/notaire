/**
 * E2E tests — CU85 Administer procedure folders.
 *
 * A folder is auto-generated (stored status "Activa") whenever a management's procedure is
 * created, and is cascade-archived when the management itself is archived (#839).
 */
import { type Page, test, expect } from "@playwright/test";
import { authenticateAsAdmin } from "./setup/auth";
import {
  apiGet,
  createPerson,
  createBudget,
  createManagementStatus,
  createProcedureType,
  createWorkflowDefinition,
  createWorkflowNode,
  createWorkflowTransition,
  assignWorkflowToProcedureType,
  createCompleteCaseManagement,
} from "./setup/api-helpers";
import { listRow } from "./setup/list-locators";

/**
 * "Archivada" is a global, singleton status (looked up by name in
 * ManagementTransitionService) — reuse the existing row instead of creating a
 * duplicate, which would make that lookup ambiguous.
 */
async function findArchivedStatus(page: Page): Promise<{ idManagementStatus: number; name: string }> {
  const result = await apiGet<{ idManagementStatus: number; name: string }[]>(
    page,
    "/management-statuses/search?name=Archivada",
  );
  const match = result.data!.find((e) => e.name === "Archivada");
  if (!match) {
    throw new Error("Status 'Archivada' not found — expected to be seeded by default data");
  }
  return match;
}

/** Seeds a management whose workflow allows a direct transition to "Archivada". */
async function seedArchivableManagement(page: Page) {
  const person = await createPerson(page);
  const budget = await createBudget(page, person.data!.personId);

  const initialStatus = await createManagementStatus(page);
  const archivedStatus = await findArchivedStatus(page);
  const workflow = await createWorkflowDefinition(page);
  const workflowId = workflow.data!.id;

  const initialNode = await createWorkflowNode(
    page,
    workflowId,
    initialStatus.data!.idManagementStatus,
    "INITIAL",
  );
  const archivedNode = await createWorkflowNode(
    page,
    workflowId,
    archivedStatus.idManagementStatus,
    "FINAL",
  );
  await createWorkflowTransition(page, workflowId, initialNode.data!.id, archivedNode.data!.id);

  const typeProcedure = await createProcedureType(page);
  await assignWorkflowToProcedureType(page, typeProcedure.data!.idProcedureType, workflowId);

  const management = await createCompleteCaseManagement(page, {
    budgetId: budget.data!.idBudget,
    procedureTypeId: typeProcedure.data!.idProcedureType,
    statusManagementId: initialStatus.data!.idManagementStatus,
  });

  return { idManagement: management.data!.idManagement, number: management.data!.number };
}

test.describe("CU85 - Manage procedure folders", () => {
  test.beforeEach(async ({ page }) => {
    await authenticateAsAdmin(page);
  });

  test("golden path: starting a procedure creates an active folder, it is put on hold, and it is archived with the management", async ({
    page,
  }) => {
    const { idManagement, number } = await seedArchivableManagement(page);

    await page.goto("/dashboard/managements");
    await page.waitForLoadState("domcontentloaded");

    const row = page.getByRole("row", { name: new RegExp(String(number)) });
    await expect(row).toBeVisible({ timeout: 10000 });

    await page.getByTestId(`btn-view-folders-${idManagement}`).click();
    let dialog = page.getByRole("dialog");
    await expect(dialog).toBeVisible();

    const item = dialog.getByTestId("folder-item").first();
    await expect(item).toContainText("Activa");

    await item.getByTestId(/^btn-place-on-hold-/).click();
    dialog = page.getByRole("dialog");
    await dialog.getByTestId("input-wait-reason").fill("Falta documentación del titular");
    await dialog.getByTestId("btn-confirm-wait").click();
    await expect(dialog).not.toBeVisible();

    await page.getByTestId(`btn-view-folders-${idManagement}`).click();
    dialog = page.getByRole("dialog");
    await expect(dialog.getByTestId("folder-item").first()).toContainText("Espera");
    await page.keyboard.press("Escape");

    await page.getByTestId(`btn-archive-management-${idManagement}`).click();
    const confirmDialog = page.getByRole("alertdialog");
    await expect(confirmDialog).toBeVisible();
    await confirmDialog.getByRole("button", { name: /archive gestión/i }).click();
    await expect(confirmDialog).toContainText(/wait/i);
    await confirmDialog.getByRole("button", { name: /archive de todos modos/i }).click();
    await expect(confirmDialog).not.toBeVisible();

    await page.getByTestId(`btn-view-folders-${idManagement}`).click();
    dialog = page.getByRole("dialog");
    await expect(dialog.getByTestId("folder-item").first()).toContainText("Archivada");
  });

  test("edge path: placing on hold without a reason shows a visible error", async ({ page }) => {
    const { idManagement, number } = await seedArchivableManagement(page);

    await page.goto("/dashboard/managements");
    await page.waitForLoadState("domcontentloaded");

    const row = page.getByRole("row", { name: new RegExp(String(number)) });
    await expect(row).toBeVisible({ timeout: 10000 });

    await page.getByTestId(`btn-view-folders-${idManagement}`).click();
    const dialog = page.getByRole("dialog");
    await expect(dialog).toBeVisible();

    await dialog.getByTestId(/^btn-place-on-hold-/).click();
    const waitDialog = page.getByRole("dialog");
    await waitDialog.getByTestId("btn-confirm-wait").click();

    await expect(page.getByText(/motivo es obligatorio/i)).toBeVisible({ timeout: 10000 });
    await expect(waitDialog).toBeVisible();
  });

  test("edge path: archiving a management with a folder on hold requires explicit confirmation", async ({
    page,
  }) => {
    const { idManagement, number } = await seedArchivableManagement(page);

    await page.goto("/dashboard/managements");
    await page.waitForLoadState("domcontentloaded");

    const row = page.getByRole("row", { name: new RegExp(String(number)) });
    await expect(row).toBeVisible({ timeout: 10000 });

    await page.getByTestId(`btn-view-folders-${idManagement}`).click();
    let dialog = page.getByRole("dialog");
    await dialog.getByTestId(/^btn-place-on-hold-/).click();
    dialog = page.getByRole("dialog");
    await dialog.getByTestId("input-wait-reason").fill("Falta documentación");
    await dialog.getByTestId("btn-confirm-wait").click();
    await expect(dialog).not.toBeVisible();
    await page.keyboard.press("Escape");

    await page.getByTestId(`btn-archive-management-${idManagement}`).click();
    const confirmDialog = page.getByRole("alertdialog");
    await expect(confirmDialog).toBeVisible();
    await confirmDialog.getByRole("button", { name: /archive gestión/i }).click();
    await expect(confirmDialog).toContainText(/wait/i);
    await expect(confirmDialog.getByRole("button", { name: /archive de todos modos/i })).toBeVisible();
  });

  for (const viewport of [
    { width: 320, height: 568, label: "320px (mobile)" },
    { width: 768, height: 1024, label: "768px (tablet)" },
    { width: 1024, height: 768, label: "1024px (desktop)" },
  ]) {
    test(`carpetas dialog is usable at ${viewport.label}`, async ({ page }) => {
      const { idManagement, number } = await seedArchivableManagement(page);

      await page.setViewportSize({ width: viewport.width, height: viewport.height });
      await page.goto("/dashboard/managements");
      await page.waitForLoadState("domcontentloaded");

      const row = listRow(page, new RegExp(String(number)));
      await expect(row).toBeVisible({ timeout: 10000 });

      await page.getByTestId(`btn-view-folders-${idManagement}`).click();
      const dialog = page.getByRole("dialog");
      await expect(dialog).toBeVisible();
      await expect(dialog.getByTestId("folder-item").first()).toContainText("Activa");
    });
  }
});
