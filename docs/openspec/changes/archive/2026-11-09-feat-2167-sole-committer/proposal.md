# feat: sole-committer dry-run policy check

> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md).

| Field | Value |
|-------|-------|
| GitHub Issue | #2167 |
| Use Case | CU76 |
| Branch | `cursor/feat-2167-sole-committer-d324` |
| Gate 1 status | passed |

## Objetivo

Detect agent-authored commits on a branch via a profile-driven dry-run. Enforce
exit fail only when `[committer] required = true`. Never claim OS isolation.

## Why

Axiom 2 needs a checkable surface before a real Isolator. Prefer evidence/policy
over a fake sandbox.

## What Changes

- Profile `[committer]` (runtime allowlist, agent deny patterns, required flag)
- `aisdlc committer [--base] [--json]` → pass / fail / no-evaluable
- Kit tests; Notaire profile with `required = false` + known bot patterns

## Reglas de negocio

| Rule | Source | Change |
|------|--------|--------|
| Agent never commits | axiom 2 | Made mechanical (dry-run) |
| Fail-closed only when required | #2167 | New |
| Never claim sandbox | ADR-002 | Made explicit in CLI output |

## Capabilities

### New Capabilities

- `sole-committer`: Dry-run committer policy check.

## Impact Analysis

### Módulos afectados

| Module | Touched | What changes |
|--------|---------|--------------|
| `framework/ai-sdlc` | yes | committer CLI + profile |
| `.aisdlc` | yes | optional `[committer]` |

### Write set

- `framework/ai-sdlc/**`
- `.aisdlc/aisdlc.toml`
- `docs/openspec/**`

## Documentation Impact

| Document | Change |
|----------|--------|
| `framework/ai-sdlc/README.md` | document `committer` |
| CHANGELOG | omit (#2150) |

## Out of Scope

OS Isolator; history rewrite; forge writes; portal.
