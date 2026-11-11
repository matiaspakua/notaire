> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — §6 Quality Gates.

## Context

Login is password-only. Slice 1 added the TOTP core. ADR-029 gives a single tenant per notary, so the only authorization needed on the new endpoints is that the caller acts on their own account.

## Goals / Non-Goals

**Goals:** Enforce a second factor for ADMIN/NOTARY without locking existing administrators out; keep every existing deployment unchanged by default; count wrong codes against the existing lockout.
**Non-Goals:** UI, admin reset of other users, WebAuthn, key rotation.

## Decisions

- Policy property `notaire.mfa.policy` OFF|OPTIONAL|ENFORCE, default OPTIONAL: existing deployments behave as before until an operator enables ENFORCE; users who enrolled voluntarily are still challenged.
- Required roles `notaire.mfa.required-roles` default `ADMIN,NOTARY`, effective only under ENFORCE.
- ENFORCE with no `NOTAIRE_MFA_ENCRYPTION_KEY` fails at startup, so enforcement cannot lock administrators out of enrolling.
- Challenge and enrolment tokens are JWTs with a `scope` claim, TTL `notaire.mfa.challenge-ttl-ms` default 300000; `JwtTokenService.isValid` rejects any token with a scope, so they are never sessions. The enrolment token authenticates only on /api/v1/mfa/enrollment/**.
- The password step does not clear the failure counter for challenged users; only a completed second factor does, so a known password cannot reset the TOTP guess budget.
- Login response keeps valid=false for challenges so older clients treat them as a failed login.

## Riesgos / Trade-offs

Authentication is high risk. A bug could issue a session without the second factor or lock administrators out. Mitigations: default OPTIONAL, OFF as break-glass, startup guard on the key, tests for token scope separation and lockout.

## Testing Strategy

Unit: MfaLoginPolicy decisions, AccountRole, use cases, JwtTokenService scope, controller login/mfa branches. Web: MfaController standalone MockMvc. Integration: SpringBootTest full flow with real filter chain.

## Regression Strategy

Existing login tests keep their assertions; UserController gains constructor arguments. UserAuthorityResolver and StandardRoleModules switch to AccountRole with unchanged outputs.

## Playwright Strategy

n/a — no UI surface.

## Deployment Strategy

Ships with the next release.

## Rollback Strategy

Revert the PR.

## Migration Plan

None.

## Open Questions

None.
