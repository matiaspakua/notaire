# Traceability

> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — P4 Traceability.

## Chain

```
Issue → Specification → Tasks → Commits → PR → Merge → Release
```

| Link | Reference | Status |
|------|-----------|--------|
| Issue | #1550 | open |
| Use Case | CU76 — Quality Assurance and Testing Infrastructure | exists |
| Specification | `docs/openspec/changes/chore-1550-board-status-label-coherence/` | Gate 1 + apply |
| Branch | `cursor/board-status-label-coherence-b6e6` | created from updated `main` |
| Tasks | `tasks.md` | groups 1–8 ticked; 9–12 pending PR/merge |
| Commits | | pending |
| Pull Request | | pending |
| CI run | | pending |
| Merge commit | | pending |
| Release / tag | | pending |
| Smoke test | | pending |

## Requirement coverage

| Scenario (Acceptance Criterion) | Test | Status |
|---------------------------------|------|--------|
| CLOSED issue left In Progress fails | `test_closed_must_be_done_not_in_progress` | covered |
| CLOSED Done without WIP passes | `test_closed_done_without_wip_label_passes` | covered |
| CLOSED keeps `in-progress` fails | `test_closed_must_drop_in_progress_label` | covered |
| OPEN In Progress requires label | `test_open_in_progress_requires_label` | covered |
| OPEN In Progress with label passes | `test_open_in_progress_with_label_passes` | covered |
| `in-progress` without In Progress Status fails | `test_open_label_requires_in_progress_status` | covered |
| OPEN blocked must not be Done | `test_blocked_open_must_not_be_done` | covered |
| Done prefers CLOSED | `test_done_prefers_closed` | covered |
| Skip without auth / force skip | `test_skip_when_forced` | covered |
| Process Checks / preflight wiring | `WiringTest` | covered |
| Constitution / merge-gate docs name the gate | `DocGuardTest` | covered |

## Permanent documentation updated

| Document | Updated | Commit |
|----------|---------|--------|
| `CONSTITUTION.md` | yes | pending |
| `CONSTITUTION-AGENT-CARD.md` | yes | pending |
| `docs/github/README.md` | yes | pending |
| `docs/300-development/304-ai-sdlc-cloud/CI-MERGE-GATE.md` | yes | pending |
| `docs/300-development/304-ai-sdlc-cloud/README.md` | yes | pending |
| `docs/300-development/304-ai-sdlc-cloud/FLEET-ARCHITECTURE.md` | yes | pending |
| `.claude/agents/cloud-foreman.md` | yes | pending |
| `CHANGELOG.md` | yes | pending |

## Gate log

| Gate | Condition | Passed | Evidence |
|------|-----------|--------|----------|
| 1 | Issue + Specification + Acceptance Criteria | yes | OpenSpec artifacts + validate-sdlc-plan |
| 2 | Failing tests written, test cases designed | yes | `workspace/tests/test_board_status_label_coherence.py` |
| 3 | Suite green, coverage held, docs updated | yes | unittest coherence suite + docs |
| 4 | CI green, review approved, no conflicts | no | pending PR |
| 5 | Deployed, smoke test passed, Issue closed | no | pending |

## Related / Refs

- Sibling board audit (Project store): `internal/board-status-label-audit.md`
- Human paste: Project store `docs/board-status-label-sync-paste.md`
- Prior pattern: #1252 OpenSpec archive hygiene
- Owner trackers left alone (R5): #1445, #1438, #1226

## Planned Files

| Path | Role |
|------|------|
| `workspace/sdlc/check-board-status-label-coherence.py` | process gate |
| `workspace/tests/test_board_status_label_coherence.py` | Gate 2 tests |
| `.github/workflows/sdlc-process.yml` | wire checker |
| `workspace/sdlc/preflight.sh` | local mirror |
| `CONSTITUTION.md` / `CONSTITUTION-AGENT-CARD.md` | normative rules |
| `docs/300-development/304-ai-sdlc-cloud/*` | fleet ops |
| `docs/github/README.md` | board contract |

## Exceptions

| Item | Reason | Owner |
|------|--------|-------|
| `in-progress` label on #1550 | GraphQL 403 `addLabelsToLabelable` for integration token | sync via PR body / human |
| Live board WIP leftovers (#1483, #1490) | Agent cannot `project item-edit` (403); humans run paste | human paste |
