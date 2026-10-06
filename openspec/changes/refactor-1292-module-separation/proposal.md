# Phase 1 module separation

> Governed by [CONSTITUTION.md](../../../CONSTITUTION.md). This proposal documents
> **only this change**; permanent documentation remains the single source of truth.

| Field | Value |
|-------|-------|
| GitHub Issue | #1292 |
| Use Case | CU76 – Quality Assurance and Testing Infrastructure |
| Branch | `refactor/1292_phase1_module_separation` |
| Gate 1 status | approved by the Owner on 2026-10-06 |

## Objetivo

Parent #1197 and ADR-024 aim at granular repositories that a Foreman agent unifies through contracts, so independent agent fleets (security, infrastructure, knowledge base) can work in isolation. ADR-024 gates 1 and 2 require, per area, a standalone guard and documented seams. Phase 1 delivers them **inside this repository**, as folders, before any extraction. Each module has one responsibility, one way to verify itself and one explicit contract.

## What Changes

- `workspace/modules.yaml`: the single manifest the Foreman reads. Per module: path, responsibility, owner fleet, verify command, contract files, `depends_on`.
- Every module gets `MODULE.md` (purpose, public contract, seams, what it must not know) and an executable `verify.sh` (build, test, format, lint for that module only).
- `contracts/`: every seam between modules (OpenAPI, image names, environment variable names, endpoints), each with a drift guard.
- `security/`: the security assets now scattered across the repository.
- Per-area guards move from `scripts/` into their module; only cross-area guards stay in `workspace/`.
- `docs/` is the knowledge base; its generators move in. Code that nothing needs any more moves to `deprecated/` (no deletion; the Owner decides later).
- Not renamed: `backend-api`, `frontend`.

## Reglas de negocio

| Rule | Source | New / Changed / Made explicit |
|------|--------|-------------------------------|
| A module may read another module only through `contracts/`; only `workspace/` reads all modules | #1197, ADR-024 gate 2 | Made explicit |
| A module is verified by its own `verify.sh` with no knowledge of other modules' internals | ADR-024 gate 1 | New |

## Capabilities

### New Capabilities

- `module-separation`: manifest, module contract and per-module verification.

### Modified Capabilities

- (none)

## Impact Analysis

### Módulos afectados

| Module | Touched | What changes |
|--------|---------|--------------|
| `backend-api` | yes | `MODULE.md`, `verify.sh`; no production code |
| `frontend` | yes | `MODULE.md`, `verify.sh`; no production code |
| `infra`, `testing`, `local-ai` | yes | `MODULE.md`, `verify.sh`, guards move in |
| `docs`, `scripts`, `.github/workflows` | yes | generators and guards relocated; workflow paths updated per slice |
| new `workspace/`, `contracts/`, `security/` | yes | new |

### Surface area

- Endpoints, entities, Flyway, dependencies: none
- Risk: moving a guard or workflow path silently disables a CI gate. Mitigation: one slice per PR, `scripts/preflight.sh` after each, `--full` (Playwright E2E, Bruno, Docker smoke) at the end, and a guard that every module in the manifest verifies.

### Architecture review

Architectural: records the module topology (new ADR-026, ADR-024 status table).

## Documentation Impact

| Permanent document | What must change |
|--------------------|------------------|
| `docs/200-architecture/202-ADR/ADR-026-module-separation.md` | new |
| `docs/200-architecture/202-ADR/ADR-024-repository-topology.md` | extraction status table |
| `docs/300-development/REPO-SPLIT-PLAN.md` | Phase 1 delivered |
| `AGENTS.md`, `CONSTITUTION.md` | module map and manifest pointer |
| `CHANGELOG.md` | one entry per slice |
