> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — §6 Quality Gates,
> §7 Testing Rules, §11 Release Rules.

## Context

ApprovalPort design (#2190 / ADR-005) and Isolator design (#2173 / ADR-004) shipped as
unevaluable stubs. Remaining gaps call for Grant **profile data** before Isolator OS.
Stage knobs (#2182) established the validate/list/show pattern this slice mirrors.

## Goals / Non-Goals

**Goals:** Typed optional `[[grants]]` in the profile; fail-closed check refs; CLI list/show;
config show surface; Notaire sample or documented empty; Isolator honesty preserved.
**Non-Goals:** OS Isolator, grant enforcement/expire, ApprovalPort runtime, flipping
`committer.required`, Wilson promotion.

## Decisions

1. **`[[grants]]` array of tables** — fields: `id` (required), `worktree` (string),
   `write_set` (string list), `required_checks` (string list of gate **names**),
   `budget` (same units as stage `effort`: tokens, time_seconds, questions, review_lines),
   optional `stage` (must match a gate stage when set).
2. **Omit = no-op** — empty/absent grants never fail doctor/validate.
3. **Fail closed on unknown checks** — when `required_checks` is non-empty, each name must
   match a declared `[[gates]].name`; unknown → profile load or validate error.
4. **CLI** — `aisdlc grant list [--json]` and `aisdlc grant show --id <id> [--json]`; also
   include `grants` in `config show --redacted`.
5. **Isolator unchanged** — `isolator_status()` stays `no-evaluable` / `sandbox=false`;
   grants are data, not a sandbox claim (ADR-004 amendment).
6. **Notaire sample** — one optional Grant bound to `plan` stage with a real gate name and
   conservative write_set; keep `committer.required = false`.

## Riesgos / Trade-offs

Readers treat Grant TOML as Isolator shipped → mitigated by ADR-004 language, doctor Isolator
INFO, and grant CLI docs stating validate/list/show only.

## Testing Strategy

| Scenario (spec) | Test level | Test class / file |
|-----------------|------------|-------------------|
| omitted grants are no-op | unit | `framework/ai-sdlc/tests/test_grants.py` |
| parse grant fields | unit | `test_grants.py` |
| unknown required_checks fails | unit | `test_grants.py` |
| grant list/show json | unit | `test_grants.py` |
| isolator stays no-evaluable with grants | unit | `test_grants.py` / isolator assertions |
| Notaire doctor green with sample | unit/kit | lifecycle/doctor against Notaire profile |

- New unit tests: kit unittest discover under `framework/ai-sdlc/tests/`
- New integration tests: n/a (no Java/API)
- Coverage impact: kit Python only; JaCoCo n/a

## Regression Strategy

- Existing tests affected: `test_isolator.py`, `test_factory.py` (config show keys),
  `test_contract.py` if FactoryConfig required keys change (keep grants optional)
- Full suite: kit unittest + `bash workspace/sdlc/preflight.sh --full`
- HTTP/Bruno: n/a for this write set
- Legacy paths at risk: none

## Playwright Strategy

n/a — no UI surface (kit profile / CLI only).

## Deployment Strategy

- Flyway migration required: no
- Deployment order / coupling: ships with next release via merge to main
- Configuration: optional `[[grants]]` in adopter profile
- Feature flag: no
- Smoke test after deploy (Gate 5): `aisdlc doctor` + `aisdlc grant list --json` on Notaire

## Rollback Strategy

- Revert safe: yes (additive profile schema)
- Database rollback: none needed
- Data written under the new behavior after revert: none
- Blast radius if rollback is delayed: low (list/validate only)

## Migration Plan

None beyond optional Notaire sample Grant in `.aisdlc/aisdlc.toml`.

## Open Questions

None.
