> Governed by [CONSTITUTION.md](../../../CONSTITUTION.md) — §6 Quality Gates, §7 Testing Rules, §11 Release Rules.

## Context

Issue #921, Use Case CU76 – Quality Assurance and Testing Infrastructure; CU77 – Monitoreo de Operaciones y Gestión de Incidentes. No end-to-end documentation audit existed; link rot and stale statements were found only by accident.

## Goals / Non-Goals

**Goals:** A measured audit, the defects it finds fixed or filed, and a guard against link rot.
**Non-Goals:** Rewriting the 88 Use Cases or the SRS; license selection (Owner decision, filed separately).

## Decisions

1. Checks first, prose second: the report cites the commands that produced each figure, so it can be re-run.
2. The link guard resolves relative links only; external URLs are not fetched (flaky and slow in CI).
3. A missing LICENSE file is reported and filed, not invented: choosing a license is the Owner's decision.

## Riesgos / Trade-offs

- The inventory ages; the report is dated and names the commands to refresh it.

## Testing Strategy

| Scenario (spec) | Test level | Test class / file |
|-----------------|------------|-------------------|
| Relative links in active documentation resolve | static | `docs/tests/test_docs_links.py` |
| The audit report exists and links its evidence | static | `docs/tests/test_docs_links.py` |

- New unit tests (`src/test/java/.../unit/`): n/a
- New integration tests: n/a
- Coverage impact: none

## Regression Strategy

- Existing tests affected: none
- Full suite command: `bash scripts/preflight.sh`
- HTTP/Bruno API suite: unchanged
- Legacy paths at risk: none

## Playwright Strategy

No UI change; n/a.

## Deployment Strategy

- Flyway migration required: no
- Deployment order / coupling: single PR
- Configuration or `.env` keys: none
- Feature flag: no
- Smoke test after deploy (Gate 5): CI green on the merge commit

## Rollback Strategy

- Revert the PR.
