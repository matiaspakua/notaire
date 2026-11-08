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
   only a `SecretCipherPort`; the key is never committed (ADR-019). Startup refuses an absent or
   malformed key once MFA is enabled.
5. **Replay protection**: the last accepted time step is stored per user; a code for a step at or below it
   is rejected.
6. **Policy**: MFA is **required for `ROLE_ADMIN`**, **optional for every other role**; `ROLE_NOTARY`
   becomes required after SR-16 (#1638). The policy decision lives in the login flow (slice 2); slice 1
   only provides the capability.
7. **Hexagonal shape**: domain `domain.mfa` (RFC 6238 algorithm in plain Java), inbound ports
   `EnrollTotpUseCase`, `VerifyTotpUseCase`, `UseRecoveryCodeUseCase`, outbound ports
   `MfaEnrollmentStorePort`, `SecretCipherPort`, `RandomBytesPort`, `ClockPort`; the adapters live in
   `adapter.out.persistence.mfa` and `adapter.out.security`.

## Consequences

- New table `mfa_enrollments` (Flyway) and one new secret per installation.
- Losing the key loses every enrolment; operators back it up like the JWT secret. Key rotation is out of
  scope for slice 1 and recorded as follow-up.
- A lost device is recoverable by a recovery code or an admin reset (slice 2).
- Login behaviour is unchanged until slice 2 wires the policy.

## Alternatives considered

- **SMS / email OTP**: weaker (SIM swap, mailbox compromise) and needs an external provider.
- **WebAuthn first**: strongest, but heavier UX and browser/device matrix; deferred.
- **Third-party library (e.g. a TOTP jar)**: RFC 6238 is ~30 lines on `javax.crypto`; no new dependency
  to scan or upgrade.
- **Hashing the TOTP secret**: impossible, the server must recompute codes.
