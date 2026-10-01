# Design — OpenCode worker backend

## Context

`run_worker` builds a `codex exec` command inline, with git-config env overrides
that point `core.hooksPath` at the harness hooks, and a `ZDOTDIR` that puts the
crawl-guard shims first on the login shell's `PATH`. OpenCode's bash tool runs
`$SHELL -c` (not a login shell) and inherits the process environment.

## Goals / Non-Goals

**Goals:** either agent runs every phase with the same guards and outputs.

**Non-Goals:** changing prompts per agent; switching the default.

## Decisions

- **`bin/worker.py` owns the agent differences** (command, environment, final
  message) as pure functions, so they are unit-tested; `run_worker` stays one call.
- **Isolated OpenCode config**: `OPENCODE_CONFIG_DIR=local-ai/opencode`,
  `OPENCODE_DISABLE_PROJECT_CONFIG=1` (the repo's `opencode.json` loads every skill),
  `OPENCODE_DISABLE_CLAUDE_CODE=1`, `OPENCODE_DISABLE_EXTERNAL_SKILLS=1`, and
  `XDG_CONFIG_HOME` pointed at an empty directory so the user's global MCP servers
  do not start; `GH_CONFIG_DIR` keeps `gh` authenticated.
- **stdin from /dev/null**: `opencode run` waits on a non-TTY stdin, as Codex did.
- **Shims on `PATH` directly** for OpenCode (non-login shell: no `path_helper`
  reorders it); Codex keeps `ZDOTDIR`.
- **`--format json`**: the final message is the last `text` part, written to
  `last-<label>.md` like Codex's `-o`.

## Riesgos / Trade-offs

- A new OpenCode release may rename an env flag; `worker.py` has one place to change.

## Testing Strategy

| Scenario (spec) | Test level | Test class / file |
|-----------------|------------|-------------------|
| OpenCode command; Codex command unchanged; Final message extracted; Guards kept | unit | `local-ai/sdlc/tests/test_worker.py` |

## Regression Strategy

Harness self-tests; `bash -n foreman.sh`; a foreman run of a real issue with each agent.

## Playwright Strategy

n/a — no UI change.

## Deployment Strategy

Merged through the PR; `AGENT=opencode` selects it per run.

## Rollback Strategy

`AGENT=codex` (the default); `git revert` of the merge.

## Migration Plan

None.

## Open Questions

None.
