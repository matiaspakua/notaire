> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — §6, §7, §11.

## Context

See [proposal.md](proposal.md). The existing guards already encode both rules; they were the red tests.

## Goals / Non-Goals

**Goals:** green `infra/verify.sh`, identical service behaviour. **Non-Goals:** redesign of report publishing.

## Decisions

1. **Inline compose `configs`** for the three-line nginx server block. Rejected: weakening the single-nginx.conf guard; moving the file elsewhere (still a second copy).
2. **Overridable mount with the exemption marker.** Reports are produced at the repo root by the security runner, so a path outside `infra/` is inherent; the marker plus a variable is the repo's documented way (see `common.sh`). Rejected: relaxing the guard.

## Riesgos / Trade-offs

- [compose `configs.content` needs a recent Docker Compose] -> verified live on the local Docker 29.

## Testing Strategy

| Scenario (spec) | Test level | Test |
|-----------------|------------|------|
| Infra standalone guards pass | configuration | `infra/tests/test_infra_standalone.py` (failed on `main`, passes now) |
| The service still serves reports | live | index and `/reports/` returned 200 |

- New Java tests: none. Coverage impact: none.

## Regression Strategy

- `bash infra/verify.sh`.

## Playwright Strategy

n/a - no UI change.

## Deployment Strategy

- Flyway: no. `.env`: optional `SECURITY_REPORTS_DIR`. Recreate the `security-reports` container.
- Smoke (Gate 5): `curl localhost:8890/reports/` returns 200.

## Rollback Strategy

- Revert safe: yes.

## Migration Plan

1. Reproduce red guards on `main`. 2. Inline the config, add the variable. 3. Verify live.
