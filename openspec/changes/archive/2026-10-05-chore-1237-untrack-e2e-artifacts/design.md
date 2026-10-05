> Governed by [CONSTITUTION.md](../../../CONSTITUTION.md) — §6 Quality Gates, §7 Testing Rules, §11 Release Rules.

## Context

Issue #1237, Use Case CU76 – Quality Assurance and Testing Infrastructure. The files entered through git add -A in #1212 and #1216 and show up as modified after every local Playwright run.

## Goals / Non-Goals

**Goals:** A clean index for the E2E suite and a guard so it does not recur.
**Non-Goals:** Rewriting git history to purge the old blobs (needs the Owner).

## Decisions

1. Untrack without history rewrite: it fixes every future clone's working tree and diff noise without invalidating anyone's checkout. Rejected: filter-repo, which rewrites main.

## Riesgos / Trade-offs

- Branches that still track the files will conflict on merge with this change; they are resolved by accepting the deletion.

## Testing Strategy

| Scenario (spec) | Test level | Test class / file |
|-----------------|------------|-------------------|
| No tracked path under an ignored E2E artifact directory | static | `scripts/test_testing_standalone.py` |

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
