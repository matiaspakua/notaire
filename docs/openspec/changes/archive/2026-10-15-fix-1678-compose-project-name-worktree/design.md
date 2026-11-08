> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — §6 Quality Gates.

## Context

Local Docker quality smoke from a worktree showed Prometheus notaire-backend/frontend-metrics DOWN and backend UnknownHostException for devsecops-otel-collector. Manual network connect proved the cause.

## Goals / Non-Goals

**Goals:** Make worktree-based start-all join the same network infra expects, without breaking intentional parallel stacks.
**Non-Goals:** Do not invent Owner product work; do not thrash CHANGELOG; do not fix Sonar #1680 or hexagonal #1693 here.

## Decisions

1. **Default COMPOSE_PROJECT_NAME in start.sh, not a hardcoded network name in docker-compose.yml.** Parallel stacks (#1186) still override the project name; pinning the network `name:` would collide those stacks on one network.

## Riesgos / Trade-offs

- [Operators forget the default and expect directory-named projects] → documented in the deployment guide; override remains available.

## Testing Strategy

`StartScriptTest.test_start_script_defaults_compose_project_name_for_observability` and the full isolation suite.

## Regression Strategy

python3 infra/tests/test_dev_stack_isolation.py; local compose smoke with check-infra.sh.

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
