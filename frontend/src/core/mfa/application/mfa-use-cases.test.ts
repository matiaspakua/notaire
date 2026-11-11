// @vitest-environment node
import { describe, expect, it, vi } from "vitest";
import type { MfaGateway } from "./ports";
import { createMfaUseCases } from "./use-cases";
import { InvalidCodeFormatError } from "../domain/mfa";

function fakeGateway(): MfaGateway {
  return {
    completeLogin: vi.fn().mockResolvedValue({ name: "a", valid: true }),
    beginEnrollment: vi.fn().mockResolvedValue({ secret: "S", otpauthUri: "otpauth://totp/x" }),
    confirmEnrollment: vi.fn().mockResolvedValue(["R1", "R2"]),
    disable: vi.fn().mockResolvedValue(undefined),
  };
}

describe("createMfaUseCases", () => {
  it("posts a TOTP as code", async () => {
    const gw = fakeGateway();
    await createMfaUseCases(gw).completeLogin({ mfaToken: "t", mode: "totp", value: "123 456" });
    expect(gw.completeLogin).toHaveBeenCalledWith({ mfaToken: "t", code: "123456" });
  });

  it("posts a recovery code as recoveryCode, normalized", async () => {
    const gw = fakeGateway();
    await createMfaUseCases(gw).completeLogin({ mfaToken: "t", mode: "recovery", value: "abcde-12345" });
    expect(gw.completeLogin).toHaveBeenCalledWith({ mfaToken: "t", recoveryCode: "ABCDE12345" });
  });

  it("refuses a malformed TOTP without calling the gateway", async () => {
    const gw = fakeGateway();
    await expect(
      createMfaUseCases(gw).completeLogin({ mfaToken: "t", mode: "totp", value: "12ab" }),
    ).rejects.toBeInstanceOf(InvalidCodeFormatError);
    expect(gw.completeLogin).not.toHaveBeenCalled();
  });

  it("refuses an empty recovery code", async () => {
    const gw = fakeGateway();
    await expect(
      createMfaUseCases(gw).completeLogin({ mfaToken: "t", mode: "recovery", value: "  " }),
    ).rejects.toBeInstanceOf(InvalidCodeFormatError);
  });

  it("begins enrolment forwarding the optional bearer token", async () => {
    const gw = fakeGateway();
    const start = await createMfaUseCases(gw).beginEnrollment("tok");
    expect(gw.beginEnrollment).toHaveBeenCalledWith("tok");
    expect(start.secret).toBe("S");
  });

  it("confirms enrolment with a validated code and returns recovery codes", async () => {
    const gw = fakeGateway();
    const codes = await createMfaUseCases(gw).confirmEnrollment("654 321", "tok");
    expect(gw.confirmEnrollment).toHaveBeenCalledWith("654321", "tok");
    expect(codes).toEqual(["R1", "R2"]);
  });

  it("refuses to confirm a malformed code", async () => {
    const gw = fakeGateway();
    await expect(createMfaUseCases(gw).confirmEnrollment("abc")).rejects.toBeInstanceOf(
      InvalidCodeFormatError,
    );
    expect(gw.confirmEnrollment).not.toHaveBeenCalled();
  });

  it("disables with a TOTP or a recovery code", async () => {
    const gw = fakeGateway();
    const uc = createMfaUseCases(gw);
    await uc.disable({ mode: "totp", value: "123456" });
    await uc.disable({ mode: "recovery", value: "ab-cd" });
    expect(gw.disable).toHaveBeenNthCalledWith(1, { code: "123456" });
    expect(gw.disable).toHaveBeenNthCalledWith(2, { recoveryCode: "ABCD" });
  });
});
