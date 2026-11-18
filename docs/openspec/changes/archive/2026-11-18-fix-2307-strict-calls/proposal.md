# fix: do not guess among same-directory methods that share a name

> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md).

| Field | Value |
|-------|-------|
| GitHub Issue | #2307 |
| Use Case | CU76 — Quality Assurance and Testing Infrastructure |
| Branch | `cursor/2307-strict-calls-47dc` |
| Gate 1 status | passed |

## Objetivo

`impact Folio.getDto` listed `History.getDto` even though that method does not call `Folio.getDto`. The linker attached a strict call to the first three same-directory methods of that name. An ambiguous same-directory call is no longer guessed. A uniquely named method still links.

## What Changes

- `Linker._scope` returns a same-directory match for a strict call only when that match is unique.
- Version 0.4.2.
- Factory comparison rows for this round's paired locates.

## Reglas de negocio

| Rule | Source | New / Changed / Made explicit |
|------|--------|-------------------------------|
| An ambiguous `getDto` call is not evidence that every nearby `getDto` is a caller | #2307 | New |

## Capabilities

### New Capabilities

- None.

### Modified Capabilities

- None. `skip_specs: true`.

## Impact Analysis

### Módulos afectados

| Module | Touched | What changes |
|--------|---------|--------------|
| `framework/ai-sdlc/repolens` | yes | Linker |
| `backend-api` | no | |
| `frontend` | no | |

### Surface area

- Product API: none

### Write set

- `repolens/linker.py`
- `tests/test_graph.py`
- CHANGELOG, version, comparison, learning log

### Architecture review

Factory tooling. No product ADR.

## Documentation Impact

| Permanent document | What must change |
|--------------------|------------------|
| `CHANGELOG.md` | 0.4.2 |
| Factory comparison and learning log | this round |
| Product docs | none |

## Out of Scope

- Resolving the receiver's Java type, which the extractor does not record.
- A Vitest retry inside `preflight.sh`.
