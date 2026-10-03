# Traceability

> Governed by [CONSTITUTION.md](../../../CONSTITUTION.md) — P4 Traceability.
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
| Issue | #1042 | open (implement in progress; in-progress label ACL 403) |
| Use Case | CU76 – Quality Assurance and Testing Infrastructure | updated |
| Related | audit-2026-09; after #1044 → #1051 → #1046 (`dbc15d30`) | referenced |
| Specification | `openspec/changes/fix-1042-cd-tested-sha/` | Gate 1 validated |
| Branch | `cursor/fix-1042-cd-tested-sha-69d3` | created from `origin/main` @ `dbc15d30` |
| Tasks | `tasks.md` | implement complete; merge pending coordinator |
| Commits | `28ef0f5d` test; `0dcd1541` fix; `38c187de` docs | pushed |
| Pull Request | https://github.com/matiaspakua/notaire/pull/1158 | draft |
| CI run | heavy gate pending on PR head | pending |
| Merge commit | — | pending |
| Release / tag | — | pending |
| Smoke test | — | pending |

## Requirement coverage

| Scenario (Acceptance Criterion) | Test | Status |
|---------------------------------|------|--------|
| `build-and-publish` checkout uses `workflow_run.head_sha` (with fallback) | `scripts/test_cd_pin_tested_sha.py` | green (TDD red→green) |
| Image tagged with that publish SHA | same — YAML assert on tags / raw SHA | green |
| `latest` moved only after SHA-tagged push succeeds | same — ordered steps / imagetools latest | green |
| CD skips publish when CI conclusion ≠ `success` | same — job `if` contains conclusion == success | green |
| DevSecOps docs describe pin-to-tested-SHA | docs review | updated |
| Static validator suite green | `python3 scripts/test_cd_pin_tested_sha.py` | green |
| Playwright product E2E | n/a — no UI surface; PR heavy CI Playwright job still required | pending CI |

## Permanent documentation updated

| Document | Updated | Commit |
|----------|---------|--------|
| `docs/200-architecture/208-devsecops/README.md` | yes | pending |
| `docs/100-business/102-use-cases/CU76 – Quality Assurance and Testing Infrastructure.md` | yes | pending |
| `CHANGELOG.md` | yes | pending |

## Gate log

| Gate | Condition | Passed | Evidence |
|------|-----------|--------|----------|
| 1 | Issue + Specification + Acceptance Criteria | yes | `bash scripts/validate-sdlc-plan.sh fix-1042-cd-tested-sha` |
| 2 | Failing tests written, test cases designed | yes | 4 FAIL on pre-change `cd.yml`; success-if OK |
| 3 | Suite green, coverage held, docs updated | yes (local) | pin tests OK; docs + CHANGELOG updated |
| 4 | CI green, review approved, no conflicts | pending | — |
| 5 | Deployed, smoke test passed, Issue closed | pending | — |

## Exceptions

- Issue `in-progress` label: GraphQL ACL 403 for integration token (tasks §1.6).
- Do **not** merge from this worker — coordinator merges after
  `bash scripts/check-heavy-ci.sh <pr>` exit 0.
