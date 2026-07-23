import { describe, expect, it } from "vitest";
import nextConfig from "../../../next.config";

describe("next.config security headers", () => {
  it("declares a headers() function", () => {
    expect(typeof nextConfig.headers).toBe("function");
  });

  it("applies security headers to every route", async () => {
    const rules = await nextConfig.headers!();

    expect(rules).toHaveLength(1);
    expect(rules[0].source).toBe("/:path*");

    const headerNames = rules[0].headers.map((h) => h.key);
    expect(headerNames).toEqual(
      expect.arrayContaining([
        "Content-Security-Policy",
        "X-Frame-Options",
        "X-Content-Type-Options",
        "Strict-Transport-Security",
      ]),
    );
  });

  it("sets X-Frame-Options to DENY", async () => {
    const rules = await nextConfig.headers!();
    const header = rules[0].headers.find((h) => h.key === "X-Frame-Options");

    expect(header?.value).toBe("DENY");
  });

  it("sets X-Content-Type-Options to nosniff", async () => {
    const rules = await nextConfig.headers!();
    const header = rules[0].headers.find((h) => h.key === "X-Content-Type-Options");

    expect(header?.value).toBe("nosniff");
  });

  it("enables HSTS with a long max-age and includeSubDomains", async () => {
    const rules = await nextConfig.headers!();
    const header = rules[0].headers.find((h) => h.key === "Strict-Transport-Security");

    expect(header?.value).toMatch(/max-age=\d+/);
    expect(header?.value).toContain("includeSubDomains");
  });
});
