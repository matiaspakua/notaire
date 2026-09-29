# Design — remove the foreman interventions found in #1063

## Context

`with_retries` runs the worker, then the gate, and re-runs the worker with the
gate output until the gate passes. `RECHECK=1` guards only the first worker run.
`run_worker` fails the run when `$IO/BLOCKED.md` exists; nothing deletes it.
`tasks_only_ticked` (in `gate_green` and `gate_docs`) rejects any `tasks.md`
change other than `[ ]` -> `[x]`. `gate_docs` lints touched Markdown with
`gates.docs_lint`; `gate_spec` does not lint. `record_ledger` and `record_pr`
write the `Commits` and `Pull Request` rows with `ledger.py row`.

## Goals / Non-Goals

**Goals:** none of the six #1063 interventions is needed again; each fix is
deterministic and done by the harness, not asked of the worker.

**Non-Goals:** reverts of earlier-phase content; new gates for other phases.

## Decisions

- **Repair `tasks.md`, do not reject it.** The rule is mechanical: the file is
  the base plus ticks. `restore-ticks` keeps every base line and sets `[x]` on
  the base items whose task ID NEW ticks. Anything else in NEW is dropped:
  added items, `[n/a]`/`[-]` marks, rewritten text. The harness logs the
  `ticks-only` diff in `gates.log` as `REPAIRED`, so Gate 4 still sees what the
  worker tried. The worker's commits stay; the repair is its own commit, and
  `squash_spec_churn` folds it into the other OpenSpec-only commits.
- **Ticks match by task ID** (`4.1`, `8.2`), the same key `ledger.py tick` uses.
  A base item without an ID keeps its base state.
- **Lint autofix before lint.** `markdownlint-cli2 --fix` fixes blank lines and
  list spacing, the worker's most frequent error. The harness fixes, lints, and
  sends only what is left (e.g. a missing fence language) to the worker. Touching
  a file means making it lint-clean, as `run_pipeline.sh` already demands.
- **Lint the change folder in the spec gate**, so errors go back to the phase
  that wrote them. The spec phase is `NO_COMMIT`; the harness commit after the
  gate carries the fixes. In the docs gate the harness commits the fixes as
  `style(docs): fix markdown lint`.
- **Ledger rows checked by `ledger.py rows`**, the same exact-once match as
  `row`, so the spec gate and the writer cannot disagree.
- **`Closes #N` checked by the harness.** `gate_green` already checks it; the
  pr phase repeats the check before starting the worker (the docs phase could
  have rewritten history), and the prompt only asks the worker to write the PR.
- **Stale block removed at the start of `run_worker`.** A block is a message
  from one run to the foreman; the run's copy is kept in `$STATE/io-<label>/`.

## Riesgos / Trade-offs

- `restore-ticks` discards a genuine improvement to `tasks.md`. After Gate 2
  the worker may not change the plan anyway; the discarded lines are logged.
- `--fix` edits files the worker did not write. Those edits are whitespace
  only, and the pipeline lints the same files.

## Testing Strategy

| Scenario (spec) | Test level | Test class / file |
|-----------------|------------|-------------------|
| Tick kept; Added item dropped; Not-applicable mark dropped | unit | `local-ai/sdlc/tests/test_ledger.py` |
| Ledger rows present; Ledger row missing | unit | `local-ai/sdlc/tests/test_ledger.py` |
| Adapter declares the lint fix command | unit | `local-ai/sdlc/tests/test_adapter.py` |

`foreman.sh` changes (stale block, `RECHECK`, pr pre-check, calls to the new
commands) are shell glue with no test runner; they are checked by `bash -n`
and by the next full harness run.

## Regression Strategy

`python3 -m unittest discover -s local-ai/sdlc/tests`, `bash -n foreman.sh`,
`restore-ticks` on the stored #1063 docs-phase `tasks.md`, then a full harness
run on the next backlog issue.

## Playwright Strategy

n/a — no UI change.

## Deployment Strategy

Merged through the PR. The next `foreman.sh` run uses it.

## Rollback Strategy

`git revert` of the merge commit.

## Migration Plan

None.

## Open Questions

None.
