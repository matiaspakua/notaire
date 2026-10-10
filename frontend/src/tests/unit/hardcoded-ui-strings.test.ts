/**
 * Static i18n gate for #1354: user-visible and assistive-technology strings in
 * src/app and src/components must come from messages/*.json, not literals.
 *
 * Scans every non-test .tsx file (comments stripped) for:
 * - string-literal attributes: placeholder, aria-label, title, label, helperText,
 *   emptyMessage, description, alt
 * - column `header: "..."`, `toast.*("...")` and default props `emptyMessage = "..."`
 * - JSX text nodes (text between `>` and `</` or `{`)
 * - any string literal with Spanish letters (á é í ó ú ñ ¿ ¡ º ª)
 */
import { readFileSync, readdirSync, statSync } from "node:fs";
import { join, relative } from "node:path";
import { describe, it, expect } from "vitest";

const ROOT = join(__dirname, "../../..");
const SCAN_DIRS = ["src/app", "src/components"];

/** Literals that are not translatable UI text. Keep this list short. */
const ALLOWED = new Set<string>([
  "Notaire", // brand name (login card title)
  // Backend status codes compared against API data, never rendered as labels.
  "Presentado para inscripción",
  "no pasó",
]);

const LETTER = "A-Za-zÁÉÍÓÚáéíóúñÑ";
const RULES: { name: string; re: RegExp }[] = [
  {
    name: "attribute",
    re: new RegExp(
      `\\b(?:placeholder|aria-label|title|label|helperText|emptyMessage|description|alt)="([^"]*[${LETTER}]{2,}[^"]*)"`,
      "g",
    ),
  },
  { name: "column header", re: /\bheader:\s*"([^"]+)"/g },
  { name: "toast", re: /toast\.\w+\(\s*"([^"]+)"/g },
  { name: "default prop", re: /\b(?:emptyMessage|placeholder|label|title)\s*=\s*"([^"]+)",/g },
  {
    name: "JSX text",
    re: new RegExp(`>\\s*([^<>{}]*?[${LETTER}]{2,}[^<>{}]*?)\\s*(?=</|\\{)`, "g"),
  },
  { name: "Spanish literal", re: /"([^"\n]*[áéíóúñÁÉÍÓÚÑ¿¡ºª][^"\n]*)"/g },
];

function listTsx(dir: string): string[] {
  return readdirSync(dir).flatMap((name) => {
    const path = join(dir, name);
    if (statSync(path).isDirectory()) return listTsx(path);
    return path.endsWith(".tsx") && !path.includes(".test.") ? [path] : [];
  });
}

function stripComments(source: string): string {
  return source.replace(/\/\*[\s\S]*?\*\//g, "").replace(/(^|\s)\/\/.*$/gm, "$1");
}

export function findHardcodedStrings(source: string): string[] {
  const code = stripComments(source);
  const hits: string[] = [];
  for (const { name, re } of RULES) {
    for (const match of code.matchAll(re)) {
      const text = match[1].trim();
      if (name === "JSX text" && (/[=;()&|?]/.test(text) || text.startsWith(","))) continue;
      if (ALLOWED.has(text)) continue;
      const line = code.slice(0, match.index).split("\n").length;
      hits.push(`${line} ${name}: ${text}`);
    }
  }
  return hits;
}

describe("hardcoded UI strings (#1354)", () => {
  const files = SCAN_DIRS.flatMap((d) => listTsx(join(ROOT, d)));

  it("scans the app and component sources", () => {
    expect(files.length).toBeGreaterThan(50);
  });

  it("has no hardcoded user-visible or aria strings", () => {
    const offenders = files.flatMap((file) =>
      findHardcodedStrings(readFileSync(file, "utf8")).map(
        (hit) => `${relative(ROOT, file)}:${hit}`,
      ),
    );
    expect(offenders).toEqual([]);
  });

  it("flags a re-added literal of each kind", () => {
    expect(findHardcodedStrings('<Input placeholder="Seleccionar tipo" />')).toHaveLength(1);
    expect(findHardcodedStrings('<div aria-label="Language selector">')).toHaveLength(1);
    expect(findHardcodedStrings('{ key: "x", header: "Presupuesto" }')).toHaveLength(1);
    expect(findHardcodedStrings('toast.success("Reporte descargado");')).toHaveLength(1);
    expect(findHardcodedStrings("<Badge>Disponible</Badge>")).toHaveLength(1);
    expect(findHardcodedStrings('const m = { a: "Generar Declaración Jurada" };')).toHaveLength(1);
  });

  it("accepts translated strings and code", () => {
    expect(findHardcodedStrings('<Input placeholder={t("selectType")} />')).toEqual([]);
    expect(findHardcodedStrings("<Badge>{t('available')}</Badge>")).toEqual([]);
    expect(findHardcodedStrings("const q = apiGet<Folio[]>(`/folios`);")).toEqual([]);
    expect(findHardcodedStrings("// Comentario en español: más\nconst a = 1;")).toEqual([]);
  });
});
