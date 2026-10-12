/**
 * TS-0145 - "Todas las plantillas" overview of required documents (admin)
 *
 * Covers: CU79 (administrar plantillas de trámite), GET /api/v1/procedure-templates
 * Issue: #1554
 *
 * Golden path: a template created through the API shows up in the overview on
 * Administración → Plantillas, the filters narrow it down, and its row opens that
 * procedure type's "Documentos requeridos" editor.
 * Edge: at 390px the overview stays usable (filter plus row link visible).
 */
import { test, expect, type Page } from "@playwright/test";
import { authenticateAsAdmin } from "./setup/auth";
import { apiDelete, apiPost, createProcedureType, uniqueId } from "./setup/api-helpers";

const PAGE = "/dashboard/administration/templates";

async function setup(page: Page) {
  const id = uniqueId();
  const procedureName = `Overview procedure E2E ${id}`;
  const documentName = `Overview document E2E ${id}`;
  const pt = await createProcedureType(page, { name: procedureName });
  expect(pt.ok, "procedure type created").toBe(true);
  const dt = await apiPost<{ idDocumentType: number }>(page, "/document-types", {
    name: documentName,
    deliveredBy: "Cliente",
    expires: false,
    returned: false,
  });
  expect(dt.ok, "document type created").toBe(true);
  const procedureTypeId = pt.data!.idProcedureType;
  const documentTypeId = dt.data!.idDocumentType;
  const tpl = await apiPost(page, "/procedure-templates", {
    fkIdProcedureType: procedureTypeId,
    fkIdDocumentType: documentTypeId,
    notes: "Overview E2E",
  });
  expect(tpl.ok, "template created").toBe(true);
  return { procedureTypeId, documentTypeId, procedureName, documentName };
}

async function cleanup(page: Page, s: { procedureTypeId: number; documentTypeId: number }) {
  await apiDelete(page, `/procedure-templates/${s.procedureTypeId}/${s.documentTypeId}`);
  await apiDelete(page, `/document-types/${s.documentTypeId}`);
  await apiDelete(page, `/procedure-types/${s.procedureTypeId}`);
}

test.describe("TS-0145 - All procedure templates overview (#1554)", () => {
  test.beforeEach(async ({ page }) => {
    await authenticateAsAdmin(page);
  });

  test("a template created via the API is listed, filterable and opens its editor", async ({ page }) => {
    const s = await setup(page);
    try {
      // GIVEN: the admin opens the templates page
      await page.goto(PAGE);
      const table = page.getByTestId("all-templates-table");

      // WHEN: they filter the overview by the new procedure type
      await page.getByTestId("all-templates-filter-procedure").fill(s.procedureName);

      // THEN: the template row shows procedure and document names
      const link = page.getByTestId(`all-templates-open-${s.procedureTypeId}-${s.documentTypeId}`);
      await expect(link).toBeVisible({ timeout: 15000 });
      await expect(table).toContainText(s.documentName);

      // AND: a non-matching document filter empties the list
      await page.getByTestId("all-templates-filter-document").fill(`nomatch-${s.documentName}`);
      await expect(page.getByTestId("all-templates-empty")).toBeVisible();
      await page.getByTestId("all-templates-filter-document").fill(s.documentName);

      // WHEN: the row link is followed
      await link.click();

      // THEN: the procedure's required-documents editor opens with the document listed
      await expect(page).toHaveURL(/\/dashboard\/administration\/procedures/);
      const dialog = page.getByRole("dialog");
      await expect(dialog.getByRole("heading", { name: new RegExp(s.procedureName) })).toBeVisible({ timeout: 15000 });
      await expect(dialog).toContainText(s.documentName);
    } finally {
      await cleanup(page, s);
    }
  });

  test("overview works at 390px", async ({ page }) => {
    await page.setViewportSize({ width: 390, height: 844 });
    const s = await setup(page);
    try {
      await page.goto(PAGE);
      await page.getByTestId("all-templates-filter-procedure").fill(s.procedureName);
      const link = page.getByTestId(`all-templates-open-${s.procedureTypeId}-${s.documentTypeId}`);
      await expect(link).toBeVisible({ timeout: 15000 });
      await link.scrollIntoViewIfNeeded();
      await expect(link).toBeInViewport();
    } finally {
      await cleanup(page, s);
    }
  });
});
