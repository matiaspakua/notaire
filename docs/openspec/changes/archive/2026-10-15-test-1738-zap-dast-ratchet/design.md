> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — §6 Quality Gates.

## Context

Run 38249512129: High/Medium/Low 0; Informational Sec-Fetch 90005 only.

## Goals / Non-Goals

**Goals:** Blocking ZAP CI with a minimal allowlist; docs/tests updated; CHANGELOG-free.
**Non-Goals:** Authenticated DAST; product header changes for Sec-Fetch.

## Decisions

1. Allowlist 90005 only (browser Sec-Fetch headers are not meaningful for server-to-server API baseline).
2. fail_action true now that High/Medium/Low are already zero.

## Riesgos / Trade-offs

Future Medium+ findings will fail the weekly job until fixed or allowlisted under Sec by Design.

## Testing Strategy

Failing security unit tests first; then workflow + rules; run security test modules.

## Regression Strategy

Local SEC-DAST-01 baseline unchanged; catalog still names zap.

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
