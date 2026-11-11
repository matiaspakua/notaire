> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — §6 Quality Gates.

## Context

Slice 2 exposed the challenge and enrolment endpoints; no UI can complete them. ADR-030 requires the frontend to call the backend only through use cases and `adapters/http`.

## Goals / Non-Goals

**Goals:** Let an enrolled user sign in with TOTP or a recovery code; let any user enrol and disable; let an ENFORCE-policy user enrol without a session; keep the ESLint hexagon baseline at `{}`.
**Non-Goals:** Backend changes, QR image, admin reset, WebAuthn, rollout docs.

## Decisions

- Classification of the login response is a pure function in `core/auth` (`resolveLoginOutcome`), so the page has no protocol knowledge beyond rendering the outcome.
- A 6-digit numeric input is a TOTP `code`; anything else is a `recoveryCode`, chosen by an explicit toggle in the UI and validated in `core/mfa` before any request.
- The enrolment-scoped token lives in React state only and is passed per call as an optional Bearer to `apiPost`; it is never written to storage or the auth store. The BFF already forwards `Authorization`.
- No QR dependency: the panel shows the `otpauth://` URI as a link plus the Base32 secret. QR generation would add a library to scan and upgrade for a convenience every authenticator also provides as manual key entry; recorded as follow-up.
- `login/mfa` answers HTTP 200 with `valid=false` for a wrong code, so the use case returns the result and the page decides; 429 is an `ApiError`.

## Riesgos / Trade-offs

Authentication UI is high risk: a bug could store a session without the second factor, leak recovery codes or the enrolment token. Mitigations: session stored only on `valid=true` from `/login/mfa`, recovery codes held in component state and cleared on done, token never persisted, tests for each.

## Testing Strategy

Vitest: core use cases and classification (fake gateway), HTTP gateway (mocked api-client), login page challenge and enrolment steps, enrolment panel, security page, api-client Bearer option. Playwright: enrol then login with TOTP.

## Regression Strategy

Existing login-page and api-client tests keep their assertions; `LoginResult` only gains optional fields.

## Playwright Strategy

`TS-0046-mfa-totp-enrol-login.spec.ts`: sign in, enrol on `/dashboard/security` with a code computed from the displayed secret, sign out, sign in with the password and a fresh TOTP. Disables MFA at the end so the shared user stays password-only.

## Deployment Strategy

Ships with the next release; behaviour is inert until a user enrols or the operator sets `NOTAIRE_MFA_POLICY=ENFORCE`.

## Rollback Strategy

Revert the PR.

## Migration Plan

None.

## Open Questions

None.
