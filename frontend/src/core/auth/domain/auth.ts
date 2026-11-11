/**
 * Auth domain (#1843 / ADR-030).
 */
export interface LoginResult {
  idUser?: number;
  name: string;
  type?: string;
  valid?: boolean;
  status?: boolean;
  version?: number;
  person?: { idPerson?: number; firstName?: string; lastName?: string };
  token?: string;
  mfaRequired?: boolean;
  mfaEnrollmentRequired?: boolean;
  /** Short-lived scoped token of a challenge or enrolment response; never a session. */
  mfaToken?: string;
}

export interface LoginInput {
  name: string;
  password: string;
}

export type LoginOutcome =
  | { kind: "session"; user: LoginResult }
  | { kind: "challenge"; mfaToken: string }
  | { kind: "enrollment"; mfaToken: string }
  | { kind: "rejected" };

export function resolveLoginOutcome(result: LoginResult): LoginOutcome {
  if (result.valid) return { kind: "session", user: result };
  if (result.mfaToken && result.mfaEnrollmentRequired) return { kind: "enrollment", mfaToken: result.mfaToken };
  if (result.mfaToken && result.mfaRequired) return { kind: "challenge", mfaToken: result.mfaToken };
  return { kind: "rejected" };
}
