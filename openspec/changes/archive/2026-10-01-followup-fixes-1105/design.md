# Design — land the #1049 follow-up fixes

## Context

oMLX's harmony adapter returns no tool call when the completion cannot be parsed;
the Responses stream then completes with an empty message and Codex ends the turn.

## Goals / Non-Goals

**Goals:** no worker turn ends silently on a lost call; the staging step is spelled out.

**Non-Goals:** fixing the model's truncation itself.

## Decisions

- **Recovery call, not an exception**: `parse_tool_calls_from_tokens` runs inside
  the scheduler step shared by every request, so raising there could fail other
  requests. An `echo` call is harmless, runs through Codex's normal path and puts
  the instruction to resend in the model's context.
- **Detect only clear losses**: the text addressed `to=functions.` and there is no
  call and no final answer.
- **Rebuild from `.orig`**: every apply starts from the pristine file, so adding or
  changing a patch never stacks on an older patched version.

## Riesgos / Trade-offs

- A model that keeps losing the same call gets several echoes; the worker timeout and
  the phase retries still bound the run.

## Testing Strategy

| Scenario (spec) | Test level | Test class / file |
|-----------------|------------|-------------------|
| EOS right after the constraint token; Final answer untouched | unit + real parser | `test_harmony_repair.py`, `test_patch_omlx.py`; oMLX's `openai_harmony` |
| Unstaged edits after an amend | replay | #1049 implement |

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
