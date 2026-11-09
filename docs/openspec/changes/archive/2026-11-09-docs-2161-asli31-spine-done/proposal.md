# docs: ASLI-31 mark factory spine done

> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md). This proposal documents
> **only this change**; permanent documentation remains the single source of truth.

| Field | Value |
|-------|-------|
| GitHub Issue | #2161 |
| Use Case | CU76 |
| Branch | `cursor/docs-2161-asli31-spine-done-d324` |
| Gate 1 status | passed |

## Objetivo

Epic #2134 child spine (#2125–#2142) plus validation fixes (#2153/#2155/#2157)
and preflight evidence (#2159) are on `main`. FACTORY-ROADMAP still presents an
Active spine and tells sessions to pick #2125.

## Why

Close the documentation gap so later sessions do not reopen finished P0–P3 work.

## What Changes

- §7: mark P0–P3 Active spine done; land #2125–#2142 and note #2153/#2155/#2157/#2159.
- Session continue pointers + FACTORY-FOUNDATION status.
- Close epic #2134 after merge (or paste if permissions block).

## Reglas de negocio

| Rule | Source | New / Changed / Made explicit |
|------|--------|-------------------------------|
| Spine complete ≠ Wilson autonomy shipped | factory deferred list | Made explicit |

## Capabilities

### New Capabilities

- `factory-roadmap`: In-repo mirror states #2134 P0–P3 spine complete.

### Modified Capabilities

## Impact Analysis

### Módulos afectados

| Module | Touched | What changes |
|--------|---------|--------------|
| docs | yes | FACTORY-ROADMAP, FACTORY-FOUNDATION |
| workspace tests | yes | roadmap assertions |

### Write set

- `docs/300-development/306-ai-sdlc-framework/**`
- `workspace/tests/test_factory_roadmap.py`
- `docs/openspec/**`

## Documentation Impact

| Permanent document | What must change |
|--------------------|------------------|
| `FACTORY-ROADMAP.md` | spine done |
| `FACTORY-FOUNDATION.md` | ASLI-31 / epic closed pointer |

## Out of Scope

Implementing deferred Isolator / Wilson / ForgePort.
