> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — §6 Quality Gates,
> §7 Testing Rules, §11 Release Rules.

## Context

The coach module already supports four harness readers (claude-code, codex, opencode, agy). Each is a generator function `reader(home: Path) -> Iterator[Session]`. The `report()` function iterates `HARNESSES`, calls the matching reader from `READERS`, and aggregates. Adding Junie follows the same pattern.

## Goals / Non-Goals

**Goals:**

- Add Junie CLI as a fifth harness reader in `coach.py`
- Read sessions from `~/.junie/sessions/index.jsonl`, events from per-session `events.jsonl`, and prompts from `~/.junie/history.jsonl`
- Never read `EnvironmentVariablesUpdatedEvent` (secrets)
- Never emit prompt text in the report
- Count tool calls once per unique `stepId`
- Filter by `projectDir` and `--days`

**Non-Goals:**

- Dashboard UI changes
- Changes to other harness readers

## Decisions

- **Junie reader follows the existing generator pattern.** Each reader function takes `home: Path` and yields `Session` objects. This keeps the `report()` function unchanged except for the new entry in `HARNESSES` and `READERS`.
- **Prompts come from `history.jsonl`, matched by project path and time window.** The session index provides `projectDir`, `sessionId`, and timestamps; `history.jsonl` has `projectPath`, `text`, and `timestamp`. Matching by project path and a time window around the session's `createdAt` links prompts to sessions.
- **Tool calls counted once per `stepId`.** `ToolBlockUpdatedEvent` and `TerminalBlockUpdatedEvent` share a `stepId`; counting unique step IDs avoids double-counting.
- **`EnvironmentVariablesUpdatedEvent` is explicitly skipped.** The `env` key contains secrets; reading it would violate the coach's privacy contract.

## Riesgos / Trade-offs

- [Junie store format may evolve] → The reader uses `_rows()` which already tolerates malformed JSON and missing keys gracefully.
- [No Junie store on CI machines] → The reader returns an empty iterator when the index file is missing, same as all other readers.

## Testing Strategy

TDD: write tests first in `test_coach.py`, observe them fail, then implement.

| Scenario (spec) | Test level | Test class / file |
|-----------------|------------|-------------------|
| Junie session counted in report | unit | `test_coach.py::CoachTest` |
| Junie prompts matched by project path | unit | `test_coach.py::CoachTest` |
| Junie tool calls counted per stepId | unit | `test_coach.py::CoachTest` |
| Junie model from modelUsage | unit | `test_coach.py::CoachTest` |
| EnvironmentVariablesUpdatedEvent never read | unit | `test_coach.py::CoachTest` |
| Prompt text never in JSON output | unit | `test_coach.py::CoachTest` |
| --days filters Junie sessions | unit | `test_coach.py::CoachTest` |
| Project filter excludes non-matching Junie sessions | unit | `test_coach.py::CoachTest` |

- New unit tests: `framework/ai-sdlc/tests/test_coach.py` (added test methods)
- Coverage impact: adds coverage for the new `junie()` function

## Regression Strategy

- Existing tests affected: `test_coach.py::CoachTest.test_the_report_counts_each_harness_of_this_project_only` and `test_a_planned_session_is_healthy` need updates to expect the Junie harness row.
- Full suite command: `cd framework/ai-sdlc/tests && python3 -m pytest -q`
- No HTTP/Bruno API suite impact.
- No legacy paths at risk.

## Playwright Strategy

n/a — no UI surface. This change is purely backend (Python kit).

## Deployment Strategy

- Flyway migration required: no
- Deployment order / coupling: none — additive change to Python module
- Configuration or `.env` keys: none
- Feature flag: no
- Smoke test after deploy (Gate 5): `aisdlc coach --json` includes a `"junie"` row

## Rollback Strategy

- Revert safe: yes — purely additive; removing the commit restores the previous state
- Database rollback: none needed
- Data written under the new behavior after revert: none
- Blast radius if rollback is delayed: none

## Migration Plan

Not applicable — no staged rollout needed.

## Open Questions

None.
