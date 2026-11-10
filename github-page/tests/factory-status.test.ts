import { execFileSync } from "node:child_process";
import { readFileSync } from "node:fs";
import path from "node:path";
import { describe, expect, it } from "vitest";
import { FRAMEWORK_COMMANDS } from "@/lib/ai-sdlc-content";
import { loadFactoryNow, readInventory, readPluginAllow } from "@/lib/factory-status";

const ROOT = path.resolve(__dirname, "../..");

describe("factory status on main (#2201)", () => {
  const profile = readFileSync(path.join(ROOT, ".aisdlc/aisdlc.toml"), "utf8");
  const inventory = readFileSync(path.join(ROOT, ".aisdlc/migration-inventory.toml"), "utf8");

  it("loads only the plugins the profile allows", () => {
    expect(readPluginAllow(profile)).toEqual(["openspec", "example-method"]);
    const now = loadFactoryNow();
    expect(now.loadedPlugins).toEqual(["openspec", "example-method"]);
    expect(now.loadedPlugins).not.toContain("spec-kit");
    expect(now.loadedPlugins).not.toContain("aws-ai-dlc");
  });

  it("keeps planned inventory rows planned", () => {
    const parsed = readInventory(inventory);
    expect(parsed.plannedSources).toContain("workspace/sdlc");
    const now = loadFactoryNow();
    expect(now.plannedSources).toContain("workspace/sdlc");
    expect(now.es.plannedTitle.toLowerCase()).toMatch(/plan/);
    expect(now.en.isolator.toLowerCase()).toMatch(/probe/);
    expect(now.en.isolator.toLowerCase()).not.toMatch(/always sandboxed|is sandboxed/);
  });

  it("only names kit commands that aisdlc --help lists", () => {
    const help = execFileSync("python3", ["framework/ai-sdlc/bin/aisdlc", "--help"], {
      cwd: ROOT,
      encoding: "utf8",
    });
    for (const row of FRAMEWORK_COMMANDS) {
      const token = row.cmd.split(/[\s|]/)[0];
      expect(help, token).toContain(token);
    }
    for (const token of loadFactoryNow().commands) {
      expect(help, token).toContain(token);
    }
  });
});
