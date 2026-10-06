> Governed by [CONSTITUTION.md](../../../CONSTITUTION.md) — §6 Quality Gates, §7 Testing Rules, §11 Release Rules.

## Context

Issue #1292, Use Case CU76, parent #1197 / ADR-024. Measured on `main`: `infra/`, `testing/` and `local-ai/` are already folders with a standalone guard; `scripts/` holds 67 files and 12 read three or more areas; 17 workflows are shared; security assets are scattered. `backend-api` and `frontend` are already single-purpose.

## Goals / Non-Goals

**Goals:** one manifest, one `MODULE.md` and one `verify.sh` per module; seams listed in `contracts/`; per-area guards inside their module; the same checks pass after each slice.
**Non-Goals:** creating repositories (Phase 2, ADR-024 gates 3-7), renaming `backend-api`/`frontend`, changing product behaviour.

## Decisions

1. Plain files, no tooling: a YAML manifest plus Markdown and shell, validated by one stdlib unittest guard. Rejected: a monorepo tool (Nx, Bazel) — new dependency for one maintainer.
2. `verify.sh` wraps the commands `scripts/preflight.sh` already runs per area; `preflight.sh` later calls the module scripts so there is one definition of "verified".
3. Slices land in dependency order: manifest, contracts, security, guard relocation, docs generators, Foreman integration. Each is one PR that keeps `preflight.sh` green.
4. Code nothing needs goes to `deprecated/` (Owner decision), never deleted.

## Riesgos / Trade-offs

- A moved guard or workflow path can silently drop a CI gate; every slice runs `preflight.sh` and the CI-invariant guard.

## Testing Strategy

| Scenario (spec) | Test level | Test class / file |
|-----------------|------------|-------------------|
| Manifest and folders agree | unit (stdlib) | `workspace/tests/test_modules_manifest.py` |
| Dependencies are acyclic | unit (stdlib) | `workspace/tests/test_modules_manifest.py` |
| verify.sh is runnable | unit (stdlib) | `workspace/tests/test_modules_manifest.py` |

- Coverage impact: none (no production code)

## Regression Strategy

- Full suite command: `bash scripts/preflight.sh`; `bash scripts/preflight.sh --full` at the end (Playwright E2E, Bruno, Docker smoke)

## Playwright Strategy

No UI change; the existing suite runs unchanged as the integrity proof after the last slice.

## Deployment Strategy

- Flyway migration required: no; configuration keys: none
- Smoke test after deploy (Gate 5): CD green on each merge commit

## Rollback Strategy

- Revert the slice PR; each slice is independent.
