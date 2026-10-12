> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — §6 Quality Gates,
> §7 Testing Rules, §11 Release Rules.

## Context

Board audits show CLOSED issues left on In Progress and stale `in-progress`
labels on Done items. Project Status options are Todo / In Progress / Done only
(no Blocked column). Agents often cannot write Issues/Projects (403), so the
harness must detect drift and Constitution/AI SDLC docs must state the contract.
Sibling audit: Project store `internal/board-status-label-audit.md`; human paste
`docs/board-status-label-sync-paste.md`.

## Goals / Non-Goals

**Goals:**

- Normative coherence rules in Constitution + AI SDLC + board README.
- Mechanical checker with fixture tests; Process Checks / preflight invoke
  `--scope pr`; `--scope wip` / `--scope all` for hygiene.
- Clear skip when `gh` is unauthenticated / token absent (never silent pass).

**Non-Goals:**

- Fixing live board drift from the agent token (403) — humans run the paste.
- Owner product decisions (#1445 / #1438 / #1226).
- Product API / UI changes.

## Decisions

1. **`--scope pr` in CI / preflight (blocking when drift found).** Full-board
   `--scope all` would fail every PR on known CLOSED leftovers until humans
   paste fixes. PR-linked issues are the merge-relevant scope. Foreman hygiene
   runs `--scope wip` / `--scope all` separately.

2. **Skip, do not fail, when `gh` auth / project lookup is unavailable.** Matches
   `validate-sdlc-plan.sh` live Issue check and archive-hygiene inventory skip.
   `SKIP_BOARD_COHERENCE=1` forces skip.

3. **Blocked = label only.** Until a Blocked Status option exists, enforce
   `blocked` label coherence (not Done while OPEN; prefer not combining with
   `in-progress`).

4. **SURFACE = none.** Tests under `workspace/tests/` (Python). Playwright n/a.

## Riesgos / Trade-offs

- [Global WIP leftovers block merges if scoped wrong] → Default CI scope is `pr`.
- [False skip without auth] → Message is explicit; CI supplies `GH_TOKEN`.
- [OPEN + Done on unfinished AC] → Gate prefers CLOSED when Done; humans reopen
  Status when AC is not met (R4 / audit taxonomy).

## Testing Strategy

| Scenario (spec) | Test level | Test class / file |
|-----------------|------------|-------------------|
| CLOSED + In Progress fails | unit | `workspace/tests/test_board_status_label_coherence.py` |
| CLOSED + Done without WIP passes | unit | same |
| OPEN In Progress ⇔ `in-progress` | unit | same |
| OPEN blocked must not be Done | unit | same |
| Skip without gh / force skip | unit | same |
| Process Checks / preflight wiring | unit | same (path guards) |

- New unit tests: board coherence rules + wiring
- Integration / Playwright: n/a — no product surface
- Coverage (JaCoCo): unchanged

## Regression Strategy

Re-run `python3 -m unittest discover -s workspace/tests` (Process Checks already
discover this package). No Java suite impact.

## Playwright Strategy

n/a — no UI surface.

## Deployment Strategy

Docs + CI workflow merge only. No deploy artifact. Agents pick up the gate on
next Process Checks run.

## Rollback Strategy

Revert the PR; remove the Process Checks step / preflight line; Constitution
text can stay as advisory until a follow-up.
