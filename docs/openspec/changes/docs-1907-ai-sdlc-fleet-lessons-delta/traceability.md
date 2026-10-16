# Traceability

> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — P4 Traceability.
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
| Issue | #1907 (child of #1693; complements #1858; CU76) | open |
| Use Case | CU76 — Quality Assurance and Testing Infrastructure | exists |
| Specification | `docs/openspec/changes/docs-1907-ai-sdlc-fleet-lessons-delta/` | Gate 1 |
| Branch | `cursor/docs-ai-sdlc-lessons-delta-69d3` | created |
| Tasks | `tasks.md` | in progress |
| Commits | pending | pending |
| Pull Request | pending push | pending |
| CI run | <!-- workflow run URL --> | pending |
| Merge commit | <!-- SHA --> | pending |
| Release / tag | n/a — docs/process | pending |
| Smoke test | Process Checks + `check-heavy-ci.sh` | pending |

## Requirement coverage

| Scenario (Acceptance Criterion) | Test | Status |
|---------------------------------|------|--------|
| R2 path hygiene + paste templates + cost in lessons | `test_fleet_lessons_1907.py` | pending |
| Mac green ≠ CI green | `test_fleet_lessons_1907.py` | pending |
| Docker `-U` already on main (runbook only) | `test_fleet_lessons_1907.py` | pending |
| `validate-sdlc-plan.sh` retry hardened | `test_fleet_lessons_1907.py` | pending |
| Baseline #1858 lessons still hold | `test_fleet_lessons_1858.py` | pending |

## Permanent documentation updated

| Document | Updated | Commit |
|----------|---------|--------|
| `docs/300-development/304-ai-sdlc-cloud/FLEET-LESSONS-HEX-1693.md` | pending | |
| `docs/300-development/304-ai-sdlc-cloud/MAC-LOCAL-VS-CI.md` | pending | |
| `docs/300-development/304-ai-sdlc-cloud/CI-MERGE-GATE.md` | pending | |
| `docs/300-development/304-ai-sdlc-cloud/README.md` | pending | |
| `.claude/skills/steward/SKILL.md` | pending | |
| `.claude/agents/cloud-foreman.md` | pending | |
| `.claude/rules/hexagonal-architecture.md` | pending | |
| `.claude/rules/ai-agent-workflow.md` | pending | |
| `CHANGELOG.md` | pending | |

## Gate log

| Gate | Condition | Passed | Evidence |
|------|-----------|--------|----------|
| 1 | Issue + Specification + Acceptance Criteria | yes | #1907 + this change folder |
| 2 | Failing tests written, test cases designed | pending | |
| 3 | Suite green, coverage held, docs updated | pending | |
| 4 | CI green, review approved, no conflicts | pending | |
| 5 | Deployed, smoke test passed, Issue closed | pending | |

## Exceptions

Fleet App cannot add `in-progress` / area labels (HTTP 403) — human paste templates
in FLEET-LESSONS; do not invent Owner scope.
