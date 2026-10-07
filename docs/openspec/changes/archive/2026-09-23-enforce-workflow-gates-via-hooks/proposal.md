# Enforce Constitution/OpenSpec gates via Claude Code hooks

> Governed by [CONSTITUTION.md](../../../CONSTITUTION.md). This proposal documents
> **only this change**; permanent documentation remains the single source of truth.

| Field | Value |
|-------|-------|
| GitHub Issue | #1027 |
| Use Case | none — internal tooling/process change, no business behavior; see "Reglas de negocio" below for the documented exception (precedent: issue #973) |
| Branch | `chore/1027_enforce_workflow_gates_via_hooks` |
| Gate 1 status | passed |

## Objetivo

The CONSTITUTION.md/OpenSpec workflow (Issue -> Use Case -> proposal/spec/design/tasks
-> TDD -> PR) is fully documented but has zero automated enforcement inside the
Claude Code session itself: `.claude/settings.json` has no hooks configured, so
every gate depends entirely on an agent choosing to read and follow CLAUDE.md
voluntarily. This change adds a small set of Claude Code lifecycle hooks that make
the highest-value parts of the workflow part of the tool-call surface rather than
prose the agent might skip.

## What Changes

- Add a `SessionStart` hook that runs `openspec list` (falls back to a static
  message if the CLI is unavailable) and prints in-flight OpenSpec changes so the
  agent has awareness of active work without relying on memory.
- Add a `PreToolUse` hook on `Bash` that detects a `git push` whose target
  resolves to `main`/`master` (explicit ref argument, or the current branch when
  none is given) and blocks it, printing a message pointing at the PR workflow.
  This is defense in depth: `gh api repos/matiaspakua/notaire/branches/main/protection`
  currently returns `404 Branch not protected` — GitHub-side branch protection is
  not configured, so this hook is not merely redundant with it.
- Evaluate a `UserPromptSubmit`/`PreToolUse` nudge for edits under
  `backend-api/src` or `frontend/src` with no matching `openspec/changes/*` present.
  **Decision: rejected**, see Out of Scope — recorded here rather than implemented.
- Document the hooks in a new `.claude/rules/hooks.md`, linked from `CLAUDE.md`.

## Reglas de negocio

| Rule | Source | New / Changed / Made explicit |
|------|--------|-------------------------------|
| No business rule — this is an internal agent-harness/process change with no user-facing or business-data behavior change. Documented exception per CONSTITUTION.md, following the precedent already set in issue #973 (purely technical epic with no Use Case). | n/a | n/a |

## Capabilities

### New Capabilities
None — no spec-level (product) behavior changes. `skip_specs: true` set in `.openspec.yaml`.

### Modified Capabilities
None.

## Impact Analysis

### Módulos afectados

| Module | Touched | What changes |
|--------|---------|--------------|
| `backend-api` | no | — |
| `frontend` | no | — |
| `frontend-swing` | no | — |
| `notaire-shared` | no | — |
| `infra` / observability | no | — |
| CI/CD (`.github/workflows`) | no | — |
| `.claude/` (agent harness) | yes | New hooks in `.claude/settings.json`; new `.claude/rules/hooks.md` |
| `CLAUDE.md` | yes | New short section pointing at `.claude/rules/hooks.md` |

### Surface area

- Entities: none
- Endpoints: none
- Database (Flyway `V{n}`): none
- Configuration / `.env`: none — hooks are inline shell commands in `.claude/settings.json`, no new secrets/env keys
- Dependencies: none (uses `bash`, `git`, `jq`/`python3` already available in the dev environment; `openspec` CLI already a repo dependency)

### Architecture review

No architectural change to the application. This only affects the Claude Code
agent harness configuration (`.claude/settings.json`), which is not part of the
Spring Boot/Next.js runtime architecture. No ADR required.

## Documentation Impact

| Permanent document | What must change |
|--------------------|------------------|
| `CLAUDE.md` | Add a short "Claude Code hooks" pointer section linking to `.claude/rules/hooks.md`, DRY against CONSTITUTION.md |
| `.claude/rules/hooks.md` (new) | Document what each hook does, why, and how it was tested |
| `CHANGELOG.md` | n/a — not user visible (internal dev tooling only) |

## Out of Scope

- **Rejected candidate: OpenSpec-change nudge hook on backend/frontend source edits.**
  Evaluated as a `PreToolUse` hook on `Write`/`Edit` matching `backend-api/src/**`
  or `frontend/src/**` with no corresponding `openspec/changes/*` directory
  present. Rejected as net-negative: it would fire on every trivial edit
  (typo fixes, comment updates, work already inside an approved change whose
  directory name doesn't lexically match the touched path) with no reliable way
  to distinguish "mid-implementation of an approved change" from "no process
  followed" from a shell heuristic — producing noise that trains the agent (and
  the human) to ignore hook output, which defeats the purpose. `SessionStart`
  awareness plus the existing `scripts/validate-sdlc-plan.sh` / pre-push hook
  already cover this at a coarser, less annoying granularity.
- Enforcing TDD (tests-written-before-code) via a hook — not mechanically
  detectable from a tool call in general; left to `scripts/preflight.sh` and
  human/CI review as today.
- Blocking `--no-verify` / `--force` pushes generally — out of scope for this
  change; only the direct-push-to-main case is addressed here.
- Any change to GitHub-side branch protection settings — flagged as a follow-up
  recommendation in the PR, not implemented here (this is an internal Claude Code
  harness change, not a repo/GitHub settings change).
