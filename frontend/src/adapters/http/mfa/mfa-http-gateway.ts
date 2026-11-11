/**
 * HTTP adapter for MFA (#1640 / ADR-032).
 */
import { apiPost } from "@/lib/api-client";
import type { LoginResult } from "@/core/auth/domain/auth";
import type { MfaGateway } from "@/core/mfa/application/ports";
import type { EnrollmentStart } from "@/core/mfa/domain/mfa";

export const mfaHttpGateway: MfaGateway = {
  completeLogin: (input) => apiPost<LoginResult>("/users/login/mfa", input),
  beginEnrollment: (bearerToken) =>
    apiPost<EnrollmentStart>("/mfa/enrollment", {}, { bearerToken }),
  confirmEnrollment: async (code, bearerToken) => {
    const response = await apiPost<{ recoveryCodes: string[] }>(
      "/mfa/enrollment/confirm",
      { code },
      { bearerToken },
    );
    return response.recoveryCodes;
  },
  disable: (factor) => apiPost("/mfa/disable", factor),
};
