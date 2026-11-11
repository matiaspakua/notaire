/**
 * MFA domain (#1640 / ADR-032): TOTP and recovery code handling.
 */
export interface EnrollmentStart {
  secret: string;
  otpauthUri: string;
}

export type SecondFactorMode = "totp" | "recovery";

export type SecondFactor = { code: string } | { recoveryCode: string };

export class InvalidCodeFormatError extends Error {
  constructor() {
    super("Invalid code format");
    this.name = "InvalidCodeFormatError";
  }
}

export function normalizeTotpCode(value: string): string {
  return value.replace(/\s+/g, "");
}

export function isTotpCode(value: string): boolean {
  return /^\d{6}$/.test(value);
}

export function normalizeRecoveryCode(value: string): string {
  return value.replace(/[\s-]+/g, "").toUpperCase();
}

export function totpCode(value: string): string {
  const code = normalizeTotpCode(value);
  if (!isTotpCode(code)) throw new InvalidCodeFormatError();
  return code;
}

export function toSecondFactor(mode: SecondFactorMode, value: string): SecondFactor {
  if (mode === "totp") return { code: totpCode(value) };
  const recoveryCode = normalizeRecoveryCode(value);
  if (!recoveryCode) throw new InvalidCodeFormatError();
  return { recoveryCode };
}
