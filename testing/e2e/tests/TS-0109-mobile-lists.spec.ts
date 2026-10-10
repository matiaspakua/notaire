/**
 * TS-0109 - List pages are usable on phones (issue #1356)
 *
 * Covers: RNF-05 (Aspecto visual), RNF-06, CU76
 * Issue: #1356 — at 390px the DataTable showed 3 columns and the row actions
 * were off-screen with no scroll affordance.
 *
 * Approach: stub each list endpoint with one row so the check does not depend
 * on seed data, then assert at 390x844 that the row is a card, its edit button
 * is inside the viewport, and the page does not scroll horizontally.
 */
import { test, expect, type Page } from "@playwright/test";

const ROW_ID = 990_109;

const PAGES: { route: string; api: string; paged: boolean; row: Record<string, unknown> }[] = [
  {
    route: "/dashboard/personas",
    api: "**/api/v1/people?**",
    paged: true,
    row: { personId: ROW_ID, firstName: "Movil", lastName: "TS0109", identificationNumber: "99010901", email: "movil.ts0109@notaire.test", isClient: true },
  },
  {
    route: "/dashboard/gestiones",
    api: "**/api/v1/gestiones?**",
    paged: true,
    row: { idManagement: ROW_ID, number: "TS0109", procedureCount: 1, statusActual: "INICIADA" },
  },
  {
    route: "/dashboard/presupuestos",
    api: "**/api/v1/presupuestos?**",
    paged: true,
    row: { idBudget: ROW_ID, date: "2026-10-01", propertyAmount: 1000, status: "PENDIENTE" },
  },
  {
    route: "/dashboard/escrituras",
    api: "**/api/v1/escrituras?**",
    paged: true,
    row: { idDeed: ROW_ID, number: 109, dateDeedrecording: "2026-10-01", status: "BORRADOR" },
  },
  {
    route: "/dashboard/pagos",
    api: "**/api/v1/pagos",
    paged: false,
    row: { idPayment: ROW_ID, idBudget: 1, date: "2026-10-01", amount: 109, paymentMethod: "EFECTIVO" },
  },
];

async function loginAsAdmin(page: Page) {
  await page.goto("/login");
  await page.getByTestId("input-usuario").fill("admin");
  await page.getByTestId("input-contrasenia").fill("admin");
  await page.getByTestId("btn-ingresar").click();
  await expect(page).toHaveURL(/\/dashboard/, { timeout: 15000 });
}

test.describe("TS-0109 - list pages at 390px (#1356)", () => {
  test.use({ viewport: { width: 390, height: 844 } });

  for (const p of PAGES) {
    test(`${p.route}: rows are cards with reachable actions and no horizontal overflow`, async ({ page }) => {
      await loginAsAdmin(page);
      await page.route(p.api, async (route) => {
        if (route.request().method() !== "GET") return route.continue();
        await route.fulfill({
          status: 200,
          contentType: "application/json",
          body: JSON.stringify(
            p.paged ? { content: [p.row], totalElements: 1, totalPages: 1, number: 0, size: 1000 } : [p.row],
          ),
        });
      });
      await page.goto(p.route);

      const cards = page.getByTestId("data-table-cards");
      await expect(cards).toBeVisible({ timeout: 15000 });
      await expect(page.getByRole("table")).toHaveCount(0);

      const card = cards.getByRole("listitem").first();
      const edit = card.getByRole("button", { name: /editar|edit/i }).first();
      await expect(edit).toBeInViewport();
      const box = await edit.boundingBox();
      expect(box!.x + box!.width).toBeLessThanOrEqual(390);
      expect(box!.height).toBeGreaterThanOrEqual(44);

      const noOverflow = await page.evaluate(
        () => document.documentElement.scrollWidth <= window.innerWidth,
      );
      expect(noOverflow).toBe(true);
    });
  }

  test("desktop keeps the table", async ({ page }) => {
    await page.setViewportSize({ width: 1440, height: 900 });
    await loginAsAdmin(page);
    await page.goto("/dashboard/personas");
    await expect(page.getByRole("table")).toBeVisible({ timeout: 15000 });
    await expect(page.getByTestId("data-table-cards")).toHaveCount(0);
  });
});
