# Optional OTel verify bootstrap must degrade

> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md). This proposal documents
> **only this change**; permanent documentation remains the single source of truth.

| Field | Value |
|-------|-------|
| GitHub Issue | #2180 |
| Use Case | CU76 |
| Branch | `cursor/fix-2180-verify-optional-otel-ca49` |
| Gate 1 status | passed |

## Objetivo

`framework/ai-sdlc/verify.sh` hard-fails when optional OpenTelemetry venv creation lacks `ensurepip`, aborting the stdlib kit suite on cloud hosts without `python3-venv`.

## What Changes

Optional OTel bootstrap warns, removes any half-created `.venv-telemetry`, and continues so unittest discover runs (SDK-path tests stay skipped). Never claims OS isolation.

## Reglas de negocio

| Rule | Source | New / Changed / Made explicit |
|------|--------|-------------------------------|
| Optional telemetry packages must not block stdlib kit verify | MODULE.md / #2125 | Made explicit |

## Capabilities

### New Capabilities

- (none — `skip_specs: true`)

### Modified Capabilities

- (none)

## Impact Analysis

### Módulos afectados

| Module | Touched | What changes |
|--------|---------|--------------|
| `ai-sdlc-framework` | yes | `verify.sh` optional venv bootstrap |
| `backend-api` | no | — |
| `frontend` | no | — |
| CI/CD | no | — |

### Surface area

- Entities: none
- Endpoints: none
- Database: none
- Configuration: none
- Dependencies: none (optional OTel remains optional)

### Write set

- `framework/ai-sdlc/**`
- `docs/openspec/**`

### Architecture review

Follows existing architecture; no ADR. TelemetryPort stays importlib-optional (ADR-003).

## Documentation Impact

| Permanent document | What must change |
|--------------------|------------------|
| `framework/ai-sdlc/README.md` | Note verify degrades when ensurepip/venv unavailable |
| `framework/ai-sdlc/MODULE.md` | Same one-liner if needed |

## Out of Scope

- Installing `python3-venv` into the cloud image (env hardening; separate)
- Enabling `[telemetry]` on Notaire
- Isolator / OS sandbox claims
