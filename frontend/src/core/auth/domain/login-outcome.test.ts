// @vitest-environment node
import { describe, expect, it } from "vitest";
import { resolveLoginOutcome } from "./auth";

describe("resolveLoginOutcome", () => {
  it("treats valid=true as a session", () => {
    const user = { name: "a", valid: true };
    expect(resolveLoginOutcome(user)).toEqual({ kind: "session", user });
  });

  it("classifies an MFA challenge with its token", () => {
    expect(
      resolveLoginOutcome({ name: "", valid: false, mfaRequired: true, mfaToken: "t" }),
    ).toEqual({ kind: "challenge", mfaToken: "t" });
  });

  it("classifies enrolment required with its token", () => {
    expect(
      resolveLoginOutcome({ name: "", valid: false, mfaEnrollmentRequired: true, mfaToken: "e" }),
    ).toEqual({ kind: "enrollment", mfaToken: "e" });
  });

  it("rejects a challenge flag without a token", () => {
    expect(resolveLoginOutcome({ name: "", valid: false, mfaRequired: true })).toEqual({
      kind: "rejected",
    });
  });

  it("rejects a plain failed login", () => {
    expect(resolveLoginOutcome({ name: "", valid: false })).toEqual({ kind: "rejected" });
  });

  it("never yields a session when valid is false even if a token is present", () => {
    expect(resolveLoginOutcome({ name: "a", valid: false, token: "jwt" })).toEqual({
      kind: "rejected",
    });
  });
});
