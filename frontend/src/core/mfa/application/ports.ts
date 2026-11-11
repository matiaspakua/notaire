import type { LoginResult } from "@/core/auth/domain/auth";
import type { EnrollmentStart, SecondFactor } from "../domain/mfa";

export interface MfaGateway {
  completeLogin(input: { mfaToken: string } & SecondFactor): Promise<LoginResult>;
  beginEnrollment(bearerToken?: string): Promise<EnrollmentStart>;
  confirmEnrollment(code: string, bearerToken?: string): Promise<string[]>;
  disable(factor: SecondFactor): Promise<void>;
}
