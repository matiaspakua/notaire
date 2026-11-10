# Step the public diagrams as the factory is on main

> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md). This proposal documents
> **only this change**.

| Field | Value |
|-------|-------|
| GitHub Issue | #2202 |
| Use Case | CU76 — Quality Assurance and Testing Infrastructure |
| Branch | `cursor/pages-diagrams-motion-2277` |
| Gate 1 status | passed |

## Objetivo

A reader can step through the public diagrams from a simple claim to the real factory: one change, the gates, and the numbers the snapshot actually stores.

## What Changes

- The factory flow no longer says only the Owner merges. The Foreman may merge a Worker pull request. The Owner merges protected paths. Workers never merge. `preflight.sh --full` is the pull-request proof. Product CI is the version tag. `aisdlc foreman plan` does not execute. The plugin allow list is openspec and example-method.
- Quality gates can be stepped, then seen as one map.
- Lead time is the snapshot median and p90 on a scale. The snapshot is not a histogram.
- Flow diagrams share one key. Node labels go through the Spanish dictionary. Reduced motion still changes steps instantly and does not move layout.

## Reglas de negocio

| Rule | Source | New / Changed / Made explicit |
|------|--------|-------------------------------|
| Public numbers come from the snapshot. Nothing is invented. | CONSTITUTION.md §7 | Made explicit |
| Merge authority is Foreman or Owner, never a Worker | CONSTITUTION.md §10 | Made explicit |
| English technical terms stay in English | #2202 | Made explicit |

## Capabilities

### New Capabilities

### Modified Capabilities

- `github-pages-ai-factory`: the factory diagram and the quality-gate story match main.

## Impact Analysis

### Módulos afectados

| Module | Touched | What changes |
|--------|---------|--------------|
| github-page | yes | Diagram data, lead-time figure, tests |
| docs | yes | This change, then the archived spec |

No backend or schema change.

## Documentation Impact

- `CHANGELOG.md` — `[Unreleased]`.
- OpenSpec spec `github-pages-ai-factory` when this change is archived.
