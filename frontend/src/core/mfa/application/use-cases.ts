import type { MfaGateway } from "./ports";
import { toSecondFactor, totpCode, type SecondFactorMode } from "../domain/mfa";

export interface CodeEntry {
  mode: SecondFactorMode;
  value: string;
}

export function createMfaUseCases(gateway: MfaGateway) {
  return {
    completeLogin: async ({ mfaToken, mode, value }: CodeEntry & { mfaToken: string }) =>
      gateway.completeLogin({ mfaToken, ...toSecondFactor(mode, value) }),
    beginEnrollment: (bearerToken?: string) => gateway.beginEnrollment(bearerToken),
    confirmEnrollment: async (value: string, bearerToken?: string) =>
      gateway.confirmEnrollment(totpCode(value), bearerToken),
    disable: async ({ mode, value }: CodeEntry) => gateway.disable(toSecondFactor(mode, value)),
  };
}

export type MfaUseCases = ReturnType<typeof createMfaUseCases>;
