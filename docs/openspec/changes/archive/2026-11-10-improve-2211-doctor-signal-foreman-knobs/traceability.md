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
| Issue | #2211 | open |
| Use Case | CU76 | exists |
| Specification | `docs/openspec/changes/improve-2211-doctor-signal-foreman-knobs/` | passed |
| Branch | `improve/2211_doctor-signal-foreman-knobs` | created |
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
| doctor accepts signal local alias | DoctorSignalAliasTest | passing |
| unknown custom stage still warns | DoctorSignalAliasTest | passing |
| foreman plan lists unmapped stages | ForemanPlanTest | passing |
| foreman doctor note mentions unmapped | ForemanPlanTest | passing |
| lead-time --issue help says Issue not PR | MetricsLeadTimeHintTest | passing |
| doctor INFO mentions Issue not PR for lead-time | MetricsLeadTimeHintTest | passing |

## Permanent documentation updated

| Document | Action | Status |
|----------|--------|--------|
| FACTORY-ROADMAP.md | Document signal alias + Foreman unmapped honesty | pending |
| framework/ai-sdlc/README.md | Custom stage / lead-time hint | pending |
| framework/ai-sdlc/docs/METRICS.md | Issue vs PR | pending |
| CHANGELOG.md | Unreleased entry | pending |

## Gate log

| Gate | Condition | Passed | Evidence |
|------|-----------|--------|----------|
| 1 | Issue + Specification + Acceptance Criteria | yes | #2211, this folder |
| 2 | Failing tests written, test cases designed | yes | kit TDD red-then-green |
| 3 | Suite green, coverage held, docs updated | pending | kit verify.sh OK; preflight --full in progress |
| 4 | CI green, review approved, no conflicts | pending | pending |
| 5 | Deployed, smoke test passed, Issue closed | pending | pending |

## Exceptions

None.
