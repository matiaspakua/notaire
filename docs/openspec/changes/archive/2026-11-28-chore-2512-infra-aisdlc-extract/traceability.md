# Traceability

> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — P4 Traceability.
> This is the change's ledger. It is created during planning with the upstream
> links filled in, and completed as the change moves through the gates. Rows below
> Tasks stay `pending` until the corresponding step actually happens — never
> pre-fill them.

## Chain

```text
Issue → Specification → Tasks → Commits → PR → Merge → Release
```

| Link | Reference | Status |
|------|-----------|--------|
| Issue | #2512 | in-progress |
| Use Case | CU76 — Quality Assurance and Testing Infrastructure | exists |
| Specification | `docs/openspec/changes/chore-2512-infra-aisdlc-extract/` | passed |
| Branch | `chore/2512_infra_aisdlc_extract` | created |
| Rollback tag | `pre-extract-infra` | created |
| Tasks | `tasks.md` | in progress |
| Commits | pending | pending |
| Pull Request | pending | pending |
| CI run | pending | pending |
| Merge commit | pending | pending |
| Release / tag | pending | pending |
| Smoke test | pending | pending |

## Requirement coverage

| Scenario (Acceptance Criterion) | Test | Status |
|---------------------------------|------|--------|
| gitmodules points at the infrastructure product | `workspace.tests.test_infra_submodule` | pending |
| module verify still runs from the same path | `infra/verify.sh` + submodule test | pending |
| descriptor matches application network and containers | `workspace.tests.test_system_descriptor_seam` | pending |
| start-infra consumes SYSTEM_DESCRIPTOR | `workspace.tests.test_system_descriptor_seam` | pending |
| gitmodules points at the kit product | `workspace.tests.test_aisdlc_adoption` | pending |
| pin matches aisdlc-v1.1.0 | `workspace.tests.test_aisdlc_adoption` | pending |
| profile invocation path unchanged | `workspace.tests.test_aisdlc_adoption` | pending |

## Permanent documentation updated

| Document | Updated | Commit |
|----------|---------|--------|
| `README.md` | no | pending |
| `AGENTS.md` | no | pending |
| `docs/300-development/REPO-SPLIT-PLAN.md` | no | pending |
| `docs/200-architecture/207-monitoring/README.md` | no | pending |
| `docs/200-architecture/209-deployment/README.md` | no | pending |
| `docs/300-development/306-ai-sdlc-framework/**` | no | pending |
| GitHub Pages modules/architecture/ai-sdlc | no | pending |

## Gate log

| Gate | Condition | Passed | Evidence |
|------|-----------|--------|----------|
| 1 | Issue + Specification + Acceptance Criteria | yes | #2512, this folder |
| 2 | Failing tests written, test cases designed | pending | pending |
| 3 | Suite green, coverage held, docs updated | pending | pending |
| 4 | CI green, review approved, no conflicts | pending | pending |
| 5 | Deployed, smoke test passed, Issue closed | pending | pending |

## Exceptions

None.
