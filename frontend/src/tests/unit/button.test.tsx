/**
 * #1369 — touch targets: small buttons stay 32px on desktop but grow to at
 * least 44x44 on coarse pointers (WCAG 2.5.5 / ui-ux-design.md), and small
 * actions are not packed 4px apart.
 */
import { describe, it, expect } from "vitest";
import { readFileSync, readdirSync, statSync } from "node:fs";
import { join, resolve } from "node:path";
import { render, screen } from "@testing-library/react";
import { Button, buttonVariants } from "@/components/ui/button";

const SRC = resolve(__dirname, "../..");

function walk(dir: string): string[] {
  return readdirSync(dir).flatMap((name) => {
    const p = join(dir, name);
    if (statSync(p).isDirectory()) return name === "tests" ? [] : walk(p);
    return p.endsWith(".tsx") ? [p] : [];
  });
}

describe("Button sizes (#1369)", () => {
  it("sm keeps 32px on desktop and is at least 44x44 on coarse pointers", () => {
    const cls = buttonVariants({ size: "sm" }).split(/\s+/);
    expect(cls).toContain("h-8");
    expect(cls).toContain("pointer-coarse:h-11");
    expect(cls).toContain("pointer-coarse:min-w-11");
  });

  it("icon-sm is a 32px square on desktop and 44px on coarse pointers", () => {
    const cls = buttonVariants({ size: "icon-sm" }).split(/\s+/);
    for (const c of ["h-8", "w-8", "pointer-coarse:h-11", "pointer-coarse:w-11"]) expect(cls).toContain(c);
  });

  it("renders an icon-sm button", () => {
    render(
      <Button size="icon-sm" aria-label="Edit">
        <svg aria-hidden="true" />
      </Button>,
    );
    expect(screen.getByRole("button", { name: "Edit" }).className).toContain("w-8");
  });
});

describe("small action spacing (#1369)", () => {
  it("no gap-1 container holds small buttons (actions need 8px between them)", () => {
    const offenders: string[] = [];
    for (const file of walk(SRC)) {
      const lines = readFileSync(file, "utf8").split("\n");
      lines.forEach((line, i) => {
        if (!/className="[^"]*\bgap-1\b(?!\.)[^"]*"/.test(line)) return;
        const next = lines.slice(i + 1, i + 4).join("\n");
        if (/size="(sm|icon-sm)"/.test(next)) offenders.push(`${file.replace(SRC + "/", "")}:${i + 1}`);
      });
    }
    expect(offenders).toEqual([]);
  });

  it("the rows-per-page select is 44px tall on coarse pointers", () => {
    const src = readFileSync(join(SRC, "components/shared/Pagination.tsx"), "utf8");
    expect(src).toMatch(/<select[\s\S]*?pointer-coarse:h-11/);
  });
});
