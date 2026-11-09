# feat: surface Wilson L0 autonomy in evidence report

> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md).

| Field | Value |
|-------|-------|
| GitHub Issue | #2165 |
| Use Case | CU76 |
| Branch | `cursor/feat-2165-report-autonomy-l0-d324` |
| Gate 1 status | passed |

## Objetivo

Embed the #2163 Wilson L0 autonomy grade into `aisdlc report` JSON/markdown/HTML so
operators see honest L0 / no-evaluable / demoted without a separate CLI dig.

## Why

Signal ledger + Wilson L0 exist; the HTML monitor does not show them. Prefer consuming
evidence over Isolator/ForgePort.

## What Changes

- `summarize()` adds `autonomy` via `autonomy_from_runs`
- Report text/JSON print it; HTML adds a read-only Autonomy section

## Reglas de negocio

| Rule | Source | Change |
|------|--------|--------|
| L0 default; never L3 | #2163 | Surfaced |
| skip ≠ pass | #2163 | Unchanged (reuse) |

## Capabilities

### Modified Capabilities

- `telemetry-ui`: Monitor embeds Wilson L0 autonomy from the same summarize payload.

## Impact Analysis

### Módulos afectados

| Module | Touched | What changes |
|--------|---------|--------------|
| `framework/ai-sdlc` | yes | report + HTML autonomy |
| `backend-api` | no | — |
| `frontend` | no | — |

### Write set

- `framework/ai-sdlc/**`
- `docs/openspec/**`

## Documentation Impact

| Document | Change |
|----------|--------|
| `framework/ai-sdlc/README.md` | report includes autonomy |
| CHANGELOG | omit (#2150) |

## Out of Scope

Isolator, ForgePort, sole-committer hook, L2–L4.
