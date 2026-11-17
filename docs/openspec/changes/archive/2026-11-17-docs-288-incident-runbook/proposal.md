# docs: incident runbook grounded in the existing stack commands

> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md). This proposal documents
> **only this change**; permanent documentation remains the single source of truth.

| Field | Value |
|-------|-------|
| GitHub Issue | #288 |
| Use Case | CU77 — Operations Monitoring and Incident Management |
| Branch | `cursor/288-incident-runbook-47dc` |
| Gate 1 status | passed |

## Objetivo

Operators have start, health and Grafana checks, and no incident runbook. This change adds one runbook that uses those existing commands for the failures they already detect. It does not invent an on-call rota or claim a drill was run.

## What Changes

- Add `infra/docs/INCIDENT-RUNBOOK.md` with scenarios, a response outline, an escalation note, a communication template, a post-incident template and a recovery checklist.
- Link it from `infra/docs/OPERATION.md`.
- Guard the file with a unit test that requires the real command names and forbids a claim that a drill already happened.

## Reglas de negocio

| Rule | Source | New / Changed / Made explicit |
|------|--------|-------------------------------|
| An incident response uses the commands already in the repo | CU77; #288 | Made explicit |
| Training and a live drill stay owner actions | CU77; #288 | Made explicit |

## Capabilities

### New Capabilities

- None.

### Modified Capabilities

- None. `.openspec.yaml` sets `skip_specs: true`.

## Impact Analysis

### Módulos afectados

| Module | Touched | What changes |
|--------|---------|--------------|
| `backend-api` | no | |
| `frontend` | no | |
| `frontend-swing` | no | |
| `infra` / observability | yes | A runbook and a test |
| CI/CD (`.github/workflows`) | no | |

### Surface area

- Entities: none
- Endpoints: none
- Database (Flyway `V{n}`): none
- Configuration / `.env`: none
- Dependencies: none
- Not **BREAKING**.

### Write set

- `infra/docs/INCIDENT-RUNBOOK.md`
- `infra/docs/OPERATION.md`
- `infra/tests/test_incident_runbook.py`
- `docs/openspec/changes/docs-288-incident-runbook/**`

### Architecture review

No architectural change. No ADR.

## Documentation Impact

| Permanent document | What must change |
|--------------------|------------------|
| `infra/docs/INCIDENT-RUNBOOK.md` | New |
| `infra/docs/OPERATION.md` | Link to the runbook |
| `CHANGELOG.md` | n/a — not user visible |

## Out of Scope

- Naming people for an on-call rota. None is configured.
- Claiming the team was trained or that a drill was executed.
- Closing #288. Those two acceptance items stay open.
- TLS, backups and SLO documents (#254, #256, #306).
