// @vitest-environment node
import { existsSync, readFileSync } from "node:fs";
import { resolve } from "node:path";
import { ESLint } from "eslint";
import { describe, expect, it } from "vitest";

/**
 * #1696 (epic #1693, ADR-030): the frontend hexagon is enforced by ESLint. These tests pin the rules
 * with virtual files; the shrink-only baseline of today's violations is eslint-suppressions.json.
 */
const root = resolve(__dirname, "../../..");
const eslint = new ESLint({ cwd: root });

async function violations(filePath: string, code: string): Promise<string[]> {
  const [result] = await eslint.lintText(code, { filePath: resolve(root, filePath) });
  return result.messages.filter((m) => m.ruleId?.startsWith("no-restricted")).map((m) => `${m.ruleId}: ${m.message}`);
}

describe("frontend import boundaries (ADR-030)", () => {
  it.each([
    ["src/hooks/useThing.ts", 'import { apiGet } from "@/lib/api-client";\nexport const x = apiGet;\n'],
    ["src/app/dashboard/thing/page.tsx", 'import { apiPost } from "@/lib/api-client";\nexport const x = apiPost;\n'],
    ["src/components/Thing.tsx", 'import { apiGet } from "../lib/api-client";\nexport const x = apiGet;\n'],
  ])("%s may not import the API client", async (file, code) => {
    expect(await violations(file, code)).toEqual([expect.stringContaining("no-restricted-imports")]);
  });

  it("UI code may not call fetch", async () => {
    expect(await violations("src/hooks/useThing.ts", 'export const x = () => fetch("/api/v1/things");\n')).toEqual([
      expect.stringContaining("no-restricted-globals"),
    ]);
  });

  it("adapters/http may import the API client", async () => {
    expect(await violations("src/adapters/http/thing/gateway.ts", 'import { apiGet } from "@/lib/api-client";\nexport const x = apiGet;\n')).toEqual([]);
  });

  it.each([
    'import { useState } from "react";\nexport const x = useState;\n',
    'import Link from "next/link";\nexport const x = Link;\n',
    'import { apiGet } from "@/lib/api-client";\nexport const x = apiGet;\n',
    'import { gateway } from "@/adapters/http/thing/gateway";\nexport const x = gateway;\n',
  ])("core may not import a framework, lib or adapter: %s", async (code) => {
    expect(await violations("src/core/thing/application/use-case.ts", code)).toEqual([
      expect.stringContaining("no-restricted-imports"),
    ]);
  });

  it("core may import itself and zod, and may not use browser globals", async () => {
    expect(await violations("src/core/thing/application/use-case.ts", 'import { z } from "zod";\nimport { Thing } from "@/core/thing/domain/thing";\nexport const x = [z, {} as Thing];\n')).toEqual([]);
    expect(await violations("src/core/thing/domain/thing.ts", "export const x = () => window.location.href;\n")).toEqual([
      expect.stringContaining("no-restricted-globals"),
    ]);
  });
});

