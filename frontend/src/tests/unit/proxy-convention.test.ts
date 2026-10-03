import { existsSync, readFileSync } from "node:fs";
import { resolve } from "node:path";
import { describe, expect, it } from "vitest";

/**
 * Gate 2 file/export asserts for issue #1056 (Next 16 middleware → proxy).
 * These fail on the pre-change tree (middleware.ts present, proxy.ts absent).
 */
describe("Next 16 edge proxy convention (issue #1056)", () => {
  const srcRoot = resolve(__dirname, "../..");
  const proxyPath = resolve(srcRoot, "proxy.ts");
  const middlewarePath = resolve(srcRoot, "middleware.ts");

  it("exposes frontend/src/proxy.ts with export function proxy", () => {
    expect(existsSync(proxyPath), "frontend/src/proxy.ts must exist").toBe(true);
    const source = readFileSync(proxyPath, "utf8");
    expect(source).toMatch(/export\s+function\s+proxy\s*\(/);
    expect(source).not.toMatch(/export\s+function\s+middleware\s*\(/);
  });

  it("removes deprecated frontend/src/middleware.ts", () => {
    expect(existsSync(middlewarePath), "frontend/src/middleware.ts must not remain").toBe(
      false,
    );
  });

  it("preserves /api/ skip and CSP nonce helpers in the proxy module", () => {
    expect(existsSync(proxyPath)).toBe(true);
    const source = readFileSync(proxyPath, "utf8");
    expect(source).toContain('pathname.startsWith("/api/")');
    expect(source).toContain("buildContentSecurityPolicy");
    expect(source).toContain("AUTH_STATUS_COOKIE");
    expect(source).toContain("AUTH_ROLE_COOKIE");
    expect(source).toContain("shouldDenyAdminRoute");
  });
});
