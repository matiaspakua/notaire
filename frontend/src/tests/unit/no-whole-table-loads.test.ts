/**
 * Guard for #1340: no source loads a whole growing table. apiGetPaged asked
 * for size=1000 and silently dropped every row after the 1000th; every list
 * now reads one server page and every dropdown searches the server.
 */
import { describe, it, expect } from "vitest";
import { readFileSync, readdirSync, statSync } from "node:fs";
import { join } from "node:path";

const SRC = join(__dirname, "..", "..");

function sources(dir: string): string[] {
  return readdirSync(dir).flatMap((f) => {
    const p = join(dir, f);
    if (statSync(p).isDirectory()) return f === "tests" ? [] : sources(p);
    return /\.tsx?$/.test(f) ? [p] : [];
  });
}

describe("no whole-table loads (#1340)", () => {
  it("no source defines or calls apiGetPaged or asks for size=1000", () => {
    const offenders = sources(SRC).filter((f) => {
      const code = readFileSync(f, "utf8").replace(/\/\*[\s\S]*?\*\/|\/\/.*$/gm, "");
      return /\bapiGetPaged\b|PAGE_SIZE_ALL|size=1000/.test(code);
    });
    expect(offenders.map((f) => f.slice(SRC.length + 1))).toEqual([]);
  });

  it("the unused size=1000 hooks are gone", () => {
    const hooks = readdirSync(join(SRC, "hooks")).map((f) => readFileSync(join(SRC, "hooks", f), "utf8")).join("\n");
    expect(hooks).not.toMatch(/export function (usePersonas|useEscrituras|useGestiones|usePresupuestos)\(\)/);
  });
});
