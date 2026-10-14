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

  it("exists and only suppresses the boundary rules", () => {
    expect(existsSync(file)).toBe(true);
    const entries = JSON.parse(readFileSync(file, "utf-8")) as Record<string, Record<string, unknown>>;
    const rules = new Set(Object.values(entries).flatMap((byRule) => Object.keys(byRule)));
    expect([...rules].sort()).toEqual(["no-restricted-globals", "no-restricted-imports"]);
  });

  it("lists only files that still exist", () => {
    const entries = JSON.parse(readFileSync(file, "utf-8")) as Record<string, unknown>;
    const missing = Object.keys(entries).filter((path) => !existsSync(resolve(root, path)));
    expect(missing).toEqual([]);
  });
});
