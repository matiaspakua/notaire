# Design — gpt-oss-20b preset for setup-omlx-codex.sh

## Context

`setup-omlx-codex.sh` installs oMLX, downloads one model, writes its oMLX
settings, a Codex model catalog and the `omlx` profile. Codex 0.159 offers a
local model only `exec_command` (string `cmd`); `shell_type: default`,
`features.unified_exec=false` and `apply_patch_tool_type: function` are ignored
or rejected, and oMLX drops freeform tools. oMLX 0.7.0 parses gpt-oss output
with `openai_harmony` in `omlx/adapter/harmony.py`.

## Goals / Non-Goals

**Goals:** a second model is one command away and reproducible; gpt-oss tool
calls reach Codex intact.

**Non-Goals:** changing the harness default model; a proxy between Codex and oMLX.

## Decisions

- **Preset table in the script**, not a config file: two presets, each a handful
  of variables. `PRESET` picks one; `MODEL_REPO`/`CONTEXT_WINDOW` still override.
- **Only the default preset writes oMLX's global defaults** (`is_default`,
  `integrations.codex_model`); per-model settings carry everything else.
- **Repair inside oMLX**, where the tool call is parsed, not in a proxy: one
  process fewer, and the repair sees the model's raw text. Codex rejects the
  malformed call and ends the turn, so repairing later is too late.
- **Repair only unambiguous shapes** of `exec_command`: an argv list (`bash -lc X`
  becomes `X`, otherwise shell-quoted join), a `command` key, a stray `]` before
  the closing brace, header tokens after the tool name. Anything else passes through.
- **Patcher with anchors**: `patch_omlx.py` replaces exact code snippets and
  copies `harmony_repair.py` next to the adapter. A missing anchor means upstream
  changed the code: skip and warn, never edit blindly. `.orig` backup kept once.
- **Instructions**: the gpt-oss preset appends `codex-local-instructions-gpt-oss.md`
  (one-string `cmd`, heredoc writes, no `apply_patch`) to the shared local prompt.

## Riesgos / Trade-offs

- An oMLX update overwrites the patch; rerunning the setup script reapplies it,
  and `patch_omlx.py --check` reports it.
- The repair could turn an intentionally odd call into a different one; it only
  touches shapes Codex would reject anyway.

## Testing Strategy

| Scenario (spec) | Test level | Test class / file |
|-----------------|------------|-------------------|
| Argv list as cmd; Stray bracket; Header tokens; Valid call unchanged | unit | `local-ai/sdlc/tests/test_harmony_repair.py` |
| Second run changes nothing; Missing anchor skipped | unit | `local-ai/sdlc/tests/test_patch_omlx.py` |
| gpt-oss preset keeps the Qwen profile | manual | `PRESET=gpt-oss` run; `omlx.config.toml` hash unchanged |

## Regression Strategy

Harness self-tests; `bash -n setup-omlx-codex.sh`; the coding smoke task
(slugify, 8 runs) with the gpt-oss profile; default-preset rerun is a no-op.

## Playwright Strategy

n/a — no UI change.

## Deployment Strategy

Merged through the PR; each machine reruns `PRESET=gpt-oss bash local-ai/setup-omlx-codex.sh`.

## Rollback Strategy

`patch_omlx.py --restore` puts back the `.orig` files; `git revert` of the merge.

## Migration Plan

None.

## Open Questions

None.
