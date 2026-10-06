# Claude Code Hooks

`.claude/settings.json` configures a small set of Claude Code lifecycle hooks
that make part of the mandatory workflow in
[CONSTITUTION.md](../../CONSTITUTION.md) and `.claude/rules/ai-agent-workflow.md`
enforced by the harness itself, instead of depending entirely on the agent
voluntarily reading and following those documents each session. See
CONSTITUTION.md for the full process these hooks assist; this file only
describes the hooks and why they exist — it does not duplicate the process.

## What's configured

| Hook | Event | Script | Purpose |
|------|-------|--------|---------|
| OpenSpec status | `SessionStart` | `.claude/hooks/session-start-openspec-status.sh` | Prints in-flight `openspec` change status at the start of every session so the agent has awareness of active work without relying on memory. Informational only — never blocks session start. |
| Block push to main | `PreToolUse` (matcher: `Bash`) | `.claude/hooks/block-push-to-main.sh` | Detects a `git push` whose resolved target branch is `main`/`master` and denies the tool call (exit 2) with a message pointing at the PR workflow. |

## Why the push guard exists

GitHub enforces protection on `main` via the active repository ruleset
`protect-main` (id `24128115`): pull-request-only merges, required status
checks (`CI`, `Frontend CI`, `Playwright E2E`, `Code Lint`, `PR Validation`),
and blocked force-push/deletion. Classic (legacy) branch protection is
intentionally unused — do not layer it on top of the ruleset. See
`docs/200-architecture/208-devsecops/README.md` and issue #1040; apply/assert
with `security/apply-protect-main-ruleset.sh` /
`security/assert-protect-main-ruleset.sh`.

CONSTITUTION.md already requires every change to land via Pull Request. This
Claude Code hook is **defense in depth** at the tool-call level so an agent
(or a human pasting a command) cannot accidentally `git push` straight to
`main` from inside a session. It is not a substitute for the GitHub ruleset.

The hook only looks at the resolved destination branch of a `git push`
invocation (explicit refspec, or the current branch when none is given). It
does not attempt to block `--force`/`--force-with-lease` in general, or
`git push` via alternate remote names beyond the common case — see
`openspec/changes/archive/2026-09-23-enforce-workflow-gates-via-hooks/design.md` (Riesgos /
Trade-offs) for the full list of known edge cases and why the scope was kept
small deliberately.

## What was considered and rejected

A `PreToolUse`/`UserPromptSubmit` hook that nudges when `backend-api/src` or
`frontend/src` files are edited with no matching `openspec/changes/*`
directory present was evaluated and **rejected** as net-negative: it fires on
trivial edits and on legitimate mid-implementation work whose OpenSpec change
directory name doesn't lexically match the touched path, producing noise that
trains the agent (and the human) to ignore hook output — which defeats the
purpose. The `SessionStart` status hook plus the existing
`scripts/validate-sdlc-plan.sh` and the pre-push git hook
(`scripts/preflight.sh`, installed via `scripts/install-git-hooks.sh`) already
cover this at a coarser, non-annoying granularity. Full rationale in
`openspec/changes/archive/2026-09-23-enforce-workflow-gates-via-hooks/proposal.md` (Out of Scope).

## How these were tested

Hooks are harness configuration, not application code — there is no
JUnit/Jest/Playwright target for them. They were verified manually:

- `SessionStart`: ran `.claude/hooks/session-start-openspec-status.sh` directly
  and confirmed it prints the current `openspec list` output (or a graceful
  fallback message if the CLI is missing), and exits 0 in both cases.
- Push guard: fed `.claude/hooks/block-push-to-main.sh` synthetic
  `PreToolUse` JSON payloads (the same shape Claude Code sends on stdin) via
  redirected-input test files covering: `git push origin main` (blocked, exit
  2), `git push origin <feature-branch>` (allowed, exit 0), a non-git command
  (allowed, exit 0), and a bare `git push` while checked out on a feature
  branch (allowed, exit 0 — resolves the current branch, which is not
  `main`/`master`).

Full test transcript and exact payloads are in the PR description for
issue #1027.

## Adding a new hook

Keep the set small. Before adding one, ask: does this block or annoy a normal,
compliant workflow more than it helps? If a hook would need to guess intent
from a shell command with a high false-positive rate, prefer documentation
(CLAUDE.md / `.claude/rules/`) or a CI/pre-push gate (`scripts/preflight.sh`)
instead — those are already the mechanically enforced parts of this process.
