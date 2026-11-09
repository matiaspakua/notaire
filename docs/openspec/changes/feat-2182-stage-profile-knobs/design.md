> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — §6 Quality Gates,
> §7 Testing Rules, §11 Release Rules.

## Context

#2139 shipped catalogs CRUD; stage→catalog refs were left unfinished. Gap audit
ASLI-44 ranked per-stage knobs as the next CU76 slice. Profile already owns
gates/stages; catalogs already list skills-dir overlay.

## Goals / Non-Goals

**Goals:**
- First-class optional `[[stages]]` knobs in the profile
- Fail-closed catalog/skills-dir resolution on validate + doctor
- Surface via `config show --redacted` and `stage list|--json` without execution
- Notaire maps ≥1 real stage by catalog/path reference

**Non-Goals:**
- Isolator / sandbox claims; `required=true`; portal UI; executing MCP/agents;
  Wilson vanity stages; ForgePort write

## Decisions

1. **`[[stages]]` array-of-tables** — matches existing `[[gates]]` style; `id`
   must equal a gate stage already declared (unknown stage → ProfileError at load).
2. **Effort as inline table** — `effort = { tokens, time_seconds, questions,
   review_lines }`; all optional non-negative numbers; units documented in README.
3. **Resolution at validate/doctor, not silent load** — profile load accepts
   structural shape; `validate_stage_profiles(ctx)` resolves ids via
   `catalogs.list_entries` (skills-dir overlay included). Missing catalogs dir
   with agent/mcp refs (or unresolved skills) → error.
4. **`aisdlc stage list|--json`** — new top-level command (not `gate --list`);
   never runs gates or MCP. `config_view` gains optional `stages` key.
5. **Notaire adopter** — seed minimal `.aisdlc/catalogs/agents/openspec-planner.json`
   with `ref` to `.claude/agents/openspec-planner.md`; `[[stages]]` for `plan`
   references that agent + skills-dir skill ids from fleet-manifest. No product
   paths inside `framework/ai-sdlc/`.

## Riesgos / Trade-offs

- [Stage id typos] → fail at profile load against declared gate stages.
- [Catalog empty + skill-only refs] → skills-dir overlay keeps plan stage usable.
- [Foreman still reads fleet-manifest] → knobs are the kit SoT; fleet YAML remains
  harness-side until a later wiring slice — no Isolator claim.

## Testing Strategy

| Scenario (spec) | Test level | Test class / file |
|-----------------|------------|-------------------|
| omitted stage knobs are no-op | unit (CLI) | `framework/ai-sdlc/tests/test_stage_profiles.py` |
| parse agents skills mcp model effort | unit | same |
| unknown stage id fails load | unit | same |
| unknown catalog id fails validate | unit | same |
| unknown id fails doctor | unit | same |
| skills-dir overlay resolves skill id | unit | same |
| missing catalogs dir with agent ref fails | unit | same |
| stage list json never executes | unit | same |
| config show redacted surfaces stages | unit | same |

- New unit tests: `test_stage_profiles.py` only (stdlib unittest via kit helpers)
- Coverage impact: kit suite only; no JaCoCo/Vitest change

## Regression Strategy

- Existing tests affected: `test_contract.py` if FactoryConfig keys grow (optional
  `stages` must not break required-key check); `test_catalogs.py` unchanged.
- Full suite command: `python3 -m unittest discover -s framework/ai-sdlc/tests`
- Notaire: `aisdlc config validate` + `doctor` + `stage list --json`
- HTTP/Bruno: n/a
- Legacy paths: n/a

## Playwright Strategy

n/a — no UI surface (kit CLI + profile only).

## Deployment Strategy

- Flyway migration required: no
- Deployment order / coupling: ships with next release; profile additive
- Configuration: optional `[[stages]]`; Notaire seeds catalogs agents entry
- Feature flag: no
- Smoke test after deploy (Gate 5): `aisdlc doctor` + `config validate` on Notaire

## Rollback Strategy

Revert the PR; remove `[[stages]]` / catalog seed from `.aisdlc/` if needed.
Safe: additive schema, no DB, no Isolator coupling.

## Migration Plan

Adopters with no `[[stages]]` need zero changes. Adopters adding knobs must
ensure catalog/skills-dir ids exist before validate/doctor go green.

## Open Questions

None — effort units fixed in this design; Owner may extend later.
