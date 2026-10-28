import { expect, test } from "@playwright/test";
import AxeBuilder from "@axe-core/playwright";

const PAGES = ["", "docs/", "docs/architecture/", "docs/releases/", "ai-sdlc/", "ai-sdlc/lifecycle/", "ai-sdlc/guardrails/", "ai-sdlc/agents/", "ai-sdlc/results/", "ai-sdlc/case-study/"];

for (const p of PAGES) {
  test(`/${p} has one header, no horizontal scroll and no axe violations`, async ({ page }) => {
    await page.goto(p);
    await expect(page.locator("header[data-site-header]")).toHaveCount(1);
    await page.waitForLoadState("networkidle");
    const overflow = await page.evaluate(() => document.documentElement.scrollWidth - document.documentElement.clientWidth);
    expect(overflow, "page scrolls sideways").toBeLessThanOrEqual(1);
    const axe = await new AxeBuilder({ page }).withTags(["wcag2a", "wcag2aa"]).analyze();
    expect(axe.violations.map((v) => `${v.id}: ${v.nodes.map((n) => n.target.join(" ")).slice(0, 3).join(", ")}`)).toEqual([]);
  });
}

test("Technical docs are one click from every page", async ({ page, isMobile }) => {
  for (const p of PAGES) {
    await page.goto(p);
    if (isMobile) {
      await page.getByRole("button", { name: /open menu/i }).click();
      await page.getByRole("dialog", { name: /site menu/i }).getByRole("link", { name: "Technical docs" }).first().click();
    } else {
      await page.getByRole("navigation", { name: /primary/i }).getByRole("link", { name: "Technical docs" }).click();
    }
    await expect(page).toHaveURL(/\/notaire\/docs\/$/);
  }
});

test("mobile drawer traps focus, closes on Escape and returns focus", async ({ page, isMobile }) => {
  test.skip(!isMobile, "the drawer is the mobile menu");
  await page.goto("ai-sdlc/");
  const button = page.getByRole("button", { name: /open menu/i });
  await expect(button).toBeVisible();
  await button.click();
  const drawer = page.getByRole("dialog", { name: /site menu/i });
  await expect(drawer).toBeVisible();
  await expect(page.getByRole("banner").getByRole("button", { name: /close menu/i })).toHaveAttribute("aria-expanded", "true");
  for (let i = 0; i < 40; i += 1) {
    await page.keyboard.press("Tab");
    expect(await drawer.evaluate((d) => d.contains(document.activeElement))).toBe(true);
  }
  await page.keyboard.press("Escape");
  await expect(drawer).toBeHidden();
  await expect(button).toBeFocused();
});

test("search finds the quality gates page", async ({ page }) => {
  await page.goto("");
  await page.getByRole("button", { name: /search/i }).first().click();
  await page.getByRole("combobox").fill("quality gates");
  await page.getByRole("option").first().getByRole("link").click();
  await expect(page).toHaveURL(/ai-sdlc\/quality-gates\/$/);
});

test("factory pages show reading progress and previous/next links", async ({ page }) => {
  await page.goto("ai-sdlc/lifecycle/");
  const pager = page.getByRole("navigation", { name: /previous and next/i });
  await expect(pager.getByRole("link", { name: /OpenSpec/ })).toBeVisible();
  await expect(pager.getByRole("link", { name: /Case study/ })).toBeVisible();
  await expect(page.locator("[data-reading-progress]")).toHaveCount(1);
});
