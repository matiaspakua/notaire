# <!-- change title -->

> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md). This proposal documents
> **only this change**; permanent documentation remains the single source of truth.

| Field | Value |
|-------|-------|
| GitHub Issue | #2411 |
| Use Case | CU76 — Quality Assurance and Testing Infrastructure |
| Branch | `feat/2411_stage_kind` |
| Gate 1 status | pending |

## Objetivo

The production-line diagram marks each station as an AI agent, a deterministic step or a human decision. The kit derives the kind from heuristics (agents present → agent, merge → human, else deterministic). A profile should be able to declare the kind explicitly, and `GET /factory/line` should return it.

## What Changes

- `[[stages]]` in the profile accepts an optional `kind = "agent" | "deterministic" | "human"`.
- An unknown `kind` value is a profile validation error.
- When `kind` is omitted, the default follows the current dashboard rule: "agent" when the stage lists agents, "human" when the stage id is "merge", otherwise "deterministic".
- `aisdlc line` and `GET /factory/line` return `kind` for every stage.
- OpenAPI schema `LineStage` gains `kind` as a required enum string; version bumps 1.4.0 → 1.5.0.
- Dashboard `LineStage` interface gains `kind?`; `stationKind(stage)` uses the kit's value.

## Reglas de negocio

| Rule | Source | New / Changed / Made explicit |
|------|--------|-------------------------------|
| Stage kind must be one of "agent", "deterministic", "human" | #2411 / CU76 | New |
| Unknown kind is a profile error | #2411 / CU76 | New |
| Default kind follows existing heuristic when omitted | #2411 / CU76 | Made explicit |

## Capabilities

### New Capabilities

(none)

### Modified Capabilities

- `aisdlc-production-line`: every stage in the line payload carries a `kind` field (agent | deterministic | human) with profile-level declaration and default heuristic.

## Impact Analysis

### Módulos afectados

| Module | Touched | What changes |
|--------|---------|--------------|
| `backend-api` | no | — |
| `frontend` | no | — |
| `frontend-swing` | no | — |
| `infra` / observability | no | — |
| CI/CD (`.github/workflows`) | no | — |
| `framework/ai-sdlc` | yes | profile.py, stages.py, line.py, OpenAPI, dashboard |

### Surface area

- Entities: none
- Endpoints: `GET /factory/line` — additive field `kind` on each LineStage (not breaking)
- Database (Flyway `V{n}`): none
- Configuration / `.env`: none
- Dependencies: none (Python stdlib only; dashboard uses existing vitest)

### Write set

- `framework/ai-sdlc/aisdlc/profile.py`
- `framework/ai-sdlc/aisdlc/stages.py`
- `framework/ai-sdlc/aisdlc/line.py`
- `framework/ai-sdlc/openapi/factory-control.openapi.yaml`
- `framework/ai-sdlc/README.md`
- `framework/ai-sdlc/CHANGELOG.md`
- `framework/ai-sdlc/tests/test_stage_kind.py`
- `framework/ai-sdlc/factory-dashboard/src/ui/model.ts`
- `framework/ai-sdlc/factory-dashboard/src/ui/line-model.ts`
- `framework/ai-sdlc/factory-dashboard/tests/line-model.test.ts`
- `docs/openspec/changes/feat-2411-stage-kind/**`
- `docs/openspec/specs/aisdlc-production-line/spec.md`

### Architecture review

Follows existing patterns: profile validation error style matches `_plugins`/`_ratchets`; `stage_dicts` output shape matches existing keys; OpenAPI additive change matches #2404 precedent. No ADR required.

## Documentation Impact

| Permanent document | What must change |
|--------------------|------------------|
| `framework/ai-sdlc/README.md` | One line where `[[stages]]` keys are documented — add `kind` |
| `framework/ai-sdlc/CHANGELOG.md` | One bullet under [Unreleased] ending with (#2411) |
| `docs/openspec/specs/aisdlc-production-line/spec.md` | Delta spec: new requirement for stage kind |

## Out of Scope

- UI rendering of the kind badge — the dashboard merely exposes `stationKind(stage)`; visual changes are a follow-up if needed.
- Changing the default heuristic — the existing rule is preserved as the fallback.
