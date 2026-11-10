import { expect, test } from "@playwright/test";
import AxeBuilder from "@axe-core/playwright";

const DARK_PAGES = ["", "ai-sdlc/", "ai-sdlc/framework/", "product/", "security/", "docs/"];

for (const path of DARK_PAGES) {
  test(`/${path} has no axe violations in dark`, async ({ page }) => {
    await page.addInitScript(() => localStorage.setItem("notaire.theme", "dark"));
    await page.goto(path);
    await expect(page.locator("html")).toHaveAttribute("data-theme", "dark");
    await page.waitForLoadState("networkidle");
    const overflow = await page.evaluate(() => document.documentElement.scrollWidth - document.documentElement.clientWidth);
    expect(overflow, "page scrolls sideways").toBeLessThanOrEqual(1);
    const axe = await new AxeBuilder({ page }).withTags(["wcag2a", "wcag2aa"]).analyze();
    expect(axe.violations.map((v) => `${v.id}: ${v.nodes.map((n) => n.target.join(" ")).slice(0, 3).join(", ")}`)).toEqual([]);
  });
}

test("Spanish home and factory, English notice on an untranslated page", async ({ page }) => {
  await page.addInitScript(() => localStorage.setItem("notaire.lang", "es"));
  await page.goto("");
  await expect(page.locator("h1")).toContainText("agentes de IA");
  const width = page.viewportSize()?.width ?? 1280;
  if (width < 1024) {
    await page.getByRole("button", { name: /abrir menú/i }).click();
    await expect(page.getByRole("dialog").getByRole("link", { name: "Documentación técnica" })).toBeVisible();
  } else {
    await expect(page.getByRole("navigation", { name: /principal/i }).getByRole("link", { name: "Documentación técnica" })).toBeVisible();
  }
  await page.goto("ai-sdlc/");
  await expect(page.locator("h1")).toContainText("AI Factory");
  await expect(page.getByRole("heading", { name: "Léelo en orden" })).toBeVisible();
  await page.goto("docs/hexagonal/");
  await expect(page.getByText("Esta página sigue en inglés")).toBeVisible();
  await expect(page.locator("[lang='en']")).toBeVisible();
});

test("theme choice survives reload and is keyboard reachable", async ({ page }) => {
  await page.goto("");
  const theme = page.getByRole("button", { name: /theme/i });
  await theme.focus();
  await page.keyboard.press("Enter");
  await page.keyboard.press("Enter");
  await expect(page.locator("html")).toHaveAttribute("data-theme", "dark");
  await page.reload();
  await expect(page.locator("html")).toHaveAttribute("data-theme", "dark");
});
