# Phase A.4 — Project, Run, RunRequest with evidence-derived status

> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md). This proposal documents
> **only this change**; permanent documentation remains the single source of truth.

| Field | Value |
|-------|-------|
| GitHub Issue | #2255 |
| Use Case | CU76 — Quality Assurance and Testing Infrastructure |
| Branch | `cursor/2255-project-run-model-b926` |
| Gate 1 status | passed |

## Objetivo

Phase D (#2242) needs to create a project, bind repositories, start a run and
monitor progress, but the factory contract has no Project, Run or RunRequest,
and `aisdlc foreman plan|route` stays `executes=false`. "Start" is undefined and
the UI must not invent `running` from a click. Phase A.4 closes F4 with honest
entities, evidence-bound RunRequest events, and a harness reporting contract.

## What Changes

- OpenAPI 3.1 + service layer schemas: Project (`profile_ref`, repos with
  `mode: dry_run|live`, default `dry_run`), Run (`workflow{id,version,digest}`,
  `status`, `requested_by`, `evidence_head`, `steps[]`), RunRequest.
- Start-run creates a `RunRequest` evidence ledger event bound to an
  **approved** workflow version + digest; non-approved fails with a stable code.
- Run status is derived only from evidence events. Only a harness adapter
  appending `run.started` / `step.*` / `run.finished` can move past `queued`.
- Harness reporting contract documented and implemented for one reference
  adapter (local worker).
- HTTP: `GET/POST /factory/projects`, `GET/POST /factory/runs`,
  `GET /factory/runs/{id}`, `GET /factory/runs/{id}/events`;
  `POST /factory/runs/{id}/cancel` → 501 with capability reason while
  `runner=false`.
- Agent never commits or approves via these paths; skip is never pass; step
  budget overrun marks the run `blocked`.
- `aisdlc evidence verify` covers the new event kinds.
- Kit-only tip under `framework/ai-sdlc` (+ OpenSpec/docs); one OpenAPI tree.

## Reglas de negocio

| Rule | Source | New / Changed / Made explicit |
|------|--------|-------------------------------|
| Project binds `profile_ref` and repos; repo `mode` defaults to `dry_run` | #2255 AC; FACTORY-UI-SPEC §5 | New |
| Start run requires an `approved` workflow version + digest; else stable error | #2255 AC; CU76 runtime | New |
| Start run appends a `RunRequest` evidence event; status starts `requested`/`queued` | #2255 AC; FACTORY-UI-SPEC §5 | New |
| Status is evidence-derived only; kit never reports `running` from the request alone | #2255 AC; F4 honesty | New |
| Only harness adapters append `run.started` / `step.*` / `run.finished` | #2255 AC; harness contract | New |
| Cancel returns 501 with capability reason until Runner port is live | #2255 AC; capabilities | New |
| Agent paths never commit/approve; skip ≠ pass; budget overrun → `blocked` | #2255 AC; axioms | New |
| Foreman `executes=false` stays honest — no fake execute | #2255; foreman plan | Made explicit |

## Capabilities

### New Capabilities

- `factory-project-run`: Project / Run / RunRequest domain, evidence-derived
  status, harness reporting contract, HTTP/CLI surfaces for A.4 (no SPA).

### Modified Capabilities

- `factory-contract`: additive OpenAPI schemas/paths for projects and runs.
- `aisdlc-serve`: GET/POST projects and runs; cancel 501 honesty.

## Impact Analysis

### Módulos afectados

| Module | Touched | What changes |
|--------|---------|--------------|
| `backend-api` | no | — |
| `frontend` | no | — |
| `frontend-swing` | no | — |
| `infra` / observability | no | — |
| CI/CD (`.github/workflows`) | no | — |
| `workspace` (harness) | yes | `framework/ai-sdlc/**`, OpenSpec, kit docs |

### Surface area

- Entities: Project, Run, RunRequest; evidence event kinds for run lifecycle
- Endpoints: kit OpenAPI project/run paths; serve implements them with `--write` for POST
- Database (Flyway `V{n}`): none
- Configuration / `.env`: none new; projects file-backed under profile evidence/catalogs area
- Dependencies: stdlib only

### Write set

- `framework/ai-sdlc/**`
- `docs/openspec/**`
- `docs/300-development/306-ai-sdlc-framework/**` (pointer notes only if needed)
- `docs/300-development/303-testing/INVENTORY.md` (pointer only if needed)
- `CHANGELOG.md` — omit Unreleased (fence held by #2271)

### Out of scope / Refuse

- Phase D SPA (#2242); Context Lake / Port.io clone; Wilson vanity; fake Isolator
- Changing committer `required=true`
- Second OpenAPI tree; hosted compute; auto-merge
- Fake execute when foreman `executes=false`

## Documentation Impact

| Document | Action |
|----------|--------|
| `framework/ai-sdlc/openapi/factory-control.openapi.yaml` | Add Project/Run/RunRequest schemas and paths |
| `framework/ai-sdlc/README.md` | Document project/run CLI + harness report contract |
| `docs/300-development/306-ai-sdlc-framework/FACTORY-UI-SPEC.md` | Point A.4 shipped (minimal) if needed |
| `CHANGELOG.md` | Omit Unreleased while #2271 holds fence |

## Success criteria

Gate 1 plan validates; failing tests first then green; `bash framework/ai-sdlc/verify.sh`
and `bash workspace/sdlc/preflight.sh --full` pass; PR `Closes #2255` only.
