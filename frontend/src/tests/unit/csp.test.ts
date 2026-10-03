import { describe, expect, it } from "vitest";
import { buildContentSecurityPolicy, isHardenedProductionCsp } from "@/lib/csp";

describe("production CSP hardening (issue #1051)", () => {
  it("omits unsafe-eval and includes a nonce in script-src", () => {
    const csp = buildContentSecurityPolicy({
      nonce: "testNonce123",
      isProduction: true,
    });

    expect(csp).toContain("script-src 'self' 'nonce-testNonce123' 'strict-dynamic'");
    expect(csp).not.toContain("unsafe-eval");
    expect(csp).not.toMatch(/script-src[^;]*'unsafe-inline'/);
    expect(isHardenedProductionCsp(csp)).toBe(true);
  });

  it("may allow unsafe-eval only outside production (HMR)", () => {
    const csp = buildContentSecurityPolicy({
      nonce: "devNonce",
      isProduction: false,
    });

    expect(csp).toContain("nonce-devNonce");
    expect(csp).toContain("unsafe-eval");
    expect(isHardenedProductionCsp(csp)).toBe(false);
  });
});
