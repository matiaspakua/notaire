> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — §6 Quality Gates.

## Context

Groups: backend, frontend, testing, pages, stack, kit, boundaries. The stack group only runs with --full or --all, so mapping compose files to it keeps the PR gate light and the release gate complete.

## Goals / Non-Goals

**Goals:** PR gate cost proportional to what a build file can break.
**Non-Goals:** Changing what --all runs.

## Decisions

D1 docker-compose* and workspace/stack/ select stack. D2 root pom.xml selects backend and stack (it holds the backend build). D3 module Dockerfiles already select their module by prefix; a root-level Dockerfile selects stack.

## Riesgos / Trade-offs

A compose change that breaks a unit-level suite is caught by --all before the release.

## Testing Strategy

`test_preflight_affected.py`: compose, stack, pom and Dockerfile mappings; no path returns every group.

## Regression Strategy

The other affected-group tests stay green.

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
