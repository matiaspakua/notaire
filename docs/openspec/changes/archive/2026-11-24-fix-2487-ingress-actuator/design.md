> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — §6 Quality Gates.

## Context

`docker-compose.prod.yml` mounts `infra/deploy/kustomize/base/nginx.conf`; the Kustomize base renders the same file into a ConfigMap, so one file covers both deployments. Probes use `/actuator/health/liveness` inside the container; Prometheus scrapes `notary-backend:8080` directly.

## Goals / Non-Goals

**Goals:** Close the public actuator surface with no change to probes or scraping.
**Non-Goals:** Changing the backend actuator configuration.

## Decisions

D1 Allow-list `/actuator/health` (exact and prefix) and deny the rest with `return 404` at the proxy, instead of relying on the backend's Basic credential.
D2 A static test parses nginx.conf; the same file serves compose and Kustomize, so one test covers both.

## Riesgos / Trade-offs

An operator who scraped through the ingress loses that path; the private-network scrape is the documented one.

## Testing Strategy

`infra/tests/test_ingress_actuator.py`: the health locations proxy to the backend, a catch-all `/actuator/` location returns 404 and does not proxy, and no other location proxies an actuator path.

## Regression Strategy

`test_prod_compose.py`, `test_staging_kustomize.py`, `security/tests` (STRIDE register parity) stay green.

## Playwright Strategy

n/a — no UI surface.

## Deployment Strategy

Ships with the next release; operators reload the proxy.

## Rollback Strategy

Revert the PR.

## Migration Plan

None.

## Open Questions

None.
