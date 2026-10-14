> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — §6 Quality Gates.

## Context

`prometheus.yml` scrapes `notary-backend:8080` with basic auth `admin`/`admin`, which only exists inside the Docker network. The native stack serves the backend on localhost with the CI actuator credentials.

## Goals / Non-Goals

**Goals:** a repeatable live check of the scrape and rules.
**Non-Goals:** other observability services; alert firing.

## Decisions

1. **Derive the test config from the repo file** by replacing only the target host and the credentials, so a change to jobs, labels or rule files is exercised.
2. **Skip, not fail, without the prerequisites**, so CI jobs without a backend stay green; the native stack and cloud sessions run it.
3. **Reuse the promtool tarball** for the binary; no new download.

## Riesgos / Trade-offs

- [Startup race] → poll the targets API up to 40 s.
- [Port clash] → pick a free port.

## Testing Strategy

`test_prometheus_live.py` plus `test_cloud_toolchain_bootstrap.py` (hook pin). The pin test is written first and fails; the live test was run against the native stack.

## Regression Strategy

`python3 -m unittest discover -s infra/tests` and `workspace/tests`.

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
