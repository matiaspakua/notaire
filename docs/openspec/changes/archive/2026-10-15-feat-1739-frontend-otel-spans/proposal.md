# Instrument frontend OpenTelemetry spans to the collector

> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md). This proposal documents
> **only this change**; permanent documentation remains the single source of truth.

| Field | Value |
|-------|-------|
| GitHub Issue | #1739 |
| Use Case | CU77 – Operations Monitoring and Incident Management |
| Branch | `cursor/feat-1739-frontend-otel-2ea7` |
| Gate 1 status | passed |

## Why

Backend already exports OTLP traces to Tempo. Monitoring README notes frontend spans are not instrumented — gap vs full integrated observability (browser → collector → Tempo).

## What Changes

- Browser WebTracerProvider + fetch instrumentation, service.name=notaire-frontend
- Same-origin BFF POST /api/otlp/v1/traces → collector (no browser Docker hostnames)
- Opt-in NEXT_PUBLIC_OTEL_TRACES_ENABLED; compose enables it
- Guards + monitoring README; CHANGELOG Unreleased Features

## Business rules

| Rule | Source | New / Changed / Made explicit |
|------|--------|-------------------------------|
| When FE OTel is enabled, browser spans MUST reach the collector as service notaire-frontend without PII attributes | CU77, #1739 | New |

## Capabilities

### New Capabilities
- `frontend-otel-browser-traces`: Browser OpenTelemetry traces export via Next BFF to the OTel collector

### Modified Capabilities

## Impact Analysis

### Affected modules

| Module | Touched | What changes |
|--------|---------|--------------|
| `backend-api` | yes | no |
| `frontend` | yes | otel init, BFF proxy route, providers, compose env, vitest |
| `frontend-swing` | no | — |
| `infra` / observability | yes | optional docs note |
| CI/CD (`.github/workflows`) | yes | no |

### Surface area

- Entities: none
- Endpoints: none
- Database (Flyway `V{n}`): none
- Configuration / `.env`: none
- Dependencies: none

### Architecture review

Follows the existing architecture; no ADR.

## Documentation Impact

| Permanent document | What must change |
|--------------------|------------------|
| `docs/200-architecture/207-monitoring/README.md` | Frontend spans instrumented via BFF OTLP proxy |
| `CHANGELOG.md` | Unreleased Features for browser OTel |

## Out of Scope

- #1740 Mac ownership
- Full document-load / user-interaction instrumentations beyond fetch
