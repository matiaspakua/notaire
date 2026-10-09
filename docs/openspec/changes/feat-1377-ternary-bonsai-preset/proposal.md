# Ternary Bonsai preset and a model registry for the local AI harness

> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md). This proposal documents
> **only this change**; permanent documentation remains the single source of truth.

| Field | Value |
|-------|-------|
| GitHub Issue | #1377 |
| Use Case | CU76 – Quality Assurance and Testing Infrastructure (local AI SDLC harness) |
| Branch | `feat/1377_add-ternary-bonsai-local-ai-model` |
| Gate 1 status | draft |

## Objetivo

The local AI harness selects its model from hard-coded `case` presets in `local-ai/setup-omlx-codex.sh`, so adding a model means editing shell. This change makes `local-ai/models.yaml` the single registry and adds the Ternary Bonsai 2 27B preset.

## What Changes

- `local-ai/models.yaml`: registry of models (repository, context, sampling, reasoning, Codex profile, patch flag, summary) including `qwen3-coder`, `gpt-oss` and `ternary-bonsai`.
- `local-ai/models_config.py`: resolves a preset key or a repository id into the shell variables the setup script evaluates.
- `local-ai/setup-omlx-codex.sh`: reads the registry; no preset is hard-coded.
- `local-ai/opencode/opencode.json` and the Codex profile `omlx-bonsai` for the new model.

## Reglas de negocio

| Rule | Source | New / Changed / Made explicit |
|------|--------|-------------------------------|
| Each model's tuning (temperature, repetition penalty, KV cache, reasoning) lives with the model entry, not in the script | #1099, #1107 | Made explicit |

## Capabilities

### New Capabilities

- `local-ai-model-registry`: models are declared once and resolved by key or repository.

### Modified Capabilities

- (none)

## Impact Analysis

### Módulos afectados

| Module | Touched | What changes |
|--------|---------|--------------|
| `local-ai` | yes | registry, resolver, setup script, profiles |
| Everything else | no | — |

### Surface area

- Endpoints, entities, Flyway, product dependencies: none
- Risk: a changed resolved value would silently change the model the harness runs. Mitigation: the resolver is tested against the values the old script hard-coded for `qwen3-coder` and `gpt-oss`.

### Architecture review

No architectural change; local tooling only.

## Documentation Impact

| Permanent document | What must change |
|--------------------|------------------|
| `local-ai/sdlc/AI-SDLC.md` | model selection by registry key |
| `CHANGELOG.md` | one entry |
