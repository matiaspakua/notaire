> Governed by [CONSTITUTION.md](../../../CONSTITUTION.md) — §6 Quality Gates,
> §7 Testing Rules, §11 Release Rules.

## Context

`.claude/settings.json` currently only configures `permissions`, `enabledPlugins`
and `tui` — no `hooks` key. Claude Code supports lifecycle hooks (`SessionStart`,
`UserPromptSubmit`, `PreToolUse`, `PostToolUse`, `Stop`, etc.) configured as shell
commands matched by tool name / regex, run by the harness itself (not by the
agent), so they cannot be skipped by an agent choosing not to follow CLAUDE.md.
Today the only two enforcement points that exist are `scripts/preflight.sh`
(manual, pre-push git hook) and human/CI review — nothing fires inside the
session itself.

## Goals / Non-Goals

**Goals:**
- Give the agent automatic, low-friction awareness of in-flight OpenSpec changes
  at the start of every session.
- Make direct `git push` to `main`/`master` fail fast, locally, before it reaches
  GitHub (defense in depth — GitHub branch protection is currently *not*
  configured on `main`, so this is the only guard today).
- Keep the hook set small and non-annoying: no hook should block a normal,
  compliant workflow.

**Non-Goals:**
- Mechanically enforcing TDD, Use Case linkage, or coverage floors from a hook —
  these require semantic judgment (or already have dedicated tooling in
  `scripts/`) and are not reliable to gate at the Bash-command-pattern level.
  Attempting to would produce false positives that make the agent/human start
  ignoring hook output.
- Replacing `scripts/preflight.sh` / `scripts/validate-sdlc-plan.sh` — hooks
  complement these, they do not replace them.
- Changing GitHub-side branch protection — out of scope for an agent-harness
  change (see proposal.md — Out of Scope).

## Decisions

1. **SessionStart hook shells out to `openspec list`, not a custom script.**
   Alternative considered: write a Node/Python summarizer that reads
   `openspec/changes/*` directly. Rejected — `openspec list` already exists,
   is the project's canonical status view, and stays correct as the schema
   evolves; a hand-rolled parser would drift from it. The hook wraps the call
   with `|| echo` fallback so a missing/broken `openspec` CLI never blocks
   session start (SessionStart hooks are informational, not gating).

2. **git-push-to-main guard implemented as a `PreToolUse` matcher on `Bash`,
   inspecting the command string, not a `PreToolUse` matcher on a hypothetical
   `Git` tool.** Claude Code does not expose a distinct git tool — all git
   commands arrive as `Bash` commands (confirmed: this repo's `rtk` wrapper
   itself intercepts at the `Bash` level). The hook parses the command for
   `git push` and inspects the explicit remote ref argument if present,
   otherwise resolves the current branch via `git rev-parse --abbrev-ref HEAD`
   in the hook's own shell invocation (same cwd the tool call would use).
   Blocks only when the resolved target branch is exactly `main` or `master`.

3. **Block by exit code 2 with a stderr message, not by rewriting the command.**
   Claude Code `PreToolUse` hooks can deny a tool call by exiting non-zero
   (or returning `"permissionDecision": "deny"` in structured JSON output) and
   printing a reason; the agent sees the denial and can course-correct
   (open a PR instead). Silently rewriting the push target was considered and
   rejected — surprising the agent/human with a different outcome than what
   was requested is worse than a clear, explained block.

4. **Rejected the OpenSpec-nudge hook on source edits** — see proposal.md,
   Out of Scope, for the full rationale (false-positive rate too high relative
   to value; SessionStart awareness + existing preflight/validate scripts
   already cover this at a coarser granularity without the noise).

## Riesgos / Trade-offs

- [Risk] SessionStart hook adds a subprocess call (`openspec list`) to every
  session start, adding latency. → Mitigation: `openspec list` is fast (reads
  local `openspec/changes/` directory only, no network); a timeout guard
  (`timeout 5s`) plus `|| true` fallback bounds the worst case.
- [Risk] The push-guard hook's branch-resolution logic (parsing `git push`
  arguments) could have edge cases it doesn't catch (e.g. `git push
  origin HEAD:main`, `git push --all`, pushing via a different remote name
  than `origin`). → Mitigation: this is explicitly defense in depth, not the
  only guard — CODEOWNERS + (recommended, tracked as a follow-up, not blocking
  this change) GitHub branch protection remain the authoritative guard. The
  hook covers the common case (`git push origin main`, `git push` on a
  checked-out `main`) which is what an agent is realistically going to type.
- [Risk] A hook with a bug could block legitimate work entirely (hooks run
  every matching tool call). → Mitigation: kept intentionally minimal (2 hooks),
  manually tested before merge (see Testing Strategy), and scoped so a failure
  mode is "denies a push to main" (recoverable, and the correct outcome anyway)
  rather than "denies arbitrary Bash calls."

## Testing Strategy

Hooks are harness configuration, not application code — there is no JUnit/Jest
target for them. Constitution's TDD mandate is honored in spirit through manual,
observed verification instead, documented in the PR per this schema's `apply`
operation guidance ("Report gate failures with their output. Never describe a
gate as passed without having run it.").

| Scenario (spec) | Test level | Test class / file |
|-----------------|------------|-------------------|
| n/a — `skip_specs: true`, no delta spec/scenarios for this change | n/a | n/a |

Manual verification performed instead (documented with actual output in the PR):
- SessionStart hook: start a new Claude Code session in this repo and confirm
  the OpenSpec status block appears in the transcript.
- Push guard: attempt `git push origin HEAD:main` (or equivalent) from a Bash
  tool call in this session against a disposable/no-op scenario and confirm the
  hook denies it with a clear message; then confirm pushing the feature branch
  itself (`chore/1027_...`) succeeds normally.

- New unit tests (`src/test/java/.../unit/`): n/a
- New integration tests (`src/test/java/.../integration/`): n/a
- Coverage impact (JaCoCo ratchet floor; 80% target): none — no Java/TS code changed

## Regression Strategy

- Existing tests affected: none (no application code touched)
- Full suite command: `mvn verify -pl backend-api` — run only to confirm this
  change did not accidentally touch backend files; expected no-op.
- HTTP/Bruno API suite: n/a — no API surface changed
- Legacy paths at risk (e.g. `jpa` package, `frontend-swing`): none

## Playwright Strategy

n/a - no UI surface. This change only touches `.claude/settings.json`,
`.claude/rules/hooks.md` and `CLAUDE.md`; there is no frontend behavior to test.

## Deployment Strategy

- Flyway migration required: no
- Deployment order / coupling: none — `.claude/settings.json` hooks take effect
  the next time a Claude Code session starts in this repo; nothing to deploy to
  an environment.
- Configuration or `.env` keys to add: none
- Feature flag: no
- Smoke test after deploy (Gate 5): open a fresh Claude Code session on `main`
  after merge and confirm the SessionStart hook output appears.

## Rollback Strategy

- Revert safe: yes — reverting the commit restores the previous (hook-less)
  `.claude/settings.json`; no state is written by these hooks that would need
  cleanup.
- Database rollback: none needed
- Data written under the new behavior after revert: none — hooks are
  read-only/blocking, they don't persist state
- Blast radius if rollback is delayed: none beyond "hooks stay active," which is
  the intended steady state

## Open Questions

None — scope is deliberately small enough that no decision here needs deferring.
