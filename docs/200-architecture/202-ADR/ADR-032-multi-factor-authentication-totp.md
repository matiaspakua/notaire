# ADR-032: Multi-factor authentication with TOTP (RFC 6238)

**Status:** Accepted — 2026-11-08
**Date:** 2026-11-08
**Deciders:** Engineering (mechanism recommended by issue #1640)
**Related:** ADR-019 (secrets), ADR-029 (roles), ADR-030 (hexagonal), CU-78, RNF-12, OWASP A07,
[OpenSpec change](../../openspec/changes/feat-1640-mfa-totp-backend/proposal.md)

## Context

Login is password-only (OWASP A07 lists "No MFA (not claimed)"). Notarial records are legally sensitive,
so administrators and notaries need a second factor. The installation is single-tenant and often offline
from third-party services, so the factor must work without an external provider.

## Decision

1. **Mechanism: TOTP, RFC 6238** (HMAC-SHA1, 6 digits, 30 s step, one step of clock skew accepted).
   Authenticator apps are universal and need no vendor. WebAuthn/passkeys are a later, separate ADR.
2. **Enrolment is two-step**: begin (server generates a 160-bit secret and returns the `otpauth://` URI)
   then confirm with a valid code. An unconfirmed enrolment never gates a login.
3. **Recovery codes**: 10 single-use codes of 10 Crockford-Base32 characters, shown once at confirmation,
   stored only as BCrypt hashes (the existing `PasswordHasherPort`). Using one consumes it.
4. **Secrets encrypted at rest**: the TOTP secret is stored as AES-256-GCM ciphertext (random 96-bit IV
   prepended, one key from `NOTAIRE_MFA_ENCRYPTION_KEY`, Base64 of 32 bytes). The application layer sees
   only the persistence adapter encrypts, through `SecretCipherPort`; the key is never committed (ADR-019).
   A malformed key fails startup; an absent key fails on first use, so installations without MFA are unaffected.
5. **Replay protection**: the last accepted time step is stored per user; a code for a step at or below it
   is rejected.
6. **Policy**: MFA is **required for `ROLE_ADMIN`**, **optional for every other role**; `ROLE_NOTARY`
   becomes required after SR-16 (#1638). The policy decision lives in the login flow (slice 2); slice 1
   only provides the capability.
7. **Hexagonal shape**: domain `domain.mfa` (RFC 6238 algorithm in plain Java), inbound ports
   `EnrollTotpUseCase`, `VerifyTotpUseCase`, `UseRecoveryCodeUseCase`, outbound ports
   `MfaEnrollmentStorePort`, `SecretCipherPort`, `RandomBytesPort`, `ClockPort`; the adapters live in
   `adapter.out.persistence.mfa` and `adapter.out.security`.

## Recorded decisions — slice 2, login policy (#1640)

8. **Policy switch**: `notaire.mfa.policy` = `OFF` | `OPTIONAL` | `ENFORCE`, env `NOTAIRE_MFA_POLICY`,
   **default `OPTIONAL`**. Rationale: no existing deployment changes behaviour on upgrade (an administrator with no
   enrolment still gets a session), while anyone who enrols voluntarily is protected. `ENFORCE` is an operator
   decision taken after administrators have enrolled; `OFF` is the break-glass switch. This refines decision 6:
   "required" is realised by `ENFORCE`, not by default.
9. **Required roles under `ENFORCE`**: `notaire.mfa.required-roles`, default `ADMIN,NOTARY` (`ROLE_NOTARY` exists
   since #1638, so the "after SR-16" condition of decision 6 is met).
10. **No lock-out by enforcement**: under `ENFORCE` an ADMIN/NOTARY without a confirmed enrolment gets no session but a
    5-minute `mfa_enrollment` token that authenticates only `/api/v1/mfa/enrollment/**`; after confirming they log in
    again with the code. `ENFORCE` without `NOTAIRE_MFA_ENCRYPTION_KEY` fails at startup, since enrolment would be
    impossible.
11. **Challenge**: after a valid password a user with a confirmed enrolment gets `valid:false, mfaRequired:true` and a
    5-minute `mfa_challenge` JWT (`scope` claim). `JwtTokenService.isValid` rejects any token with a scope, so a
    scoped token can never act as a session. `POST /api/v1/users/login/mfa` takes the token and a TOTP or recovery
    code and only then issues the session.
12. **Lockout**: wrong second-factor codes (login, enrolment confirm, disable) call `LoginAttemptService.onLoginFailed`
    under the same username key; the password step does not clear the counter for challenged users, so a known
    password cannot reset the TOTP guess budget. Failures are logged and counted without code values.
13. **Endpoints** (`/api/v1/mfa`): `POST /enrollment`, `/enrollment/confirm`, `/disable`; the caller acts only on their
    own account (ADR-029). Disabling is allowed; under `ENFORCE` the next login then asks for enrolment again.

## Consequences

- New table `mfa_enrollments` (Flyway) and one new secret per installation.
- Losing the key loses every enrolment; operators back it up like the JWT secret. Key rotation is out of
  scope for slice 1 and recorded as follow-up.
- A lost device is recoverable by a recovery code; an admin reset of another user's enrolment is a follow-up.
- Login behaviour is unchanged unless a user enrols or the operator sets `NOTAIRE_MFA_POLICY=ENFORCE`.

## Alternatives considered

- **SMS / email OTP**: weaker (SIM swap, mailbox compromise) and needs an external provider.
- **WebAuthn first**: strongest, but heavier UX and browser/device matrix; deferred.
- **Third-party library (e.g. a TOTP jar)**: RFC 6238 is ~30 lines on `javax.crypto`; no new dependency
  to scan or upgrade.
- **Hashing the TOTP secret**: impossible, the server must recompute codes.
