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
| Issue | #1858 (child of #1693; CU76) | open |
| Use Case | CU76 — Quality Assurance and Testing Infrastructure | exists |
| Specification | `docs/openspec/changes/docs-1858-ai-sdlc-hex-fleet-lessons/` | Gate 1 |
| Branch | `docs/1858_ai_sdlc_hex_fleet_lessons` | created |
| Tasks | `tasks.md` | in progress |
| Commits | `0013a842` Gate 1 + guard; `c394eb2e` encode lessons | done |
| Pull Request | pending push | pending |
| CI run | <!-- workflow run URL --> | pending |
| Merge commit | <!-- SHA --> | pending |
| Release / tag | n/a — docs/process | pending |
| Smoke test | Process Checks + `check-heavy-ci.sh` | pending |

## Requirement coverage

| Scenario (Acceptance Criterion) | Test | Status |
|---------------------------------|------|--------|
| Durable lessons doc exists and is indexed | `test_fleet_lessons_1858.py` | pending |
| Mac vs CI ownership doc exists | `test_fleet_lessons_1858.py` | pending |
| Steward covers CHANGELOG fence / Process Checks / sync-after-land | `test_steward_skill.py` | pending |
| `validate-sdlc-plan.sh` retries `gh` issue lookup | source assert in `test_fleet_lessons_1858.py` | pending |

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
| `AGENTS.md` | pending | |

## Gate log

| Gate | Condition | Passed | Evidence |
|------|-----------|--------|----------|
| 1 | Issue + Specification + Acceptance Criteria | yes | #1858 + this change folder |
| 2 | Failing tests written, test cases designed | pending | |
| 3 | Suite green, coverage held, docs updated | pending | |
| 4 | CI green, review approved, no conflicts | pending | |
| 5 | Deployed, smoke test passed, Issue closed | pending | |

## Exceptions

Fleet App cannot add `in-progress` label (HTTP 403) — leave human paste; do not invent Owner scope. Matches #1693 board-hygiene lesson.
