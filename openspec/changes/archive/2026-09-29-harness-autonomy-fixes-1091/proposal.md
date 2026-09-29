# Remove the foreman interventions found in #1063

> Governed by [CONSTITUTION.md](../../../CONSTITUTION.md). This proposal documents
> **only this change**; permanent documentation remains the single source of truth.

| Field | Value |
|-------|-------|
| GitHub Issue | #1091 |
| Use Case | CU76 — Quality Assurance and Testing Infrastructure |
| Branch | `chore/1091_harness_autonomy_fixes` |
| Gate 1 status | passed |

## Objetivo

The #1063 run needed seven foreman interventions between triage and merge. Six
trace to harness defects, not to the worker's model:

- A `BLOCKED.md` written in the pr phase stayed in `$IO` and stopped the later
  Gate 4 fix run after the fix worker had committed its work.
- `09-pr.md` asks the worker to find `Closes #N` with `git log --oneline`; the
  keyword is in the body, so the worker blocked twice.
- `RECHECK=1` skips only the first worker run; a failing gate still starts retries.
- The worker edits `tasks.md` beyond ticking boxes (`[n/a]`, new items) and
  could not undo it in three retries.
- The docs gate lints every touched Markdown file, so errors already on `main`
  or left by the spec phase land on the docs worker.
- The spec gate passed a `traceability.md` without the `Commits` and
  `Pull Request` rows that the harness writes later.

## What Changes

- `run_worker` removes a stale `$IO/BLOCKED.md` before each run.
- `phase_pr` checks `Closes #N` in full commit messages before the worker runs;
  `09-pr.md` no longer asks the worker to check it.
- `RECHECK=1` runs the gate once and never starts the worker.
- New `ledger.py restore-ticks BASE NEW`: prints BASE with the `[x]` ticks NEW
  holds for the same task IDs. `tasks_only_ticked` repairs `tasks.md` with it,
  commits the repair and logs the discarded lines in `gates.log`.
- New adapter key `gates.docs_lint_fix`. The harness runs it on the files it is
  about to lint (spec gate: the change folder; docs gate: every touched `.md`),
  then lints; the spec gate now lints the change folder.
- New `ledger.py rows TRACE LABEL...`: exits 1 naming each row not present
  exactly once. The spec gate requires `Commits` and `Pull Request`.

## Reglas de negocio

| Rule | Source | New / Changed / Made explicit |
|------|--------|-------------------------------|
| After Gate 2 the worker may only tick `tasks.md` boxes | AI-SDLC.md (ledger) | Made explicit (now repaired, not only rejected) |
| A gate-only re-check never runs the worker | AI-SDLC.md (`RECHECK`) | Changed |
| Mechanical Markdown fixes are the harness's job | AI-SDLC.md | New |

## Capabilities

### New Capabilities

None.

### Modified Capabilities

- `ai-sdlc-enforcement`: `tasks.md` repair, ledger row check, gate-only re-check.

## Impact Analysis

### Módulos afectados

| Module | Touched | What changes |
|--------|---------|--------------|
| `backend-api` | no | — |
| `frontend` | no | — |
| `notaire-shared` | no | — |
| `infra` / observability | no | — |
| CI/CD (`.github/workflows`) | no | — (the harness self-tests already run in `sdlc-process.yml`) |
| `local-ai/sdlc` harness | yes | see What Changes |
| `.aisdlc/project.yml` | yes | new key `gates.docs_lint_fix` |

### Surface area

- Entities: none
- Endpoints: none
- Database (Flyway `V{n}`): none
- Configuration / `.env`: none
- Dependencies: none (Python standard library)

### Architecture review

AUDIT §4.1 layers L4 (gates) and L5 (guards). No ADR: the product architecture does not change.

## Documentation Impact

| Permanent document | What must change |
|--------------------|------------------|
| `local-ai/sdlc/AI-SDLC.md` | `RECHECK`, `tasks.md` repair, lint autofix, spec gate ledger rows, stale block |
| `CHANGELOG.md` | n/a — not user visible |

## Out of Scope

Worker retries that revert content committed in earlier phases (needs its own
design), and splitting `foreman.sh`.
