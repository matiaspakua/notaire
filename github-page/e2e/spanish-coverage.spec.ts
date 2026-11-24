import { expect, test } from "@playwright/test";

import { TRANSLATED_ROUTES } from "../lib/i18n/es";

/**
 * #2475: 240 English lines were found on Spanish pages. Every translated route is read in Spanish and a
 * line that is clearly English fails. Quoted Issue titles and real command output stay verbatim.
 */
const EN = /\b(the|and|with|when|what|which|every|each|this|that|from|into|only|before|after|without|does|is|are|was|were|has|have|its|their|you|your|it|of|to|for|on|in|by)\b/gi;
const ES = /\b(el|la|los|las|y|con|cuando|qué|que|cada|este|esta|desde|antes|después|sin|es|son|fue|tiene|su|sus|de|del|para|por|en|un|una|se|no|lo)\b/gi;
const VERBATIM = /^(feat|fix|test|docs|chore|refactor)\(|^\s*(fix the violation|TODO )|\.(sh|py|yml|md|json|ts)\b|^(python3|bash|npm|git|mvn|aisdlc|cd) /;

test.describe("Spanish pages have no English prose", () => {
  test.skip(({ isMobile }) => isMobile, "one viewport is enough for a text check");

  for (const route of TRANSLATED_ROUTES) {
    test(route, async ({ page }) => {
      await page.addInitScript(() => localStorage.setItem("notaire.lang", "es"));
      await page.goto(`.${route}`, { waitUntil: "networkidle" });
      const text = await page.locator("main").innerText();
      const english = text
        .split(/\n+/)
        .flatMap((line) => line.split("\t"))
        .map((line) => line.trim())
        .filter((line) => line.length > 25 && !VERBATIM.test(line))
        .filter((line) => {
          const en = (line.match(EN) ?? []).length;
          const es = (line.match(ES) ?? []).length;
          return en >= 3 && en > es * 1.5;
        });
      expect(english, `English on ${route}`).toEqual([]);
    });
  }
});
