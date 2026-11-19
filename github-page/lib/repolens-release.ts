/**
 * Build-time RepoLens release facts (#2315). The version and the current changelog
 * section are read from the package so the Pages showcase cannot drift from the checkout.
 */
import { readFileSync } from "node:fs";
import path from "node:path";

const ROOT = path.resolve(process.cwd(), "..");

export type RepoLensRelease = {
  version: string;
  highlights: string[];
};

export function changelogHighlights(markdown: string): string[] {
  const head = markdown.search(/^## \[/m);
  if (head < 0) return [];
  const rest = markdown.slice(head);
  const next = rest.slice(1).search(/^## \[/m);
  const section = next < 0 ? rest : rest.slice(0, next + 1);
  return section
    .split("\n")
    .filter((line) => line.startsWith("- "))
    .map((line) => line.slice(2).trim())
    .filter(Boolean);
}

export function loadRepoLensRelease(): RepoLensRelease {
  const init = readFileSync(
    path.join(ROOT, "framework/ai-sdlc/repolens/repolens/__init__.py"),
    "utf8",
  );
  const version = init.match(/__version__\s*=\s*"([^"]+)"/)?.[1] ?? "";
  const changelog = readFileSync(
    path.join(ROOT, "framework/ai-sdlc/repolens/CHANGELOG.md"),
    "utf8",
  );
  return { version, highlights: changelogHighlights(changelog) };
}
