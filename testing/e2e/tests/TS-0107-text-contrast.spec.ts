/**
 * TS-0107 - Text contrast meets WCAG AA (issue #1341)
 *
 * Covers: RNF-09 (Uso de colores en la GUI), CU76
 * Issue: #1341 — axe reported color-contrast on every dashboard route: table
 * headers (#86868B, 3.3:1), the sidebar subtitle (4.4:1), delete icons
 * (#ED2C2C, 4.2:1), the workflow legend and report card descriptions.
 *
 * The check walks every visible element with its own text, resolves the
 * effective background by compositing ancestor background colours and
 * requires 4.5:1 (3:1 for large text, WCAG 1.4.3). Elements painted over a
 * gradient or image are skipped, as axe does ("needs review").
 */
import { test, expect, type Page } from "@playwright/test";

const ROUTES = [
  "/dashboard",
  "/dashboard/personas",
  "/dashboard/gestiones",
  "/dashboard/reportes",
  "/dashboard/administracion/workflows",
];

async function login(page: Page) {
  await page.goto("/login");
  await page.getByTestId("input-usuario").fill("admin");
  await page.getByTestId("input-contrasenia").fill("admin");
  await page.getByTestId("btn-ingresar").click();
  await expect(page).toHaveURL(/\/dashboard/, { timeout: 15000 });
}

type Offender = { text: string; ratio: number; fg: string; bg: string };

async function lowContrastText(page: Page): Promise<Offender[]> {
  return page.evaluate(() => {
    // Normalise any CSS colour (rgb, lab, oklch...) through a 1x1 canvas.
    const ctx = document.createElement("canvas").getContext("2d", { willReadFrequently: true })!;
    const parse = (c: string): number[] => {
      ctx.clearRect(0, 0, 1, 1);
      ctx.fillStyle = "#000";
      ctx.fillStyle = c;
      ctx.fillRect(0, 0, 1, 1);
      const [r, g, b, a] = ctx.getImageData(0, 0, 1, 1).data;
      return [r, g, b, a / 255];
    };
    const over = (top: number[], under: number[]): number[] => {
      const a = top[3];
      return [0, 1, 2].map((i) => top[i] * a + under[i] * (1 - a)).concat(1);
    };
    const lum = (c: number[]) => {
      const l = c.slice(0, 3).map((v) => {
        const s = v / 255;
        return s <= 0.03928 ? s / 12.92 : ((s + 0.055) / 1.055) ** 2.4;
      });
      return 0.2126 * l[0] + 0.7152 * l[1] + 0.0722 * l[2];
    };
    // Effective background: the painted stack under the element's centre
    // (this also catches absolutely positioned pills behind the text).
    const background = (el: Element, x: number, y: number): number[] | null => {
      const stack = document.elementsFromPoint(x, y);
      const start = stack.indexOf(el);
      const layers: number[][] = [];
      for (const e of start >= 0 ? stack.slice(start) : [el]) {
        const cs = getComputedStyle(e);
        if (cs.backgroundImage !== "none") return null;
        const bg = parse(cs.backgroundColor);
        if (bg[3] > 0) layers.push([...bg.slice(0, 3), bg[3] * Number(cs.opacity)]);
        if (bg[3] >= 1 && Number(cs.opacity) >= 1) break;
      }
      return layers.reduceRight((acc, layer) => over(layer, acc), [255, 255, 255, 1]);
    };
    const out: Offender[] = [];
    for (const el of Array.from(document.querySelectorAll("body *"))) {
      const own = Array.from(el.childNodes).some((n) => n.nodeType === 3 && n.textContent!.trim() !== "");
      if (!own) continue;
      const rect = (el as HTMLElement).getBoundingClientRect();
      const cs = getComputedStyle(el);
      if (rect.width === 0 || rect.height === 0 || cs.visibility === "hidden" || Number(cs.opacity) === 0) continue;
      if (el.closest("[aria-hidden=true], [disabled], [aria-disabled=true], svg, option")) continue;
      const x = rect.left + rect.width / 2;
      const y = rect.top + rect.height / 2;
      if (x < 0 || y < 0 || x > innerWidth || y > innerHeight) continue; // off-screen: covered by scrolling
      const bg = background(el, x, y);
      if (!bg) continue;
      const fg = over(parse(cs.color), bg);
      const [hi, lo] = [lum(fg), lum(bg)].sort((a, b) => b - a);
      const ratio = (hi + 0.05) / (lo + 0.05);
      const size = parseFloat(cs.fontSize);
      const bold = Number(cs.fontWeight) >= 700;
      const min = size >= 24 || (bold && size >= 18.66) ? 3 : 4.5;
      if (ratio < min - 0.01) {
        out.push({ text: el.textContent!.trim().slice(0, 40), ratio: Math.round(ratio * 100) / 100, fg: cs.color, bg: `rgb(${bg.slice(0, 3).map(Math.round).join(", ")})` });
      }
    }
    return out;
  });
}

for (const viewport of [{ width: 1440, height: 900 }, { width: 390, height: 844 }]) {
  test.describe(`TS-0107 - text contrast at ${viewport.width}px (#1341)`, () => {
    test.use({ viewport });

    test("login page text reaches 4.5:1", async ({ page }) => {
      await page.goto("/login");
      await expect(page.getByTestId("btn-ingresar")).toBeVisible();
      expect(await lowContrastText(page)).toEqual([]);
    });

    for (const route of ROUTES) {
      test(`${route} text reaches 4.5:1`, async ({ page }) => {
        await login(page);
        await page.goto(route);
        await page.waitForLoadState("networkidle");
        await page.waitForTimeout(600); // let entrance animations settle
        expect(await lowContrastText(page)).toEqual([]);
      });
    }
  });
}
