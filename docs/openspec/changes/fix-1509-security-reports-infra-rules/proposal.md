# Security reports service keeps the infra self-containment rules

> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md). This proposal documents
> **only this change**; permanent documentation remains the single source of truth.

| Field | Value |
|-------|-------|
| GitHub Issue | #1509 |
| Use Case | CU78 – Security and Compliance |
| Branch | `fix/1509_security_reports_infra_rules` |
| Gate 1 status | Pending |

## Objetivo

#1507 (#1506) added the `security-reports` service and broke two infra rules on `main`: no path outside `infra/`, and exactly one `nginx.conf`. Restore both without changing what the service does.

## What Changes

- Serve the reports with an inline compose `configs` entry instead of a second `nginx.conf` file (the file is deleted).
- Make the reports directory overridable (`SECURITY_REPORTS_DIR`, default `../../reports/security`) with the repo's `INFRA_ROOT_ENV_FALLBACK` exemption marker, the same pattern `infra/scripts/common.sh` uses; add the key to `infra/.env.example`.

## Reglas de negocio

| Rule | Source | New / Changed / Made explicit |
|------|--------|-------------------------------|
| `infra/` references nothing outside itself except documented, overridable fallbacks. | `infra/tests/test_infra_standalone.py` | Made explicit |
| `nginx.conf` exists exactly once. | same | Made explicit |

## Capabilities

### New Capabilities
- `infra-rules`: the security reports service complies with the infra standalone rules.

### Modified Capabilities
None.

## Impact Analysis

### Módulos afectados

| Module | Touched | What changes |
|--------|---------|--------------|
| `backend-api` | No | |
| `frontend` | No | |
| `frontend-swing` | No | Retired. |
| `infra` | Yes | compose service config, `.env.example`. |
| CI/CD | No | |

### Surface area

- Endpoints/entities/Flyway: none. `.env`: one optional key with a default. Compatibility: same ports and URLs.

### Architecture review

No ADR; keeps ADR-016's topology.

## Documentation Impact

| Permanent document | What must change |
|--------------------|------------------|
| `CHANGELOG.md` | N/A: local tooling. |

## Out of Scope

- Redesigning how the security catalog publishes reports (owner of #1506).
