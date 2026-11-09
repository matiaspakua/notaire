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
| Issue | #2182 | open |
| Use Case | CU76 | exists |
| Specification | `docs/openspec/changes/feat-2182-stage-profile-knobs/` | passed |
| Branch | `cursor/feat-2182-stage-profile-knobs-51cd` | created |
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
| omitted stage knobs are no-op | StageProfilesTest | passing |
| parse agents skills mcp model effort | StageProfilesTest | passing |
| unknown catalog id fails validate | StageProfilesTest | passing |
| unknown id fails doctor | StageProfilesTest | passing |
| skills-dir overlay resolves skill id | StageProfilesTest | passing |
| missing catalogs dir with agent ref fails | StageProfilesTest | passing |
| stage list json never executes | StageProfilesTest | passing |
| config show redacted surfaces stages | StageProfilesTest | passing |
| unknown stage id fails load | StageProfilesTest | passing |

## Permanent documentation updated

| Document | Updated | Commit |
|----------|---------|--------|
| `framework/ai-sdlc/README.md` | pending | pending |
| `framework/ai-sdlc/templates/project/aisdlc.toml` | pending | pending |

## Gate log

| Gate | Condition | Passed | Evidence |
|------|-----------|--------|----------|
| 1 | Issue + Specification + Acceptance Criteria | yes | #2182, this folder |
| 2 | Failing tests written, test cases designed | pending | pending |
| 3 | Suite green, coverage held, docs updated | pending | pending |
| 4 | CI green, review approved, no conflicts | pending | pending |
| 5 | Deployed, smoke test passed, Issue closed | pending | pending |

## Exceptions

None.
