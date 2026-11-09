# feat: Notaire opt-in sole-committer gate

> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md).

| Field | Value |
|-------|-------|
| GitHub Issue | #2169 |
| Use Case | CU76 |
| Branch | `cursor/feat-2169-committer-opt-in-d324` |
| Gate 1 status | passed |

## Objetivo

Make sole-committer dry-run **no-evaluable** until a runtime committer identity is
configured; wire an opt-in Notaire gate; document axiom-2 ≠ Isolator.

## Why

#2168 left agent deny-lists active without `runtime_*`, so every Cursor Agent tip
is advisory-fail. Greenfield PRs must stay no-evaluable until Owner configures runtime.

## What Changes

- Kit: evaluable only when `runtime_names` or `runtime_emails` is set
- Notaire: advisory `[[gates]]` for `aisdlc committer`; keep agent patterns as prep
- FACTORY-FOUNDATION note

## Reglas de negocio

| Rule | Source | Change |
|------|--------|--------|
| no-evaluable until runtime identity | #2169 | Changed |
| Never claim Isolator/sandbox | #2167 | Unchanged |

## Capabilities

### Modified Capabilities

- `sole-committer`: Evaluable only with runtime allowlist; Notaire opt-in gate.

## Impact Analysis

### Módulos afectados

| Module | Touched | What changes |
|--------|---------|--------------|
| `framework/ai-sdlc` | yes | CommitterPolicy.evaluable |
| `.aisdlc` | yes | gate + comments |
| docs | yes | FACTORY-FOUNDATION |

### Write set

- `framework/ai-sdlc/**`
- `.aisdlc/aisdlc.toml`
- `workspace/tests/**`
- `docs/300-development/306-ai-sdlc-framework/**`
- `docs/openspec/**`

## Documentation Impact

| Document | Change |
|----------|--------|
| `FACTORY-FOUNDATION.md` | axiom-2 policy evidence ≠ isolation |
| CHANGELOG | omit (#2150) |

## Out of Scope

`required = true`; OS Isolator.
