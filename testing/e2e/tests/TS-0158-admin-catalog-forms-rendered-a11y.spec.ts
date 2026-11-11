/**
 * TS-0158 — Administration catalog forms rendered a11y (#2088)
 *
 * One rendered pass per catalog page at desktop (1280) and 320px:
 * primary Save meets WCAG 2.2 AA 24px (design system default h-11 ≈ 44px),
 * empty required submit shows alerts when the dialog has required fields,
 * and 320px does not force horizontal page overflow.
 */
import { test, expect, type Page } from "@playwright/test";
import { establishAdminBrowserSession } from "./setup/auth";

type CatalogSurface = {
  name: string;
  path: string;
  openTestId: string;
  saveTestId: string;
  /** When true, empty Save should surface role=alert inside the dialog. */
  expectRequiredAlerts: boolean;
};

const SURFACES: CatalogSurface[] = [
  {
    name: "concepts",
    path: "/dashboard/administration/concepts",
    openTestId: "btn-new-concept",
    saveTestId: "btn-save-concept",
    expectRequiredAlerts: true,
  },
  {
    name: "documents",
    path: "/dashboard/administration/documents",
    openTestId: "btn-new-document-type",
    saveTestId: "btn-save-document",
    expectRequiredAlerts: true,
  },
  {
    name: "folios",
    path: "/dashboard/administration/folios",
    openTestId: "btn-new-folio",
    saveTestId: "btn-save-folio",
    expectRequiredAlerts: true,
  },
  {
    name: "identification-types",
    path: "/dashboard/administration/identification-types",
    openTestId: "btn-new-identification-type",
    saveTestId: "btn-save-identification-type",
    expectRequiredAlerts: true,
  },
  {
    name: "management-statuses",
    path: "/dashboard/administration/management-statuses",
    openTestId: "btn-new-status",
    saveTestId: "btn-save-status",
    expectRequiredAlerts: true,
  },
  {
    name: "procedures",
    path: "/dashboard/administration/procedures",
    openTestId: "btn-new-procedure-type",
    saveTestId: "btn-save-procedure",
    expectRequiredAlerts: true,
  },
  {
    name: "roles",
    path: "/dashboard/administration/roles",
    openTestId: "btn-new-role",
    saveTestId: "btn-save-role",
    expectRequiredAlerts: true,
  },
  {
    name: "templates",
    path: "/dashboard/administration/templates",
    openTestId: "btn-new-template",
    saveTestId: "btn-save-template",
    expectRequiredAlerts: true,
  },
  {
    name: "users",
    path: "/dashboard/administration/users",
    openTestId: "btn-new-user",
    saveTestId: "btn-save-user",
    expectRequiredAlerts: true,
  },
  {
    name: "workflows",
    path: "/dashboard/administration/workflows",
    openTestId: "btn-new-workflow",
    saveTestId: "btn-save-workflow",
    expectRequiredAlerts: true,
  },
];

async function openCreateDialog(page: Page, surface: CatalogSurface) {
  await page.goto(surface.path);
  await page.getByTestId(surface.openTestId).click();
  const dialog = page.getByRole("dialog");
  await expect(dialog).toBeVisible();
  return dialog;
}

test.describe("TS-0158 - admin catalog forms rendered a11y (#2088)", () => {
  test.beforeEach(async ({ page }) => {
    await establishAdminBrowserSession(page);
  });

  for (const surface of SURFACES) {
    test(`desktop ${surface.name}: primary height and required errors`, async ({ page }) => {
      await page.setViewportSize({ width: 1280, height: 800 });
      const dialog = await openCreateDialog(page, surface);

      const primary = dialog.getByTestId(surface.saveTestId);
      const pbox = await primary.boundingBox();
      expect(pbox?.height ?? 0).toBeGreaterThanOrEqual(24);
      expect(pbox?.height ?? 0).toBeGreaterThanOrEqual(40); // h-11 token ~44px

      if (surface.expectRequiredAlerts) {
        await primary.click();
        await expect(dialog.getByRole("alert").first()).toBeVisible({ timeout: 5000 });
      }
    });

    test(`320px ${surface.name}: primary still ≥24px and no horizontal overflow`, async ({ page }) => {
      await page.setViewportSize({ width: 320, height: 720 });
      const dialog = await openCreateDialog(page, surface);

      const primary = dialog.getByTestId(surface.saveTestId);
      const pbox = await primary.boundingBox();
      expect(pbox?.height ?? 0).toBeGreaterThanOrEqual(24);
      expect(await primary.isVisible()).toBe(true);

      const overflow = await page.evaluate(
        () => document.documentElement.scrollWidth > window.innerWidth + 1,
      );
      expect(overflow).toBe(false);
    });
  }
});
