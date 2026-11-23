> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — §6 Quality Gates,
> §7 Testing Rules, §11 Release Rules.

## Context

The kit's `[[stages]]` TOML array is parsed in `profile._stage_profiles()` into `StageProfile` dataclass instances. `stage_dicts()` serializes them for config view and line output. The dashboard currently derives station kind from heuristics in the UI layer. We move the kind into the kit so the dashboard consumes it.

## Goals / Non-Goals

**Goals:**
- Profile accepts `kind` on `[[stages]]` with validation
- `build_line()` returns `kind` for every stage
- Dashboard exposes `stationKind(stage)` using the kit value
- OpenAPI contract updated additively

**Non-Goals:**
- Visual changes to the dashboard line view
- Changing the default heuristic logic

## Decisions

1. **`kind` on `StageProfile` is optional (`str | None`)** — when absent, the default heuristic applies. This keeps existing profiles working without migration.
2. **Validation at profile load time** — unknown kind raises `ProfileError`, consistent with how `_plugins` validates kind against `PLUGIN_KINDS`.
3. **Default function in `stages.py`** — `resolve_stage_kind(stage_profile, stage_id)` centralizes the heuristic so both `stage_dicts` and `build_line` use the same logic.
4. **OpenAPI `kind` is a required enum string** — every stage always has a kind (declared or defaulted), so the field is never absent in the output.

## Riesgos / Trade-offs

- [Risk] Existing profiles without `kind` will get the default — no behavior change since the default matches the current dashboard heuristic. → Mitigation: tests cover all three default cases.
- [Risk] Dashboard consumers expecting no `kind` field — additive field, TypeScript uses optional `kind?`. → Mitigation: no breaking change.

## Testing Strategy

TDD is mandatory (Constitution P1): tests are written and observed FAILING before implementation.

| Scenario (spec) | Test level | Test file |
|-----------------|------------|-----------|
| Declared kind is returned | unit | `tests/test_stage_kind.py` |
| Default kind: agents → "agent" | unit | `tests/test_stage_kind.py` |
| Default kind: merge → "human" | unit | `tests/test_stage_kind.py` |
| Default kind: no agents, not merge → "deterministic" | unit | `tests/test_stage_kind.py` |
| Invalid kind is a profile error | unit | `tests/test_stage_kind.py` |
| Dashboard stationKind uses kit kind | unit | `factory-dashboard/tests/line-model.test.ts` |
| Dashboard stationKind falls back to heuristic | unit | `factory-dashboard/tests/line-model.test.ts` |

- Kit tests: `cd framework/ai-sdlc/tests && python3 -m pytest -q`
- Dashboard tests: `cd framework/ai-sdlc/factory-dashboard && pnpm vitest run`

## Regression Strategy

- Existing tests affected: `test_line_stage_metadata.py` — existing assertions on stage keys remain valid; `kind` is an additional key. Tests may need updating if they assert exact key sets.
- Kit suite: `cd framework/ai-sdlc/tests && python3 -m pytest -q`
- Dashboard suite: `cd framework/ai-sdlc/factory-dashboard && pnpm vitest run`
- No Java, no backend-api, no frontend changes.

## Playwright Strategy

n/a — no UI surface changed. The dashboard adds `stationKind(stage)` and `kind?` on the interface; no component rendering changes.

## Deployment Strategy

- Flyway migration required: no
- Deployment order / coupling: none — kit and dashboard deploy together
- Configuration or `.env` keys: none
- Feature flag: no
- Smoke test: `aisdlc line --json` shows `kind` on each stage

## Rollback Strategy

- Revert safe: yes — additive field only; removing it restores prior behavior
- Database rollback: none needed
- Data written under the new behavior after revert: none
- Blast radius if rollback is delayed: none

## Migration Plan

(none — additive change only)

## Open Questions

(none)
