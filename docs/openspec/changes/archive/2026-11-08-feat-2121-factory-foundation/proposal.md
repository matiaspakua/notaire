# Factory foundation: config, plugins, inventory

> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md).

| Field | Value |
|-------|-------|
| GitHub Issue | #2121 |
| Use Case | CU76 — Quality Assurance and Testing Infrastructure (RNF-90) |
| Branch | `cursor/ai-sdlc-factory-foundation-b7c4` |
| Gate 1 status | passed |

## Objetivo

The portable kit and the adopter harnesses disagree about what is core. This change adds a validated config view, a plugin list that does not execute commands, and an inventory that says what was not moved.

## What Changes

- `config validate`, `config show --redacted`, `plugin list`, `plugin capabilities`, `plugin plan`, `inventory check` on the existing `aisdlc` CLI.
- OpenSpec as an argv adapter. An example method plugin. Spec Kit and AWS AI-DLC files marked planned and not loaded.
- `.aisdlc/migration-inventory.toml` and three ADRs. No product source, product OpenAPI, or skill bodies are moved.
- The profile pull-request cap is a ceiling.

## Reglas de negocio

| Rule | Source | New / Changed / Made explicit |
|------|--------|-------------------------------|
| The Owner merges policy and harness changes | CU76, CONSTITUTION | Unchanged |
| Open pull requests stay at or under the profile cap | #2121 | Made explicit as a ceiling, not a distributed lock |
| A plugin list does not run plugin commands | #2121 | New |
| Product contracts stay with the product | #2121 | Made explicit as inventory class stay |

## Capabilities

### New Capabilities

- `factory-foundation`: config, plugins, inventory honesty

### Modified Capabilities

- None

## Impact Analysis

### Módulos afectados

| Module | Touched | What changes |
|--------|---------|--------------|
| `backend-api` | no | — |
| `frontend` | no | — |
| `frontend-swing` | no | — |
| `infra` / observability | no | — |
| CI/CD (`.github/workflows`) | no | Release-only CI unchanged |

### Surface area

- Entities: none
- Endpoints: none
- Database (Flyway `V{n}`): none
- Configuration / `.env`: none. Secrets stay `env:` references.
- Dependencies: none. The kit remains the Python standard library.

### Write set

- `framework/ai-sdlc/**`
- `.aisdlc/aisdlc.toml`
- `.aisdlc/migration-inventory.toml`
- `workspace/tests/test_migration_inventory.py`
- `docs/300-development/306-ai-sdlc-framework/FACTORY-FOUNDATION.md`
- `docs/300-development/306-ai-sdlc-framework/ai_sdlc_framework.md`
- `docs/openspec/changes/feat-2121-factory-foundation/**`
- `CHANGELOG.md`

### Architecture review

ADRs under `framework/ai-sdlc/docs/adr/`. No product ADR. The canonical root is the existing kit.

## Documentation Impact

| Permanent document | What must change |
|--------------------|------------------|
| `framework/ai-sdlc/README.md` | New commands |
| `docs/300-development/306-ai-sdlc-framework/FACTORY-FOUNDATION.md` | New pointer |
| `CHANGELOG.md` | One unreleased line |

## Out of Scope

Moving `workspace/sdlc` or `local-ai/sdlc`. Product UI and the unpublished people-form branch. A new repository. An HTTP server. An OpenTelemetry SDK. A feature-flag SDK. Claiming a sandbox.
