> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — §6 Quality Gates,
> §7 Testing Rules, §11 Release Rules.

## Context

`line.py`'s `build_line()` currently emits per-stage dicts with only `id, gates, inputs, outputs, io_source`. The shared contract (#2393 Line section C) and the OpenAPI schema already declare that stages carry agents, skills, mcp, tools, model, effort, and guardrails — but the implementation does not populate them. `stage_dicts(profile)` in `stages.py` already returns all of these keys for stages with a `[[stages]]` block. `profile.grants` already holds the `[[grants]]` entries, each with an optional `stage` field. The gap is purely compositional: `build_line` must merge these sources into every stage dict.

## Goals / Non-Goals

**Goals:**

- Every stage in `build_line()` output carries all keys: `id, gates, agents, skills, mcp, tools, model, effort, guardrails, inputs, outputs, io_source` — never missing.
- `stage_dicts(profile)` is the single source of truth for agents/skills/mcp/model/effort; `build_line` reuses it.
- Per-stage guardrails derived from `[[grants]]` entries matching the stage id.
- Gate-only stages (no `[[stages]]` block) get empty lists, `null` model, `{}` effort.
- OpenAPI `LineStage` schema updated with full `required` list and `guardrails` array property.
- Contract `info.version` minor bump (1.3.0 → 1.4.0).

**Non-Goals:**

- Changing the top-level `guardrails` block shape.
- The dashboard (#2400) config-merge workaround removal — out of this write set, follow-up in the dashboard branch.
- Any new profile keys or schema changes to the TOML.

## Decisions

### D1 — Reuse `stage_dicts(profile)` as the single source of truth

`build_line` already builds `sd_by_id = {row["id"]: row for row in stage_dicts(profile)}`. That lookup is extended: for each stage, if `sd_by_id[sid]` exists, its `agents`, `skills`, `mcp`, `model`, `effort` are used directly. If it does not exist (gate-only stage), defaults are applied.

Rationale: `stage_dicts` is the function used by `GET /factory/config` stages[] and by `aisdlc stage list`. Reusing it guarantees consistency across all endpoints.

### D2 — Key set and empty values

Every stage dict ALWAYS has these keys: `agents` (list), `skills` (list), `mcp` (list), `tools` (list), `model` (string or null), `effort` (object), `guardrails` (list). When a stage has no `[[stages]]` block: `agents=[]`, `skills=[]`, `mcp=[]`, `tools=[]`, `model=null`, `effort={}`, `guardrails=[]`. The `effort` key is `{}` (empty object), never `null`, when no effort is declared.

Rationale: consumers (dashboard, CLI) must not check for key presence — they read the value directly. Empty collections and `{}` are the natural "none declared" values.

### D3 — `tools` equals `mcp` ids because the profile has no other tool source

The profile stores MCP server references in `stage.mcp`. There is no separate `tools` field in the TOML. The line payload exposes both `mcp` and `tools`, where `tools` is a copy of `mcp`.

Rationale: the shared contract (#2393) uses "tools/MCP" terminology. The dashboard and CLI may refer to either name. Duplicating the list avoids a separate tool source that does not exist.

### D4 — Per-stage guardrails = grants of the stage because the profile has no other per-stage guardrail source

Per-stage `guardrails` is the list of `[[grants]]` entries whose `stage` field equals the stage id, serialized with `aisdlc/grants.py grant_dict` plus `"kind": "grant"` injected. When no grants match, the list is empty.

The profile has no other per-stage guardrail source. The top-level `guardrails` block (protected paths, hooks, rules, isolator) is payload-level, not per-stage.

Rationale: `[[grants]]` is the only profile construct that associates constraints with a specific stage. Using `grant_dict` ensures consistent serialization with `GET /factory/config` grants[].

### D5 — Gate-only stages get empty values

A stage that appears only because it has gates (no `[[stages]]` block) gets: `agents=[]`, `skills=[]`, `mcp=[]`, `tools=[]`, `model=null`, `effort={}`, `guardrails=[]` (unless a `[[grants]]` entry targets it).

Rationale: these stages are real pipeline steps; they just have no AI configuration. Empty values are more honest than omitting keys.

### D6 — OpenAPI additive change: LineStage required list and guardrails array

`LineStage.required` is expanded to `[id, gates, agents, skills, mcp, tools, model, effort, guardrails, inputs, outputs, io_source]`. A new `guardrails` property (array of objects) is added. Contract `info.version` bumps from 1.3.0 to 1.4.0 (minor, additive).

Rationale: the schema must reflect that these keys are always present. The `guardrails` array on LineStage is distinct from the top-level `LineGuardrails` object.

## Riesgos / Trade-offs

- [Dashboard depends on the current shape] The dashboard (#2400) merges `GET /factory/config` stages[] into the line payload as a workaround. Once this fix lands, the dashboard can drop that merge. The dashboard branch is tracked separately; this fix does not break the dashboard because the new keys are additive.
- [Existing tests assert the old shape] `test_line*.py` tests that check stage dicts will need to assert the new keys. No assertion is weakened — only new assertions are added.

## Testing Strategy

| Scenario (spec) | Test level | Test class / file |
|-----------------|------------|-------------------|
| Stage always carries all keys with a configured stage | unit | `tests/test_line_stage_metadata.py` |
| Gate-only stage carries empty values | unit | `tests/test_line_stage_metadata.py` |
| Stage guardrails list grants of the stage | unit | `tests/test_line_stage_metadata.py` |
| Real profile shape with agents, skills, model, effort and mcp | unit | `tests/test_line_stage_metadata.py` |

- New unit tests: `framework/ai-sdlc/tests/test_line_stage_metadata.py`
- New integration tests: none (stdlib-only kit; unit tests cover the full stack)
- Coverage impact: additive; kit's existing floor is not lowered

## Regression Strategy

- `build_line` output shape changes additively: existing callers that read `id`, `gates`, `inputs`, `outputs`, `io_source` are unaffected.
- Existing `tests/test_line.py` tests that inspect stage dicts may need updated assertions for the new keys. No assertion is weakened.
- Full suite command: `python3 -m pytest framework/ai-sdlc/tests/`
- HTTP/Bruno API suite: n/a (kit-level tests only)
- Legacy paths at risk: none

## Playwright Strategy

n/a — no UI surface. This change adds keys to a JSON payload consumed by the CLI and the dashboard.

## Deployment Strategy

- Flyway migration required: no
- Deployment order / coupling: none; the kit is a Python package used locally
- Configuration or `.env` keys to add: none
- Feature flag: no
- Smoke test after deploy (Gate 5): `aisdlc line --json` exits 0 and every stage has all required keys

## Rollback Strategy

- Revert safe: yes — the change is additive (new keys on existing stage dicts). Reverting removes the new keys without affecting existing behavior.
- Database rollback: none needed
- Data written under the new behavior after revert: none
- Blast radius if rollback is delayed: none; read-only surface

## Migration Plan

No staged rollout required. Additive change; deploy as a single commit.

## Open Questions

None — all decisions are captured above.
