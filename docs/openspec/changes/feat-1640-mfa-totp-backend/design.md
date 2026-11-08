> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — §6 Quality Gates.

## Context

The login flow authenticates by password only. ADR-032 chooses TOTP (RFC 6238) with encrypted secrets and recovery codes.

## Goals / Non-Goals

**Goals:** - A tested, hexagonal TOTP core with no Spring or DB in unit tests.
- Secrets encrypted at rest; recovery codes hashed.
**Non-Goals:** - Wiring into login, REST endpoints, UI (later slices).

## Decisions

- RFC 6238 implemented in the domain on `javax.crypto` (no new dependency).
- AES-256-GCM via `SecretCipherPort`; recovery codes reuse `PasswordHasherPort`.
- Replay protection by persisting the last accepted time step.

## Riesgos / Trade-offs

- Losing `NOTAIRE_MFA_ENCRYPTION_KEY` loses all enrolments: documented, backup like the JWT secret.
- Clock skew: one step tolerated.

## Testing Strategy

Unit tests per use case and the algorithm against RFC 6238 vectors; cipher round-trip; pg-integration test for the V54 migration and the persistence adapter.

## Regression Strategy

ArchUnit hexagon and slice-cycle tests; login tests untouched because the login flow is not modified.

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
