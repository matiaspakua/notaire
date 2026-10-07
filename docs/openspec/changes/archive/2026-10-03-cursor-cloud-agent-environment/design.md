# Design — Cursor Cloud Agent environment scripts

## Context

Cursor Cloud Agent VMs run nested Docker. Bridge networks between Postgres and
the backend time out; host networking + `127.0.0.1` works. Agents need
idempotent install/start entrypoints wired from Cloud `environment.json`.

## Goals / Non-Goals

- Goal: version `.cursor/install.sh`, `.cursor/start.sh`, and
  `docker-compose.cloud.yml` for reproducible Cloud boots.
- Non-goal: change default local `docker-compose.yml` behavior for developers.
- Non-goal: fleet docs/agents (owned by PR #1111 / Issue #1114).

## Decisions

- **`skip_specs: true`**: chore/scripts only; no product requirement deltas.
- **Host networking override** in a separate compose file so local bridge
  networking remains the default outside Cloud.
- **Idempotent install**: second run of `install.sh` must succeed without
  destructive resets of developer state.

## Riesgos / Trade-offs

- Host networking reduces container isolation on the Cloud VM — acceptable for
  ephemeral agent environments, not a production deploy pattern.
- Branch name uses Cloud Agent prefix `cursor/cloud_agent_environment` rather
  than `<type>/<issue>_…`; Issue #1115 records the association.

## Testing Strategy

No application test surface. Verification (Gate 2 equivalent):

- Idempotent `bash .cursor/install.sh` (second run OK).
- Stack healthy: `/actuator/health` UP, login JWT OK, frontend `/login` 200
  (manual / Cloud boot evidence).
- `git diff --name-only origin/main...HEAD | grep '^openspec/changes/'` present
  so `check-sdlc-exception.sh` passes.

## Regression Strategy

No application code under `backend-api` / `frontend/src`. Confirm
`sdlc-process.yml` Process Checks pass once this OpenSpec folder is in the PR
diff.

## Playwright Strategy

n/a — no UI surface changed by this PR.

## Deployment Strategy

Nothing is deployed to production runtime. Merge makes the scripts available to
Cloud `environment.json` install/start hooks.

## Rollback Strategy

Plain `git revert` of the merge commit.

## Open Questions

None for this change. Fleet docs are sibling PR #1111.
