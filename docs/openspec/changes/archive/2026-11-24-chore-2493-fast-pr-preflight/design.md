> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — §6 Quality Gates.

## Context

Since #2001 GitHub Actions run the product suites only on version tags, so the local preflight is the only PR gate. #2413 made it change-aware, but `--full` still starts the app stack and runs the 8-minute Playwright suite for any branch that touches the stack, and a change to the gate scripts forces everything.

## Goals / Non-Goals

**Goals:** PR gate in minutes, not tens of minutes; no repeated runs; nothing lost before a release.
**Non-Goals:** Removing any suite.

## Decisions

D1 Default hook mode = default preflight (no --full). D2 `--all` before release-please dispatch, plus the tag CI. D3 preflight.sh writes `<sha>.<mode>` markers only on a clean tree; the hook accepts pr<full<all. D4 The gate scripts leave FORCE_ALL; Docker, pom.xml and the stack stay in it.

## Riesgos / Trade-offs

A regression only a server-backed suite catches now surfaces at release instead of at the PR: the release run and the tag CI catch it, and the PR may still opt in with PREFLIGHT_FULL=1 for risky changes (auth, Flyway on existing rows, REST contract).

## Testing Strategy

`test_preflight_release_gate.py` (hook default, preflight messages, RELEASE.md, marker writing), `test_preflight_hook_cache.py` (manual pass covers the hook; pr does not cover full), `test_preflight_affected.py` (gate scripts no longer force all).

## Regression Strategy

Existing hook-cache, hook-env, run_pipeline and CI invariant tests stay green.

## Playwright Strategy

n/a — no UI surface.

## Deployment Strategy

Ships with the next release.

## Rollback Strategy

Revert the PR.

## Migration Plan

None.

## Open Questions

None.
