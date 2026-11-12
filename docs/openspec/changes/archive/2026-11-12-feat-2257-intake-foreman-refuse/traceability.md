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
| Issue | #2257 | open (WIP label SKIPPED — issues:write 403) |
| Use Case | CU76 — Quality Assurance and Testing Infrastructure | exists |
| Specification | `docs/openspec/changes/archive/2026-11-12-feat-2257-intake-foreman-refuse/` | archived |
| Branch | `cursor/2257_intake-foreman-refuse-7ab7` | created |
| Tasks | `tasks.md` | Gate 1–8 mostly complete; preflight pending |
| Commits | `da631016`, `08e1fa83` | recorded |
| Pull Request | # | pending |
| CI run | | pending |
| Merge commit | | pending |
| Release / tag | | pending |
| Smoke test | | pending |

## Requirement coverage

| Scenario (Acceptance Criterion) | Test | Status |
|---------------------------------|------|--------|
| enforced claim refuses a failing issue | `test_intake.ForemanClaimTest` | pending |
| advisory claim reports fail without refusing | `test_intake.ForemanClaimTest` | pending |
| claim allows a passing issue | `test_intake.ForemanClaimTest` | pending |
| clean board reports clean true | `test_intake.IntakeBoardTest` | pending |
| dirty board reports clean false | `test_intake.IntakeBoardTest` | pending |
| no list command is no-evaluable | `test_intake.IntakeBoardTest` | pending |
| custom section names replace defaults | `test_intake.IntakeCliTest` (preserve) | pending |
| kit intake modules name no Notaire paths | `test_intake.KitIndependenceTest` | pending |

## Permanent documentation updated

| Document | Updated | Commit |
|----------|---------|--------|
| `framework/ai-sdlc/README.md` | pending | |
| `framework/ai-sdlc/CHANGELOG.md` | pending | |
| `docs/openspec/specs/issue-intake/spec.md` | on archive | |

## Gate log

| Gate | Condition | Passed | Evidence |
|------|-----------|--------|----------|
| 1 | Issue + Specification + Acceptance Criteria | pending | validate-sdlc-plan.sh |
| 2 | Failing tests written, test cases designed | pending | |
| 3 | Suite green, coverage held, docs updated | pending | |
| 4 | CI green, review approved, no conflicts | pending | |
| 5 | Deployed, smoke test passed, Issue closed | pending | |

## Exceptions

WIP label 403 SKIPPED (honest; see FLEET-IDENTITY.md). Kit-only change: Maven /
Bruno / Playwright groups recorded as n/a with reason in tasks.md.
