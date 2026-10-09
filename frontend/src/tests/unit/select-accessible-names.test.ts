/**
 * Static gate for #1343: every Radix <SelectTrigger> and native <select> in
 * src/app has an accessible name: aria-label/aria-labelledby on the control,
 * or a labelled <FormField> around it (FormField renders a wrapping <label>).
 */
import { readFileSync, readdirSync, statSync } from "node:fs";
import { join } from "node:path";
import { describe, expect, it } from "vitest";
import es from "../../../messages/es.json";
import en from "../../../messages/en.json";

function tsxFiles(dir: string): string[] {
  return readdirSync(dir).flatMap((name) => {
    const p = join(dir, name);
    return statSync(p).isDirectory() ? tsxFiles(p) : p.endsWith(".tsx") ? [p] : [];
  });
}

function unnamedSelects(src: string): number[] {
  const lines: number[] = [];
  const re = /<(SelectTrigger|select)\b([^>]*)>/g;
  for (let m = re.exec(src); m; m = re.exec(src)) {
    if (/\baria-label(ledby)?=/.test(m[2])) continue;
    const before = src.slice(0, m.index);
    const open = before.lastIndexOf("<FormField");
    const close = before.lastIndexOf("</FormField>");
    if (open > close && /^<FormField\b[^>]*\blabel=/.test(before.slice(open))) continue;
    lines.push(before.split("\n").length);
  }
  return lines;
}

describe("unnamedSelects()", () => {
  it("flags a bare trigger and accepts aria-label or a labelled FormField", () => {
    expect(unnamedSelects('<SelectTrigger className="w-48">')).toEqual([1]);
    expect(unnamedSelects('<SelectTrigger aria-label={t("x")}>')).toEqual([]);
    expect(unnamedSelects('<FormField label={t("x")}>\n<Select><SelectTrigger>')).toEqual([]);
    expect(unnamedSelects('<FormField label="a"><Input /></FormField>\n<select value={v}>')).toEqual([2]);
  });
});

describe("filter selects in src/app have accessible names (#1343)", () => {
  it("no SelectTrigger or select is left without a name", () => {
    const offenders = tsxFiles(join(__dirname, "../../app")).flatMap((f) =>
      unnamedSelects(readFileSync(f, "utf8")).map((line) => `${f.split("/src/")[1]}:${line}`)
    );
    expect(offenders).toEqual([]);
  });

  it("the filter labels exist in es and en", () => {
    for (const m of [es, en] as unknown as Array<Record<string, Record<string, unknown>>>) {
      const adm = m.administracion as Record<string, Record<string, string>>;
      expect((m.gestiones as Record<string, string>).clienteFilter).toBeTruthy();
      expect((m.presupuestos as Record<string, string>).estadoFilter).toBeTruthy();
      expect(adm.folios.estadoFilter).toBeTruthy();
      expect(adm.estadosGestion.workflowFilter).toBeTruthy();
      expect((m.auditoria as Record<string, string>).moduleFilter).toBeTruthy();
    }
  });
});
