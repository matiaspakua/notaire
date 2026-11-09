# feat: Wilson L0 grade from local signal ledger

> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md). This proposal documents
> **only this change**; permanent documentation remains the single source of truth.

| Field | Value |
|-------|-------|
| GitHub Issue | #2163 |
| Use Case | CU76 |
| Branch | `cursor/feat-2163-wilson-l0-evidence-d324` |
| Gate 1 status | passed |

## Objetivo

Make `aisdlc improve autonomy` honest: L0 by default, no fake L3, first incident → L1,
skip ≠ pass, graded from required gates in `signal` stage evidence that preflight feeds.

## Why

After #2159/#2160 the ledger has real `signal` rows. Current autonomy can report level 3
and treat stage `ok` (where skip counts toward stage success) as a clean pass.

## What Changes

- Autonomy grades **required** gates in `stage=signal` runs only.
- Skip/todo excluded from Wilson trials (skip ≠ pass).
- Levels: default / eligible = **0**; demoted = **1**; never claim 3.
- Docs + kit tests updated; README notes L0.

## Reglas de negocio

| Rule | Source | New / Changed / Made explicit |
|------|--------|-------------------------------|
| L0 default; no fake L3 | factory axioms / #2163 | Changed |
| First incident → L1 | axiom 7 | Unchanged |
| skip ≠ pass | kit axioms | Made explicit in autonomy |

## Capabilities

### New Capabilities

### Modified Capabilities

- `self-improve`: Wilson autonomy is L0-honest from signal ledger gates.

## Impact Analysis

### Módulos afectados

| Module | Touched | What changes |
|--------|---------|--------------|
| `framework/ai-sdlc` | yes | improve.autonomy |
| docs OpenSpec | yes | self-improve delta |

### Write set

- `framework/ai-sdlc/**`
- `docs/openspec/**`
- `docs/300-development/306-ai-sdlc-framework/**` (thin pointer if needed)

## Documentation Impact

| Permanent document | What must change |
|--------------------|------------------|
| `framework/ai-sdlc/README.md` | autonomy = L0 / demote L1 |
| `CHANGELOG.md` | omit Unreleased (#2150) |

## Out of Scope

L2–L4 ladder, Isolator, ForgePort, portal UI.
