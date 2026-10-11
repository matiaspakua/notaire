/**
 * TS-0140 - Row action icons are visible
 *
 * Covers: RNF-07 (usability), CU76
 * Issue: #1525
 *
 * The edit and delete buttons in admin table rows hold a NotaireIcon image.
 * In the narrow desktop action column the image was squeezed to 0px, so the
 * buttons looked empty. Every icon inside a row action must keep a real size.
 * Rows come from the global setup fixtures (management status, concept,
 * procedure type).
 */
import { test, expect } from "@playwright/test";
import { authenticateAsAdmin } from "./setup/auth";

const PAGES = [
  { path: "/dashboard/administration/management-statuses", action: "btn-delete-status" },
  { path: "/dashboard/administration/concepts", action: "btn-delete-concept" },
  { path: "/dashboard/administration/procedures", action: "btn-delete-procedure" },
];

for (const viewport of [
  { name: "desktop", width: 1440, height: 900 },
  { name: "mobile", width: 390, height: 844 },
]) {
  test.describe(`TS-0140 - Row action icons are visible at ${viewport.name} (#1525)`, () => {
    test.use({ viewport: { width: viewport.width, height: viewport.height } });

    test.beforeEach(async ({ page }) => {
      await authenticateAsAdmin(page);
    });

    for (const { path, action } of PAGES) {
      test(`row action icons on ${path} have a real size`, async ({ page }) => {
        // GIVEN: an admin list with at least one row
        await page.goto(path);
        const first = page.getByTestId(action).first();
        await expect(first).toBeVisible({ timeout: 20000 });

        // THEN: the icons in the first rows' action buttons are about 16x16
        const sizes = await page.evaluate(() =>
          Array.from(document.querySelectorAll("table tbody tr, [data-testid='data-table-card']"))
            .slice(0, 10)
            .flatMap((row) => Array.from(row.querySelectorAll("button img")))
            .filter((img) => img.getClientRects().length > 0) // skip the layout hidden at this width
            .map((img) => {
              const b = img.getBoundingClientRect();
              return { w: Math.round(b.width), h: Math.round(b.height) };
            }),
        );
        expect(sizes.length, "row action icons found").toBeGreaterThan(0);
        for (const s of sizes) {
          expect(s.w, "icon width").toBeGreaterThanOrEqual(12);
          expect(s.h, "icon height").toBeGreaterThanOrEqual(12);
        }
      });
    }
  });
}
