// @vitest-environment node
import { beforeEach, describe, expect, it, vi } from "vitest";

vi.mock("@/lib/api-client", () => ({ apiPost: vi.fn() }));

import { apiPost } from "@/lib/api-client";
import { mfaHttpGateway } from "./mfa-http-gateway";

describe("mfaHttpGateway", () => {
  beforeEach(() => vi.clearAllMocks());

  it("posts the challenge token with a code to /users/login/mfa", async () => {
    vi.mocked(apiPost).mockResolvedValue({ name: "a", valid: true });
    await mfaHttpGateway.completeLogin({ mfaToken: "t", code: "123456" });
    expect(apiPost).toHaveBeenCalledWith("/users/login/mfa", { mfaToken: "t", code: "123456" });
  });

  it("begins enrolment with the bearer token when given", async () => {
    vi.mocked(apiPost).mockResolvedValue({ secret: "S", otpauthUri: "otpauth://x" });
    await expect(mfaHttpGateway.beginEnrollment("tok")).resolves.toEqual({
      secret: "S",
      otpauthUri: "otpauth://x",
    });
    expect(apiPost).toHaveBeenCalledWith("/mfa/enrollment", {}, { bearerToken: "tok" });
  });

  it("begins enrolment on the session cookie without a bearer token", async () => {
    vi.mocked(apiPost).mockResolvedValue({ secret: "S", otpauthUri: "o" });
    await mfaHttpGateway.beginEnrollment();
    expect(apiPost).toHaveBeenCalledWith("/mfa/enrollment", {}, { bearerToken: undefined });
  });

  it("confirms and unwraps the recovery codes", async () => {
    vi.mocked(apiPost).mockResolvedValue({ recoveryCodes: ["A", "B"] });
    await expect(mfaHttpGateway.confirmEnrollment("123456", "tok")).resolves.toEqual(["A", "B"]);
    expect(apiPost).toHaveBeenCalledWith(
      "/mfa/enrollment/confirm",
      { code: "123456" },
      { bearerToken: "tok" },
    );
  });

  it("posts only the relevant field on disable", async () => {
    vi.mocked(apiPost).mockResolvedValue(undefined);
    await mfaHttpGateway.disable({ recoveryCode: "ABCD" });
    expect(apiPost).toHaveBeenCalledWith("/mfa/disable", { recoveryCode: "ABCD" });
  });
});
