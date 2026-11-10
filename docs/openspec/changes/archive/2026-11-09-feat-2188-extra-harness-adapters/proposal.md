# Extra HarnessPort adapters (Claude Code / Codex / ACP)

> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md). This proposal documents
> **only this change**; permanent documentation remains the single source of truth.

| Field | Value |
|-------|-------|
| GitHub Issue | #2188 |
| Use Case | CU76 — Quality Assurance and Testing Infrastructure |
| Branch | `cursor/feat-2188-harness-stubs-7c58` |
| Gate 1 status | passed |

## Objetivo

HarnessPort today only surfaces Cursor Cloud and the local oMLX foreman. Gap audit
rank 4 needs discoverable inventory stubs for Claude Code, OpenAI Codex, and Agent
Client Protocol (ACP) so the kit is portable — without executing remote agents and
without claiming OS isolation (`sandbox=false` forever until a real Isolator).

## What Changes

Add three harness-kind plugin stubs under `framework/ai-sdlc/plugins/` with adapter
YAML. Extend `aisdlc harness list` / `show` to surface harness-kind plugins from the
profile (including opt-out / unavailable ones) always with `sandbox=false`. Notaire
declares them with `trusted=false` and leaves them out of `[plugins] allow` (Spec Kit
pattern). Dry `plugin plan` prints describe argv only when allowed; never runs a runtime.

## Reglas de negocio

| Rule | Source | New / Changed / Made explicit |
|------|--------|-------------------------------|
| Extra harness stubs are inventory + dry plan only; YAML/permissions are never a sandbox | CU76 / ADR-002 / #2188 | New |
| Notaire may leave stubs out of `[plugins] allow` until Owner opts in | Spec Kit #2123 pattern | Made explicit for harness kind |

## Capabilities

### New Capabilities

- `extra-harness-stubs`: Claude Code / Codex / ACP discoverable via harness list with sandbox=false

### Modified Capabilities

- `harness-port`: list/show also surface harness-kind plugin stubs from the profile

## Impact Analysis

### Módulos afectados

| Module | Touched | What changes |
|--------|---------|--------------|
| `backend-api` | no | — |
| `frontend` | no | — |
| `frontend-swing` | no | — |
| `infra` / observability | no | — |
| CI/CD (`.github/workflows`) | no | — |
| `workspace` (harness) | yes | aisdlc harness/plugins, inventory, kit tests, adopter tests, docs |

### Surface area

- Entities: none
- Endpoints: none (OpenAPI HarnessRow stays additionalProperties)
- Database (Flyway `V{n}`): none
- Configuration / `.env`: none
- Dependencies: none

### Write set

- `framework/ai-sdlc/**`
- `.aisdlc/aisdlc.toml`
- `.aisdlc/migration-inventory.toml`
- `workspace/tests/test_harness_adoption.py`
- `docs/300-development/306-ai-sdlc-framework/FACTORY-FOUNDATION.md`
- `docs/300-development/306-ai-sdlc-framework/FACTORY-ROADMAP.md`
- `docs/openspec/changes/feat-2188-extra-harness-adapters/**`
- `docs/openspec/specs/harness-port/spec.md` (via archive)

### Architecture review

Follows ADR-002; no new ADR (Isolator remains ADR-004 stub).

## Documentation Impact

| Permanent document | What must change |
|--------------------|------------------|
| `docs/300-development/306-ai-sdlc-framework/FACTORY-FOUNDATION.md` | Document three stub harness plugins + opt-in |
| `docs/300-development/306-ai-sdlc-framework/FACTORY-ROADMAP.md` | Note rank-4 slice landed |
| `framework/ai-sdlc/README.md` | Harness stubs / sandbox=false |
| `framework/ai-sdlc/docs/adr/ADR-002-ports-and-trust.md` | Mention extra stubs |
| `CHANGELOG.md` | n/a — release-please owns Unreleased |

## Out of Scope

- Isolator OS runtime / `sandbox=true` / fake isolation
- `[committer] required = true`
- Launching Claude Code, Codex, or ACP from the kit
- Portal / Context Lake / ForgePort write
- Replacing cursor-cloud or local-foreman
