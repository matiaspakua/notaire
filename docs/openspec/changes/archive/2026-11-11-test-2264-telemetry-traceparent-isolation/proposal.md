# Isolate telemetry TRACEPARENT assertions from the parent process

> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md). This proposal documents
> **only this change**; permanent documentation remains the single source of truth.

| Field | Value |
|-------|-------|
| GitHub Issue | #2264 |
| Use Case | CU76 — Quality Assurance and Testing Infrastructure |
| Branch | `cursor/2264-telemetry-traceparent-b566` |
| Gate 1 status | passed |

## Objetivo

Cloud agent shells export `TRACEPARENT` into every process. The kit telemetry default-off test asserts the *parent* process env is empty, so `bash framework/ai-sdlc/verify.sh` fails in those sessions even though the kit did not inject the header. Agents already work around with `env -u TRACEPARENT` (#2261). Fix the test so verify is honest under a contaminated parent env.

## What Changes

- 
- 
- `framework/ai-sdlc/tests/test_telemetry_port.py`: clear `TRACEPARENT` and `OTEL_*` for the duration of the default-off scenario (via `unittest.mock.patch.dict`), stop copying parent `TRACE*` into the child probe, and keep asserting the kit does not inject.
- 
- 
- Delta on `Observability`: require kit self-tests to isolate from parent TRACEPARENT/OTEL_*.

## Reglas de negocio

| Rule | Source | New / Changed / Made explicit |
|------|--------|-------------------------------|
| Kit self-tests prove “no TRACEPARENT inject” without requiring the parent process env to be empty | #2264, CU76, axiom: skip ≠ pass (test must evaluate) | New / Made explicit |

## Capabilities

### New Capabilities

### Modified Capabilities

- 
- 
- `Observability`: default-off TRACEPARENT proof must isolate from parent process environment variables.

## Impact Analysis

### Módulos afectados

| Module | Touched | What changes |
|--------|---------|--------------|
| `backend-api` | no | — |
| `frontend` | no | — |
| `frontend-swing` | no | — |
| `infra` / observability | yes | kit unit test only (`framework/ai-sdlc`) |
| CI/CD (`.github/workflows`) | no | — |

### Surface area

- 
- 
- Entities: none
- 
- 
- Endpoints: none
- 
- 
- Database (Flyway `V{n}`): none
- 
- 
- Configuration / `.env`: none
- 
- 
- Dependencies: none

### Write set

- 
- 
- `framework/ai-sdlc/tests/test_telemetry_port.py`
- 
- 
- `docs/openspec/**`
- 
- 
- `docs/300-development/303-testing/CU-API-MATRIX.csv`  # boy-scout: MfaController drift after #1640 blocks every preflight

### Architecture review

Test-only; no ADR. TelemetryPort production behaviour unchanged.

## Documentation Impact

| Permanent document | What must change |
|--------------------|------------------|
| `docs/openspec/specs/Observability/spec.md` | sync via archive |
| `CHANGELOG.md` | n/a — not user-visible product behaviour |

## Out of Scope

Changing TelemetryPort emit/inject behaviour; adding OTel exporters; Foreman wiring.
