/**
 * Static regression gate for #1057 — icon-only dashboard Buttons must declare
 * translated aria-label at the call site (stock jsx-a11y does not reliably
 * flag Lucide / NotaireIcon children).
 */
import { readFileSync } from "node:fs";
import { join } from "node:path";
import { describe, it, expect } from "vitest";

const ROOT = join(__dirname, "../../..");

type InventoryEntry = {
  relativePath: string;
  requiredSnippets: string[];
};

/** Inventoried icon-only row actions from OpenSpec / issue #1057 (17 buttons). */
const INVENTORY: InventoryEntry[] = [
  {
    relativePath: "src/app/dashboard/administracion/usuarios/page.tsx",
    requiredSnippets: ['aria-label={tc("edit")}', 'aria-label={tc("delete")}'],
  },
  {
    relativePath: "src/app/dashboard/administracion/roles/page.tsx",
    requiredSnippets: ['aria-label={tc("edit")}', 'aria-label={tc("delete")}'],
  },
  {
    relativePath: "src/app/dashboard/escrituras/page.tsx",
    requiredSnippets: ['aria-label={tc("edit")}', 'aria-label={tc("delete")}'],
  },
  {
    relativePath: "src/app/dashboard/pagos/page.tsx",
    requiredSnippets: ['aria-label={tc("edit")}', 'aria-label={tc("delete")}'],
  },
  {
    relativePath: "src/app/dashboard/personas/page.tsx",
    requiredSnippets: ['aria-label={tc("edit")}', 'aria-label={tc("delete")}'],
  },
  {
    relativePath: "src/app/dashboard/presupuestos/page.tsx",
    requiredSnippets: ['aria-label={t("resumen.title")}'],
  },
  {
    relativePath: "src/app/dashboard/administracion/conceptos/page.tsx",
    requiredSnippets: ['aria-label={tc("edit")}', 'aria-label={tc("delete")}'],
  },
  {
    relativePath: "src/app/dashboard/administracion/documentos/page.tsx",
    requiredSnippets: ['aria-label={tc("edit")}', 'aria-label={tc("delete")}'],
  },
  {
    relativePath: "src/app/dashboard/administracion/tramites/page.tsx",
    requiredSnippets: ['aria-label={tc("edit")}', 'aria-label={tc("delete")}'],
  },
];

describe("icon-only Buttons expose aria-label (#1057)", () => {
  it("inventory covers nine pages (17 actions)", () => {
    const snippetCount = INVENTORY.reduce((n, e) => n + e.requiredSnippets.length, 0);
    expect(INVENTORY).toHaveLength(9);
    // 8 pages × edit+delete + presupuestos resumen = 17
    expect(snippetCount).toBe(17);
  });

  for (const entry of INVENTORY) {
    it(`${entry.relativePath} includes required aria-label snippets`, () => {
      const src = readFileSync(join(ROOT, entry.relativePath), "utf8");
      for (const snippet of entry.requiredSnippets) {
        expect(src, `missing ${snippet} in ${entry.relativePath}`).toContain(snippet);
      }
    });
  }
});
