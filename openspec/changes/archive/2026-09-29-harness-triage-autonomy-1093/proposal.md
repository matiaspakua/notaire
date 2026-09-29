# Triage autonomy fixes found in #1064

> Governed by [CONSTITUTION.md](../../../CONSTITUTION.md). This proposal documents
> **only this change**; permanent documentation remains the single source of truth.

| Field | Value |
|-------|-------|
| GitHub Issue | #1093 |
| Use Case | CU76 — Quality Assurance and Testing Infrastructure |
| Branch | `chore/1093_harness_triage_autonomy` |
| Gate 1 status | passed |

## Objetivo

The first autonomous run after #1091, on #1064, stopped in triage after 3 attempts:

- In `triage-retry1` the worker created an OpenSpec file. The scope guard
  reverted it, but `gate_scope` still failed the attempt before the triage gate ran.
- #1064 is a rule about a docs file (`CU-API-MATRIX.csv`). The natural proof is a
  backend test that reads the file, but the triage gate rejects `KIND=code` when
  no Files to Edit path is under a surface root, and the prompt never covers the case.

## What Changes

- `gate_scope` stops failing the attempt: a reverted violation is logged in
  `gates.log` (`REVIEW`), and is prepended to `gate.out` only when the gate fails.
- `triage.env` gets `TEST_SURFACE`, pre-filled `backend`. `adapter.py surfaces`
  returns it when no path is under a surface root.
- `01-triage.md`: how to prove a rule about a repository file (a test that reads
  it), and triage never creates OpenSpec files.

## Reglas de negocio

| Rule | Source | New / Changed / Made explicit |
|------|--------|-------------------------------|
| A violation the harness already reverted does not cost an attempt | AI-SDLC.md (scope guard) | Changed |
| A change whose only edited files are docs may still be proven by tests on a surface | AI-SDLC.md (triage) | New |

## Capabilities

### New Capabilities

None.

### Modified Capabilities

- `ai-sdlc-enforcement`: surface fallback, reverted scope violations.

## Impact Analysis

### Módulos afectados

| Module | Touched | What changes |
|--------|---------|--------------|
| `backend-api` | no | — |
| `frontend` | no | — |
| `notaire-shared` | no | — |
| `infra` / observability | no | — |
| CI/CD (`.github/workflows`) | no | — |
| `local-ai/sdlc` harness | yes | see What Changes |

### Surface area

- Entities: none
- Endpoints: none
- Database (Flyway `V{n}`): none
- Configuration / `.env`: none
- Dependencies: none

### Architecture review

AUDIT §4.1 layers L4 (gates) and L5 (guards). No ADR: the product architecture does not change.

## Documentation Impact

| Permanent document | What must change |
|--------------------|------------------|
| `local-ai/sdlc/AI-SDLC.md` | scope guard row, triage `TEST_SURFACE` |
| `CHANGELOG.md` | n/a — not user visible |

## Out of Scope

Worker-model quality in triage beyond the prompt, and non-JVM check scripts as a surface.
