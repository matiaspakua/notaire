# Unify Master Test Plan and testing process architecture

> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md). This proposal documents
> **only this change**; permanent documentation remains the single source of truth.

| Field | Value |
|-------|-------|
| GitHub Issue | #1511 |
| Use Case | CU76 — Quality Assurance and Testing Infrastructure |
| Branch | `cursor/1511-testing-architecture-b585` |
| Gate 1 status | ready for validation |

## Objetivo

Make Notaire’s testing process a complete, coherent DevSecOps/QA Automation
architecture: one Master Test Plan (MTP), living inventory with drift guards,
aligned coverage floors, clear artifact IDs (UC → TC/TS → Issue), and an updated
ADR-006 — following the AI SDLC and the specialization guidelines on
traceability, identification, knowledge base, fail-fast pyramid, and MTP.

## What Changes

- Rewrite `docs/300-development/303-testing/TEST-PLAN.md` as the Master Test Plan
  covering all test levels (not only E2E), with identification and traceability.
- Add `PROCESS-ARCHITECTURE.md` (high-level process boxes, owners, CI triggers,
  evidence) and generated `INVENTORY.md` from disk.
- Add `workspace/sdlc/generate-testing-inventory.py` and
  `validate-testing-inventory.py`; wire into `preflight.sh` / SDLC process checks.
- Rename the nine unprefixed Playwright specs to `TS-0130`–`TS-0138`.
- Update ADR-006 to current practice; fix CONSTITUTION §7 stale pyramid stats and
  Vitest floor; align `303-testing/README.md`, coverage docs, and pom comment.
- Archive orphan snapshot reports; clarify TC (logical) vs TS (automation) IDs.
- Extend CU76 with #1511.

## Reglas de negocio

| Rule | Source | New / Changed / Made explicit |
|------|--------|-------------------------------|
| `TEST-PLAN.md` is the Master Test Plan (MTP) for all test levels | Specialization MTP; CU76 | New |
| Living inventory MUST be regenerable from disk and drift-guarded in CI | Specialization: identificación + trazabilidad | New |
| Every Playwright spec MUST use `TS-nnnn-*.spec.ts` | Constitution §7 | Made explicit (enforce) |
| Documented coverage floors MUST match enforced tool config | Constitution §7; JaCoCo/Vitest | Changed (fix false Vitest 75%) |
| ADR-006 MUST describe current tools and suite scale | ADR process | Changed |
| Narrative `TC-*` docs are logical cases; automation IDs are `TS-*` / Bruno paths | Specialization ID scheme | Made explicit |

## Capabilities

### New Capabilities

- `testing-process-architecture`: MTP, process architecture, living inventory,
  drift validation, and aligned Constitution/ADR coverage truth.

### Modified Capabilities

- (none under existing product specs — process/docs/tooling only)

## Impact Analysis

### Módulos afectados

| Module | Touched | What changes |
|--------|---------|--------------|
| `backend-api` | yes (comment only) | JaCoCo floor comment aligned with enforced 80/65 |
| `frontend` | no | — |
| `testing/` | yes | Spec renames; inventory meta-tests |
| `docs/` | yes | MTP, process arch, ADR-006, CONSTITUTION §7, CU76 |
| CI / `workspace/sdlc` | yes | Inventory generate/validate + preflight wiring |
| `infra/` | no | — |

### Surface area

- Entities / Endpoints / Flyway: none
- Configuration / `.env`: none
- Dependencies: none

### Architecture review

- ADR-006 updated (testing strategy). No new runtime architecture.

## Documentation Impact

| Document | Action |
|----------|--------|
| `docs/300-development/303-testing/TEST-PLAN.md` | Rewrite as MTP |
| `docs/300-development/303-testing/PROCESS-ARCHITECTURE.md` | Create |
| `docs/300-development/303-testing/INVENTORY.md` | Generate (committed) |
| `docs/300-development/303-testing/README.md` | Update entry + counts via inventory |
| `docs/300-development/303-testing/E2E-TEST-MAPPING.md` | Point to inventory; note rename |
| `docs/200-architecture/202-ADR/ADR-006-testing-strategy.md` | Update |
| `CONSTITUTION.md` §7 | Fix pyramid + Vitest floor |
| `docs/100-business/102-use-cases/CU76 – …` | Add #1511 |
| `docs/300-development/303-testing/test-coverage/TEST-COVERAGE-STRATEGY.md` | Align floors |
| `docs/000-archive/…/LOGIN-TEST-REPORT.md` | Archive snapshot |
| `CHANGELOG.md` | `[Unreleased]` entry |

## Out of Scope

- Changing JaCoCo/Vitest numeric floors (only document truth).
- Filling Bruno `MISSING` rows (#953) or raising Vitest to 80% (#1246).
- Moving full Playwright off every PR (fail-fast redesign deferred).
- Generating `UseCaseRouteCatalog` from OpenAPI (follow-up).
- Product feature tests beyond renames and process guards.
