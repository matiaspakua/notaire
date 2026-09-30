# Design — fixes from the autonomous gpt-oss runs

## Context

oMLX's `_is_tool_call_message` accepts an analysis-channel message addressed to
`functions.*` only when its content parses as a JSON object. `with_retries` runs
the worker, then the phase gate; the gate checks the artifacts on disk, whoever wrote them.

## Goals / Non-Goals

**Goals:** a truncated gpt-oss call reaches Codex repaired; a review note cannot be skipped.

**Non-Goals:** judging whether the worker applied the note correctly (Gate 4 does).

## Decisions

- **Close the string, not guess content**: when parsing fails and the text ends in
  `}` without a preceding quote, insert `"` before the brace and parse again.
- **Repair before the channel check**: the patch calls `repair_tool_call` inside
  `_is_tool_call_message`, the same function the extraction uses afterwards.
- **Fingerprint the worktree** (`HEAD` plus `git status --porcelain` plus
  `git diff`) before and after the worker run; equal fingerprints with a pending
  `review-<phase>.md` fail the attempt with a message that repeats the note.

## Riesgos / Trade-offs

- A note that is already satisfied by the files forces a no-op retry; the foreman
  deletes such a note instead.

## Testing Strategy

| Scenario (spec) | Test level | Test class / file |
|-----------------|------------|-------------------|
| Unterminated cmd string | unit | `local-ai/sdlc/tests/test_harmony_repair.py` |
| Analysis-channel check uses the repair | unit | `local-ai/sdlc/tests/test_patch_omlx.py` |
| Review note ignored | replay | `foreman.sh 1049 spec` with `review-spec.md` pending |

## Regression Strategy

Harness self-tests; `bash -n foreman.sh`; patcher on the live oMLX; the #1049 spec rerun.

## Playwright Strategy

n/a — no UI change.

## Deployment Strategy

Merged through the PR; rerun `PRESET=gpt-oss bash local-ai/setup-omlx-codex.sh`.

## Rollback Strategy

`patch_omlx.py --restore`; `git revert` of the merge.

## Migration Plan

None.

## Open Questions

None.
