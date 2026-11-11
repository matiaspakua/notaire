// @vitest-environment node
import { describe, expect, it } from "vitest";
import { isTotpCode, normalizeRecoveryCode, normalizeTotpCode } from "./mfa";

describe("mfa domain helpers", () => {
  it("accepts exactly six digits as a TOTP code", () => {
    expect(isTotpCode("123456")).toBe(true);
    expect(isTotpCode("12345")).toBe(false);
    expect(isTotpCode("1234567")).toBe(false);
    expect(isTotpCode("12345a")).toBe(false);
  });

  it("strips whitespace from a pasted TOTP code", () => {
    expect(normalizeTotpCode(" 123 456 ")).toBe("123456");
  });

  it("normalizes a recovery code: no spaces or hyphens, upper case", () => {
    expect(normalizeRecoveryCode(" abcde-12345 ")).toBe("ABCDE12345");
  });
});
