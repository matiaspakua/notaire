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
| Issue | #2215 | open |
| Use Case | CU76 | exists |
| Specification | `docs/openspec/changes/feat-2215-foreman-stage-knobs-beyond-plan/` | passed |
| Branch | `feat/2215_foreman-stage-knobs-beyond-plan` | created |
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
| Notaire foreman plan maps at least four stages | kit Notaire assert / `foreman plan --json` | pending |
| Notaire stage knobs fail closed on unknown agent | StageProfilesTest (existing) | pending |
| Notaire omits green knobs without a green gate | plan JSON + FACTORY-ROADMAP note | pending |
| remaining Notaire stages stay unmapped honestly | foreman plan JSON unmapped_stages | pending |

## Permanent documentation updated

| Document | Action | Status |
|----------|--------|--------|
| FACTORY-ROADMAP.md | mapped ≥4; green omission; remaining unmapped honesty | pending |
| CHANGELOG.md | Unreleased entry | pending |

## Gate log

| Gate | Condition | Passed | Evidence |
|------|-----------|--------|----------|
| 1 | Issue + Specification + Acceptance Criteria | yes | #2215, this folder |
| 2 | Failing tests written, test cases designed | pending | pending |
| 3 | Suite green, coverage held, docs updated | pending | pending |
| 4 | CI green, review approved, no conflicts | pending | pending |
| 5 | Deployed, smoke test passed, Issue closed | pending | pending |

## Exceptions

None.
