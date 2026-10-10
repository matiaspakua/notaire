/**
 * TS-0122 — Touch targets (#1369, RNF-07 / CU76).
 * On 390px and 820px touch devices every button and select in the list pages
 * (row actions, filters and pagination) is at least 44x44; on desktop the small buttons keep their
 * 32px look.
 */
import { test, expect, type Page } from "@playwright/test";
import { establishAdminBrowserSession } from "./setup/auth";

const PAGES = ["/dashboard/people", "/dashboard/managements", "/dashboard/budgets", "/dashboard/administration/users"];

async function smallButtons(page: Page) {
  return page.locator("main button:visible, main select:visible").evaluateAll((els) =>
    els
      .map((el) => {
        const r = el.getBoundingClientRect();
        return {
          name: el.getAttribute("aria-label") || el.textContent?.trim() || el.outerHTML.slice(0, 60),
          w: Math.round(r.width),
          h: Math.round(r.height),
        };
      })
      .filter((b) => b.w > 0 && b.h > 0),
  );
}

for (const device of [
  { label: "phone", width: 390, height: 844 },
  { label: "tablet", width: 820, height: 1180 },
]) {
  test.describe(`TS-0122 touch targets on a ${device.label} (#1369)`, () => {
    test.use({ viewport: { width: device.width, height: device.height }, hasTouch: true, isMobile: true });

    for (const path of PAGES) {
      test(`every button and select on ${path} is at least 44x44`, async ({ page }) => {
        await establishAdminBrowserSession(page);
        await page.goto(path);
        await expect(page.getByRole("heading", { level: 1 })).toBeVisible({ timeout: 10000 });
        await page.waitForLoadState("networkidle");
        expect(await page.evaluate(() => matchMedia("(pointer: coarse)").matches)).toBe(true);
        const tooSmall = (await smallButtons(page)).filter((b) => b.w < 44 || b.h < 44);
        expect(tooSmall, JSON.stringify(tooSmall)).toEqual([]);
      });
    }
  });
}

test.describe("TS-0122 desktop look is unchanged (#1369)", () => {
  test.use({ viewport: { width: 1440, height: 900 } });

  test("row actions on personas stay 32px tall with a fine pointer", async ({ page }) => {
    await establishAdminBrowserSession(page);
    await page.goto("/dashboard/people");
    const edit = page.locator("main tbody button[aria-label]").first();
    await expect(edit).toBeVisible({ timeout: 10000 });
    const box = await edit.boundingBox();
    expect(Math.round(box!.height)).toBe(32);
    expect(box!.width).toBeGreaterThanOrEqual(32);
  });
});
