> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — §6 Quality Gates,
> §7 Testing Rules, §11 Release Rules.

## Context

The local gate is the pull-request gate (#2001): GitHub does not re-run the product suites. It runs everything on every push, about 35 minutes.

## Goals / Non-Goals

Goals: a branch pays only for the suites its changes can break, and a re-push of a passed commit is instant. Non-goals: weakening any check, parallel suites.

## Decisions

- D1 Keep the name `--full`: every governance sentence ("a PR is ready when `preflight.sh --full` passed") stays true; `--all` is the explicit everything mode.
- D2 Classification by path prefix in `preflight_affected.py` (stdlib), not `modules.py affected`: that one cascades dependents (backend pulls frontend, testing and workspace) and does not cover `github-page/`, root files or the gate.
- D3 Groups: backend = `backend-api/`, `contracts/`; frontend = `frontend/`; e2e lint = `testing/`; pages = `github-page/`, `docs/`, `CHANGELOG.md`, `release/`, `workspace/release/`; server-backed = backend, frontend, `testing/`, `infra/`, `contracts/`. Force-all = the gate, `.githooks/`, `workspace/stack/`, compose files, the root `pom.xml`, Dockerfiles.
- D4 The pass cache is keyed on the commit sha (not only the tree), so a reworded commit message is still checked by the commit-message gate; markers live under the git common directory so every worktree shares them and nothing is committed.

## Riesgos / Trade-offs

A path the classification misses would skip a suite that could fail. Mitigations: anything outside the known groups only skips product suites, the force-all list covers shared infrastructure, `--all` stays available, and a release runs `--all`.

## Testing Strategy

Unit tests for the classification (each scenario) and for the hook cache with a throwaway repository and a stub preflight.

## Regression Strategy

Existing workspace self-tests for preflight (hook env, Playwright browsers, layout) keep passing.

## Playwright Strategy

n/a — no UI surface.

## Deployment Strategy

Local only: the hook and preflight ship with the branch; `bash workspace/sdlc/install-git-hooks.sh` is unchanged.

## Rollback Strategy

Revert the merge commit, or run `preflight.sh --all` (and `PREFLIGHT_ALL=1 git push`) to get the old behaviour.

## Migration Plan

None.

## Open Questions

None.
