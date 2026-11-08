import { expect, test } from "@playwright/test";
import AxeBuilder from "@axe-core/playwright";

const PAGES = ["", "product/", "product/story/", "product/platform/", "product/numbers/", "docs/", "docs/stack/", "docs/hexagonal/", "docs/database/", "docs/api/", "docs/architecture/", "docs/testing/", "docs/releases/", "security/", "security/architecture/", "security/threats/", "security/toolchain/", "security/posture/", "ai-sdlc/", "ai-sdlc/lifecycle/", "ai-sdlc/guardrails/", "ai-sdlc/quality-gates/", "ai-sdlc/security-gates/", "ai-sdlc/agents/", "ai-sdlc/results/", "ai-sdlc/case-study/", "about/"];

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

test("product pages share the sidebar and the pager (#1976)", async ({ page, isMobile }) => {
  await page.goto("product/story/");
  if (!isMobile) {
    const sidebar = page.getByRole("navigation", { name: /product pages/i });
    for (const name of ["Overview", "Story", "Platform", "Numbers"]) await expect(sidebar.getByRole("link", { name })).toBeVisible();
  }
  const pager = page.getByRole("navigation", { name: /previous and next/i });
  await expect(pager.getByRole("link", { name: /Platform/ })).toBeVisible();
});

test("diagrams step with the keyboard and announce each step (#1977)", async ({ page, isMobile }) => {
  await page.goto("docs/hexagonal/");
  const figure = page.locator('[data-diagram="request"]');
  const live = figure.locator("#request-live");
  await expect(live).toContainText("The UI posts a payment");
  await page.waitForTimeout(3000);
  await expect(live, "nothing autoplays").toContainText("The UI posts a payment");
  await figure.getByRole("button", { name: /next step/i }).focus();
  await page.keyboard.press("ArrowRight");
  await expect(live).toContainText("The controller makes a command");
  await expect(figure.getByRole("img")).toBeVisible({ visible: !isMobile });
});

test("reduced motion still steps, instantly (#1977)", async ({ browser }) => {
  const context = await browser.newContext({ reducedMotion: "reduce" });
  const page = await context.newPage();
  await page.goto("http://localhost:4173/notaire/docs/stack/");
  const figure = page.locator('[data-diagram="pipeline"]');
  await figure.getByRole("button", { name: /next step/i }).click();
  await expect(figure.locator("#pipeline-live")).toContainText("Push");
  await context.close();
});
