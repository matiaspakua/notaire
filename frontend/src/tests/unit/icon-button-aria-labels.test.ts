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
    relativePath: "src/app/dashboard/administration/users/page.tsx",
    requiredSnippets: ['aria-label={tc("edit")}', 'aria-label={tc("delete")}'],
  },
  {
    relativePath: "src/app/dashboard/administration/roles/page.tsx",
    requiredSnippets: ['aria-label={tc("edit")}', 'aria-label={tc("delete")}'],
  },
  {
    relativePath: "src/app/dashboard/deeds/page.tsx",
    requiredSnippets: ['aria-label={tc("edit")}', 'aria-label={tc("delete")}'],
  },
  {
    relativePath: "src/app/dashboard/payments/page.tsx",
    requiredSnippets: ['aria-label={tc("edit")}', 'aria-label={tc("delete")}'],
  },
  {
    relativePath: "src/app/dashboard/people/page.tsx",
    requiredSnippets: ['aria-label={tc("edit")}', 'aria-label={tc("delete")}'],
  },
  {
    relativePath: "src/app/dashboard/budgets/page.tsx",
    requiredSnippets: ['aria-label={t("resumen.title")}'],
  },
  {
    relativePath: "src/app/dashboard/administration/concepts/page.tsx",
    requiredSnippets: ['aria-label={tc("edit")}', 'aria-label={tc("delete")}'],
  },
  {
    relativePath: "src/app/dashboard/administration/documents/page.tsx",
    requiredSnippets: ['aria-label={tc("edit")}', 'aria-label={tc("delete")}'],
  },
  {
    relativePath: "src/app/dashboard/administration/procedures/page.tsx",
    requiredSnippets: ['aria-label={tc("edit")}', 'aria-label={tc("delete")}'],
  },
];

describe("icon-only Buttons expose aria-label (#1057)", () => {
  it("inventory covers nine pages (17 actions)", () => {
    const snippetCount = INVENTORY.reduce((n, e) => n + e.requiredSnippets.length, 0);
    expect(INVENTORY).toHaveLength(9);
    // 8 pages × edit+delete + budgets summary = 17
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
