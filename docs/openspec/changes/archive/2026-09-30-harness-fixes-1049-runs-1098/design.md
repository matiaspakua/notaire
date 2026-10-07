# Design — harness fixes from the #1049 runs

## Context

`gate_spec` runs `openspec validate --strict`, `validate-sdlc-plan.sh`, the
harness's own tasks/traceability checks and markdown lint, and sends every
failure back to the worker. `md_fix` runs the adapter's `markdownlint --fix`,
which cannot fix MD040 or MD055. `run_worker` starts `codex exec`, whose shell
is `zsh -lc`.

## Goals / Non-Goals

**Goals:** no spec attempt is lost to a failure the harness can repair; the
worker's searches stay out of dependency trees.

**Non-Goals:** repairing content that needs judgement; the gpt-oss preset (#1099).

## Decisions

- **Repair, not reject** (as `repair_tasks`, #1091, and the triage restore, #1095):
  a leftover `specs/` under `skip_specs`, ticks outside groups 1-2 and the two
  lint rules have exactly one right answer, so the harness applies it and logs it.
- **Untick by group number**, not by comparing with the template: groups 1-2
  (prerequisites, branch) are the only ones true before implementation.
- **`md_repair.py` before `markdownlint --fix`**: fence state is tracked line by
  line, so only opening fences get a language; a closing fence stays bare (the
  worker was told so in every lint message and still broke it).
- **Crawl guard through `ZDOTDIR`**: Codex runs `zsh -lc`; a `ZDOTDIR` whose
  startup files source the user's own and then put `bin/shims` first on `PATH`
  survives `/etc/zprofile`'s `path_helper`, which a plain `PATH` override does not.

## Riesgos / Trade-offs

- A worker that ticks a group 3+ task on purpose in the spec phase loses the
  tick; nothing in groups 3+ can be true before Gate 2.
- The shims change `grep`/`find` behaviour only for recursive calls.

## Testing Strategy

| Scenario (spec) | Test level | Test class / file |
|-----------------|------------|-------------------|
| Premature tick removed; Prerequisite tick kept | unit | `local-ai/sdlc/tests/test_ledger.py` |
| Bare opening fence; Table row without trailing pipe | unit | `local-ai/sdlc/tests/test_md_repair.py` |
| Recursive grep skips node_modules | unit | `local-ai/sdlc/tests/test_crawl_guard.py` |
| Leftover specs removed | replay | `gate_spec` on the stored #1049 Qwen spec (`openspec validate --strict` passes after removal) |

## Regression Strategy

`python3 -m unittest discover -s local-ai/sdlc/tests`, `bash -n foreman.sh`,
`bash scripts/preflight.sh`.

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