describe("the shrink-only baseline", () => {
  const file = resolve(root, "eslint-suppressions.json");

  it("exists, is not empty and only suppresses the boundary rules", () => {
    expect(existsSync(file)).toBe(true);
    const entries = JSON.parse(readFileSync(file, "utf-8")) as Record<string, Record<string, unknown>>;
    const rules = new Set(Object.values(entries).flatMap((byRule) => Object.keys(byRule)));
    const boundaryRules = new Set(["no-restricted-globals", "no-restricted-imports"]);
    expect([...rules].filter((rule) => !boundaryRules.has(rule))).toEqual([]);
    expect(rules.size).toBeGreaterThan(0);
  });

  it("lists only files that still exist", () => {
    const entries = JSON.parse(readFileSync(file, "utf-8")) as Record<string, unknown>;
    const missing = Object.keys(entries).filter((path) => !existsSync(resolve(root, path)));
    expect(missing).toEqual([]);
  });

  it("identification-type catalog left the baseline (#1732)", () => {
    const entries = JSON.parse(readFileSync(file, "utf-8")) as Record<string, unknown>;
    expect(entries["src/hooks/useIdentificationTypes.ts"]).toBeUndefined();
    expect(entries["src/app/dashboard/administration/identification-types/page.tsx"]).toBeUndefined();
    expect(Object.keys(entries).length).toBeLessThan(66);
  });

  it("management-status catalog left the baseline (#1769)", () => {
    const entries = JSON.parse(readFileSync(file, "utf-8")) as Record<string, unknown>;
    expect(entries["src/hooks/useManagementStatuses.ts"]).toBeUndefined();
    expect(entries["src/app/dashboard/administration/management-statuses/page.tsx"]).toBeUndefined();
    expect(Object.keys(entries).length).toBeLessThan(64);
  });

  it("document-type catalog left the baseline (#1777)", () => {
    const entries = JSON.parse(readFileSync(file, "utf-8")) as Record<string, unknown>;
    expect(entries["src/hooks/useDocuments.ts"]).toBeUndefined();
    expect(entries["src/app/dashboard/administration/documents/page.tsx"]).toBeUndefined();
    expect(Object.keys(entries).length).toBeLessThan(61);
  });

  it("concept catalog left the baseline (#1781)", () => {
    const entries = JSON.parse(readFileSync(file, "utf-8")) as Record<string, unknown>;
    expect(entries["src/hooks/useConcepts.ts"]).toBeUndefined();
    expect(entries["src/app/dashboard/administration/concepts/page.tsx"]).toBeUndefined();
    expect(Object.keys(entries).length).toBeLessThan(59);
  });

  it("folios admin left the baseline (#1785)", () => {
    const entries = JSON.parse(readFileSync(file, "utf-8")) as Record<string, unknown>;
    expect(entries["src/app/dashboard/administration/folios/page.tsx"]).toBeUndefined();
    expect(Object.keys(entries).length).toBeLessThan(58);
  });

  it("workflows admin left the baseline (#1792)", () => {
    const entries = JSON.parse(readFileSync(file, "utf-8")) as Record<string, unknown>;
    expect(entries["src/hooks/useWorkflow.ts"]).toBeUndefined();
    expect(entries["src/hooks/useManagementWorkflow.ts"]).toBeUndefined();
    expect(entries["src/app/dashboard/administration/workflows/page.tsx"]).toBeUndefined();
    expect(entries["src/app/dashboard/administration/workflows/[id]/page.tsx"]).toBeUndefined();
    expect(Object.keys(entries).length).toBeLessThan(54);
  });


  it("procedures admin left the baseline (#1796)", () => {
    const entries = JSON.parse(readFileSync(file, "utf-8")) as Record<string, unknown>;
    expect(entries["src/hooks/useProcedureTypes.ts"]).toBeUndefined();
    expect(entries["src/hooks/useProcedureTypes.test.tsx"]).toBeUndefined();
    expect(entries["src/app/dashboard/administration/procedures/page.tsx"]).toBeUndefined();
    expect(entries["src/app/dashboard/administration/procedures/RequiredDocumentsDialog.tsx"]).toBeUndefined();
    expect(Object.keys(entries).length).toBeLessThan(54);
  });

  it("templates admin left the baseline (#1788)", () => {
    const entries = JSON.parse(readFileSync(file, "utf-8")) as Record<string, unknown>;
    expect(entries["src/hooks/useProcedureTemplate.ts"]).toBeUndefined();
    expect(entries["src/hooks/useProcedureTemplate.test.tsx"]).toBeUndefined();
    expect(entries["src/app/dashboard/administration/templates/page.tsx"]).toBeUndefined();
    expect(Object.keys(entries).length).toBeLessThan(54);
  });

  it("people hooks left the baseline (#1823)", () => {
    const entries = JSON.parse(readFileSync(file, "utf-8")) as Record<string, unknown>;
    expect(entries["src/hooks/usePeople.ts"]).toBeUndefined();
    expect(entries["src/hooks/usePersonSearch.ts"]).toBeUndefined();
    expect(Object.keys(entries).length).toBeLessThan(53);
  });

  it("budget hooks and dashboard left the baseline (#1827)", () => {
    const entries = JSON.parse(readFileSync(file, "utf-8")) as Record<string, unknown>;
    expect(entries["src/hooks/useBudgets.ts"]).toBeUndefined();
    expect(entries["src/hooks/useBudgetSearch.ts"]).toBeUndefined();
    expect(entries["src/hooks/useBudgets.test.tsx"]).toBeUndefined();
    expect(entries["src/app/dashboard/budgets/page.tsx"]).toBeUndefined();
    expect(Object.keys(entries).length).toBeLessThan(45);
  });

  it("deed hooks and dashboard left the baseline (#1831)", () => {
    const entries = JSON.parse(readFileSync(file, "utf-8")) as Record<string, unknown>;
    expect(entries["src/hooks/useDeeds.ts"]).toBeUndefined();
    expect(entries["src/hooks/useDeedSearch.ts"]).toBeUndefined();
    expect(entries["src/app/dashboard/deeds/page.tsx"]).toBeUndefined();
    expect(Object.keys(entries).length).toBeLessThan(42);
  });
});
