/**
 * E2E — Motion tokens (#1368, RNF-03 / RNF-05).
 *
 * Route changes no longer wait for an exit animation (was 200ms exit + 400ms
 * enter), dialogs use the shared timing tokens, and reduced motion removes
 * transforms.
 */
import { test, expect, type Page } from "@playwright/test";
import { establishAdminBrowserSession } from "./setup/auth";

/** ms from the nav click until the new page's content wrapper is fully opaque. */
async function navigateAndTime(page: Page, testId: string, path: string): Promise<number> {
  const link = page.getByTestId(testId);
  await expect(link).toBeVisible();
  await page.evaluate(() => {
    (window as unknown as { __clickAt: number }).__clickAt = 0;
    document.addEventListener(
      "click",
      () => ((window as unknown as { __clickAt: number }).__clickAt = performance.now()),
      { capture: true, once: true },
    );
  });
  await link.click();
  await page.waitForURL(`**${path}`);
  const handle = await page.waitForFunction(
    (p) => {
      if (location.pathname !== p) return false;
      const wrapper = document.querySelector("main > div");
      if (!wrapper || !wrapper.querySelector("h1")) return false;
      if (getComputedStyle(wrapper).opacity !== "1") return false;
      return performance.now() - (window as unknown as { __clickAt: number }).__clickAt;
    },
    path,
    { polling: "raf", timeout: 10_000 },
  );
  return (await handle.jsonValue()) as number;
}

test.describe("Motion tokens (#1368)", () => {
  test("route content is fully visible soon after a nav click (no exit wait)", async ({ page }) => {
    await establishAdminBrowserSession(page);
    await page.goto("/dashboard/copias");
    await page.waitForLoadState("networkidle");
    const samples: number[] = [];
    for (let i = 0; i < 5; i++) {
      samples.push(await navigateAndTime(page, "nav-items", "/dashboard/items"));
      samples.push(await navigateAndTime(page, "nav-copias", "/dashboard/copias"));
    }
    samples.sort((a, b) => a - b);
    const median = samples[Math.floor(samples.length / 2)];
    test.info().annotations.push({ type: "median-ms", description: String(Math.round(median)) });
    // Old timing: 200ms exit + 400ms enter (median ~450ms). New: 160ms fade, no exit wait.
    expect(median).toBeLessThan(300);
  });

  test("dialogs use the motion tokens", async ({ page }) => {
    await establishAdminBrowserSession(page);
    await page.goto("/dashboard/personas");
    await page.getByTestId("btn-nueva-persona").click();
    const dialog = page.getByRole("dialog");
    await expect(dialog).toBeVisible();
    const timing = await dialog.evaluate((el) => {
      const s = getComputedStyle(el);
      return { duration: s.animationDuration, ease: s.animationTimingFunction };
    });
    expect(timing.duration).toBe("0.24s");
    expect(timing.ease).toBe("cubic-bezier(0.3, 0, 0, 1)");
  });

  test.describe("with reduced motion", () => {
    test.use({ reducedMotion: "reduce" });

    test("page content and dialogs end without a transform", async ({ page }) => {
      await establishAdminBrowserSession(page);
      await page.goto("/dashboard/personas");
      const wrapper = page.locator("main > div").first();
      await expect(wrapper).toBeVisible();
      await expect(wrapper).toHaveCSS("transform", "none");
      await page.getByTestId("btn-nueva-persona").click();
      const dialog = page.getByRole("dialog");
      await expect(dialog).toBeVisible();
      const seconds = await dialog.evaluate((el) => parseFloat(getComputedStyle(el).animationDuration));
      expect(seconds).toBeLessThan(0.05);
    });
  });
});
