# Traceability

> Governed by [CONSTITUTION.md](../../../CONSTITUTION.md) — P4 Traceability.
> This is the change's ledger. It is created during planning with the upstream
> links filled in, and completed as the change moves through the gates. Rows below
> Tasks stay `pending` until the corresponding step actually happens — never
> pre-fill them.

## Chain

```
Issue → Specification → Tasks → Commits → PR → Merge → Release
```

| Link | Reference | Status |
|------|-----------|--------|
| Issue | #1045 | open → implement in progress |
| Use Case | CU78 – Security, Privacy and Compliance | exists (updated) |
| Related | #1046 (alerts, earlier); #1043 (frontend GHCR, merged `8ab8a6e5`); #1136 (npm Dependabot); ADR-017; audit-2026-09 | referenced |
| Specification | `openspec/changes/chore-1045-pin-images-dependabot/` | Gate 1 validated |
| Branch | `cursor/chore-1045-pin-images-dependabot-69d3` | created from `origin/main` @ `8ab8a6e5` |
| Tasks | `tasks.md` | implement in progress |
| Commits | — | pending |
| Pull Request | — | pending |
| CI run | — | pending |
| Merge commit | — | pending (coordinator after heavy CI) |
| Release / tag | — | pending |
| Smoke test | — | pending |

## Requirement coverage

| Scenario (Acceptance Criterion) | Test | Status |
|---------------------------------|------|--------|
| pgadmin is not on latest | `scripts/test_image_pins_and_dependabot.py` | covered (green) |
| infra stack has no latest tags | same | covered (green) |
| sonarqube channel tag is versioned | same | covered (green) |
| postgres images are minor-or-digest pinned | same | covered (green) |
| backend Dockerfile bases are pinned | same | covered (green) |
| frontend Dockerfile bases are pinned | same | covered (green) |
| CI postgres service images follow the same pin rule | same | covered (green) |
| npm ecosystem targets frontend | same | covered (green) |
| existing maven and github-actions ecosystems remain | same | covered (green) |
| docker ecosystem for backend-api | same | covered (green) |
| docker ecosystem for frontend | same | covered (green) |
| no duplicate docker directory entries | same | covered (green) |

Playwright product specs: **n/a — no UI surface** (compose/Dockerfile/Dependabot/docs only). Repository Playwright CI job must still pass.

## Permanent documentation updated

| Document | Updated | Commit |
|----------|---------|--------|
| `docs/200-architecture/202-ADR/ADR-017-container-base-images.md` | yes | pending |
| `docs/200-architecture/208-devsecops/README.md` | yes | pending |
| `infra/README.md` | yes | pending |
| `docs/100-business/102-use-cases/CU78 – Security and Compliance.md` | yes | pending |
| `CHANGELOG.md` | yes | pending |

## Gate log

| Gate | Condition | Passed | Evidence |
|------|-----------|--------|----------|
| 1 | Issue + Specification + Acceptance Criteria | yes | `openspec/changes/chore-1045-pin-images-dependabot/`; `validate-sdlc-plan.sh` OK |
| 2 | Failing tests written, test cases designed | yes | hygiene script failed 9 asserts on pre-change tree; green after pins |
| 3 | Suite green, coverage held, docs updated | pending | hygiene + CD/prod-compose guards green; docs updated |
| 4 | CI green, review approved, no conflicts | pending | — |
| 5 | Deployed, smoke test passed, Issue closed | pending | coordinator merges after `check-heavy-ci.sh` exit 0 |

## Exceptions

None. Branch name uses agent suffix `-69d3` per cloud task instructions (OpenSpec draft originally suggested `-9312`).
