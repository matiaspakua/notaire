/**
 * TS-0142 - Required documents per procedure type (admin)
 *
 * Covers: CU55 (procedure template), CU03 (documents required for a procedure), RF-53
 * Issue: #1362
 *
 * Golden path: on Administración → Trámites an ADMIN opens "Documentos requeridos" for a
 * procedure type, adds a document type with notes, edits the notes, and the Documentos
 * necesarios page lists that document by name. Removing it asks for confirmation and the
 * row is really gone (the API used to answer 204 and keep it).
 * Edge: the add select no longer offers a document type that is already on the list.
 */
import { test, expect, type Page } from "@playwright/test";
import { authenticateAsAdmin } from "./setup/auth";
import { apiDelete, apiGet, apiPost, createProcedureType, uniqueId } from "./setup/api-helpers";

const ADMIN_PAGE = "/dashboard/administration/procedures";

async function setup(page: Page) {
  const id = uniqueId();
  const procedureName = `Procedure checklist E2E ${id}`;
  const documentName = `Document checklist E2E ${id}`;
  const pt = await createProcedureType(page, { name: procedureName });
  expect(pt.ok, "procedure type created").toBe(true);
  const dt = await apiPost<{ idDocumentType: number }>(page, "/document-types", {
    name: documentName,
    deliveredBy: "Cliente",
    expires: false,
    returned: false,
  });
  expect(dt.ok, "document type created").toBe(true);
  return { procedureTypeId: pt.data!.idProcedureType, documentTypeId: dt.data!.idDocumentType, procedureName, documentName };
}

async function openChecklist(page: Page, procedureName: string) {
  await page.goto(ADMIN_PAGE);
  await page.getByTestId("input-search-procedure").fill(procedureName);
  const row = page.getByRole("row").filter({ has: page.getByRole("cell", { name: procedureName, exact: true }) });
  await expect(row).toBeVisible({ timeout: 15000 });
  await row.getByTestId("btn-required-docs-procedure").click();
  const dialog = page.getByRole("dialog");
  await expect(dialog.getByRole("heading", { name: new RegExp(procedureName) })).toBeVisible();
  return dialog;
}

test.describe("TS-0142 - Required documents per procedure type (#1362)", () => {
  test.beforeEach(async ({ page }) => {
    await authenticateAsAdmin(page);
  });

  test("admin adds, edits and removes a required document; Documentos necesarios shows it", async ({ page }) => {
    const s = await setup(page);
    try {
      // GIVEN: the checklist dialog of a new procedure type is empty
      let dialog = await openChecklist(page, s.procedureName);
      await expect(dialog.getByTestId("required-docs-empty")).toBeVisible();

      // WHEN: the admin adds the document type with notes
      await dialog.getByTestId("select-required-doc-type").selectOption(String(s.documentTypeId));
      await dialog.getByTestId("input-required-doc-notes").fill("Original");
      await dialog.getByTestId("btn-add-required-doc").click();
      const row = dialog.getByTestId(`required-doc-row-${s.documentTypeId}`);
      await expect(row).toContainText(s.documentName);
      await expect(row).toContainText("Original");

      // AND: it is no longer offered in the add select
      await expect(dialog.getByTestId("select-required-doc-type").locator(`option[value="${s.documentTypeId}"]`)).toHaveCount(0);

      // WHEN: the admin edits the notes
      await row.getByTestId("btn-edit-required-doc").click();
      await row.getByTestId("input-edit-required-doc-notes").fill("Original and copy");
      await row.getByTestId("btn-save-required-doc").click();
      await expect(row).toContainText("Original and copy");
      await page.keyboard.press("Escape");

      // THEN: Documentos necesarios lists it by name for that procedure type
      await page.goto("/dashboard/required-documents");
      await page.getByTestId("select-procedure").click();
      await page.getByRole("option", { name: s.procedureName, exact: true }).click();
      await expect(page.getByRole("cell", { name: s.documentName, exact: true }).first()).toBeVisible({ timeout: 15000 });

      // WHEN: the admin removes it (with confirmation)
      dialog = await openChecklist(page, s.procedureName);
      await dialog.getByTestId(`required-doc-row-${s.documentTypeId}`).getByTestId("btn-delete-required-doc").click();
      await page.getByRole("alertdialog").getByRole("button", { name: /confirmar|confirm|eliminar|delete/i }).click();

      // THEN: the row is gone in the UI and in the API
      await expect(dialog.getByTestId("required-docs-empty")).toBeVisible({ timeout: 15000 });
      const after = await apiGet(page, `/procedure-templates/${s.procedureTypeId}/${s.documentTypeId}`);
      expect(after.status).toBe(404);
    } finally {
      await apiDelete(page, `/procedure-templates/${s.procedureTypeId}/${s.documentTypeId}`);
      await apiDelete(page, `/document-types/${s.documentTypeId}`);
      await apiDelete(page, `/procedure-types/${s.procedureTypeId}`);
    }
  });

  test("the checklist dialog is usable at 390px (mobile)", async ({ page }) => {
    await page.setViewportSize({ width: 390, height: 844 });
    const s = await setup(page);
    try {
      await page.goto(ADMIN_PAGE);
      await page.getByTestId("input-search-procedure").fill(s.procedureName);
      const card = page.getByTestId("data-table-card").filter({ hasText: s.procedureName });
      await expect(card).toBeVisible({ timeout: 15000 });
      await card.getByTestId("btn-required-docs-procedure").click();
      const dialog = page.getByRole("dialog");
      await expect(dialog.getByTestId("btn-add-required-doc")).toBeInViewport();
      const box = await dialog.getByTestId("select-required-doc-type").boundingBox();
      expect(box?.width ?? 0).toBeLessThanOrEqual(390);
    } finally {
      await apiDelete(page, `/document-types/${s.documentTypeId}`);
      await apiDelete(page, `/procedure-types/${s.procedureTypeId}`);
    }
  });
});
