> Governed by [CONSTITUTION.md](../../../CONSTITUTION.md) — §6 Quality Gates,
> §7 Testing Rules, §11 Release Rules.

## Context

Issue #1186, Use Case CU76. `docker-compose.yml` hardcodes four container names and
four host ports; `scripts/start.sh` health-checks fixed `localhost` ports. A second stack
collides even with a different `COMPOSE_PROJECT_NAME`. The idea was recovered from a
stashed, never-filed WIP (`docker-isolation-921`) and is re-implemented test-first
against current `main` rather than applied from the stale diff.

## Goals / Non-Goals

**Goals:**

- Overridable names and host ports through `.env`, defaults unchanged.
- `start.sh` consistent with the configured ports.

**Non-Goals:**

- Prod and cloud compose files; the observability stack following custom names.

## Decisions

1. **`${VAR:-default}` on host port and container name only**
   - Why: smallest change that removes the collisions; container ports and
     in-network service names stay, so backend/frontend wiring is untouched.
   - Alternative rejected: drop `container_name` entirely — breaks the observability
     scrape targets and scripts that `docker exec` by name.

2. **Variable names**: `POSTGRES_PORT`, `BACKEND_PORT`, `PGADMIN_PORT`, `FRONTEND_PORT`,
   `NOTAIRE_{POSTGRES,BACKEND,PGADMIN,FRONTEND}_CONTAINER_NAME`.
   - Why: ports follow the existing `POSTGRES_*` style; names are namespaced because
     `*_CONTAINER_NAME` is generic.

3. **Re-implement, do not `git stash apply`**: the stash is based on a commit weeks old.

## Riesgos / Trade-offs

- [A set override silently breaks the observability stack] → documented limitation
  in the deployment guide.
- [Backend CORS or frontend URLs hardcode a port] → the frontend reaches the backend
  server-side over the compose network, so host ports are not involved; verified by the
  smoke test with overrides.

## Testing Strategy

| Scenario (spec) | Test level | Test class / file |
|-----------------|------------|-------------------|
| Names and host ports overridable | static | `scripts/test_dev_stack_isolation.py` |
| Defaults render today's values | `docker compose config` | same (skipped without docker) |
| Overrides rendered | `docker compose config` | same |
| start.sh follows ports | static | same |
| Keys documented | static | same |

- New unit tests (`src/test/java/.../unit/`): n/a (no Java touched)
- New integration tests: n/a
- Coverage impact (JaCoCo): none

## Regression Strategy

- Existing tests affected: compose guards (`test_image_pins_and_dependabot.py`,
  `test_prod_compose.py`) must stay green.
- Full suite command: `bash scripts/preflight.sh`, then `bash scripts/run_pipeline.sh`.
- HTTP/Bruno suite: unchanged defaults, run via the pipeline.
- Legacy paths at risk: none.

## Playwright Strategy

n/a — no UI surface. Required Playwright CI still runs against default ports.

## Deployment Strategy

- Flyway migration required: no
- Deployment order / coupling: single PR; defaults identical.
- Configuration or `.env` keys: eight optional keys in `.env.example`.
- Feature flag: no
- Smoke test after deploy (Gate 5): default `start.sh` healthy; second stack with
  overrides starts beside it.

## Rollback Strategy

- Revert the PR; defaults were identical, so running stacks are unaffected.
