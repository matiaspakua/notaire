> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — §6 Quality Gates.

## Context

Issue #1680 reproduced locally: without SCM the gate fails with 70 new violations on untouched files. #1535 listed backend vulns/bugs; blockers were @Transactional self-invocation and related production defects.

## Goals / Non-Goals

**Goals:** FE QG green with SCM; clear true #1535 blockers without hexagon thrash; Process Checks via OpenSpec.
**Non-Goals:** No CHANGELOG; no domain.document / DocumentType hexagon; no jpa equals rewrite.

## Decisions

1. Mount `$APP_DIR` into the scanner and set projectBaseDir to frontend.
2. Force `-Dsonar.projectKey=$BACKEND_KEY` on Maven sonar.
3. Fix blockers surgically; hand jpa S4973 to hexagonal epic.

## Riesgos / Trade-offs

Larger Docker volume for FE scanner. NOSONAR on scoped CSRF/MD5 must stay justified.

## Testing Strategy

Infra unit guards for SCM/projectKey; targeted Maven unit tests; Vitest session-expiry/api-client; local FE Sonar re-measure (new_violations=0).

## Regression Strategy

Re-run run-sonar.sh after merge; security/run_gates.py local profile.

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
