> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — §6 Quality Gates,
> §7 Testing Rules, §11 Release Rules.

## Context

#2214 made Foreman unmapped stages honest. Notaire still declares only
`[[stages]] id = "plan"`. Fleet roles/skills already exist
(`fleet-manifest.yaml`, `.claude/agents/`, skills-dir); agent catalog today has
only `openspec-planner`. Canonical kit stage `green` has **no** Notaire gate —
G3 verified is split into `pre-push` and `pre-pr`.

## Goals / Non-Goals

**Goals:**
- `foreman plan --json` `mapped_count` ≥ 4 with real knobs (`plan`, `red`,
  `pre-push`, `pre-pr`)
- Agent catalog entries for every agent id referenced
- Skills from skills-dir / fleet; empty MCP; models from fleet defaults
- Document green omission + remaining unmapped honesty in FACTORY-ROADMAP
- Doctor clean (`signal` alias preserved); `[committer] required` untouched

**Non-Goals:**
- Wilson L1, portal, inventing MCP/agents, flipping `required=true`
- Adding a vanity `green` gate solely to map a knob
- Kit schema changes (unless a Notaire verify assertion needs a tiny test)

## Decisions

1. **Map existing gate stages only.** `[[stages]]` ids must match declared gates
   (`profile._stage_profiles`). No `green` knob without a green gate.
2. **pre-push stands in for TDD/G3 local verify.** Document that Notaire does not
   use a separate `green` gate; operators route “make tests/gates pass locally”
   via `pre-push` knobs + implementer roles on `red`.
3. **Lean primary agents per stage** (match `plan` style):
   - `red` → `testing-qa` (+ `backend-implementer` for red-bar ownership)
   - `pre-push` → `cloud-foreman` (gate orchestration / preflight)
   - `pre-pr` → `cloud-foreman` (+ `code-reviewer` for review-budget awareness)
4. **Skills from fleet-manifest mapped to skills-dir ids** (`test-master`,
   `hexagonal-architecture`, `senior-devops`, etc.). No MCP entries.
5. **Register agents via `aisdlc catalog add`** with path refs to
   `.claude/agents/*.md` (same pattern as `openspec-planner.json`).

## Riesgos / Trade-offs

- [Operators expect a `green` mapped stage] → Mitigation: FACTORY-ROADMAP +
  proposal evidence that no green gate exists; pre-push/pre-pr cover G3.
- [Wrong skill id (folder vs frontmatter name)] → Mitigation: resolve via
  `catalog list --kind skills` before wiring.
- [Doctor fails on unknown agent] → Mitigation: add catalog JSON before knobs;
  run doctor in Gate 2/3.

## Testing Strategy

| Scenario (spec) | Test level | Test class / file |
|-----------------|------------|-------------------|
| Notaire foreman plan maps ≥4 real stages | kit/adopter verify | `test_lifecycle.py` and/or manual `foreman plan --json` + unittest assert in kit Notaire check |
| unknown agent still fails closed | existing | `test_stage_profiles.py` (regression) |
| doctor clean with signal alias | adopter | `aisdlc doctor` / lifecycle Notaire doctor |
| no green stage id without gate | profile load | document + optional assert green absent from plan stages |

- New unit tests: only if kit behavior changes; prefer adopter assertion in
  existing Notaire lifecycle/doctor verify path
- Coverage impact: kit Python only if tests added; JaCoCo N/A

## Regression Strategy

- Existing tests: `test_stage_profiles.py`, `test_foreman_plan.py`,
  `test_lifecycle.py` / `test_init_doctor.py` (Notaire doctor)
- Full suite: `bash framework/ai-sdlc/verify.sh` + `preflight.sh --full`
- HTTP/Bruno: n/a
- Legacy paths: none

## Playwright Strategy

n/a — no UI surface (profile / catalogs / docs)

## Deployment Strategy

- Flyway: no
- Ships as adopter profile + docs; no env keys
- Smoke (Gate 5): `aisdlc --profile .aisdlc/aisdlc.toml foreman plan --json`
  shows mapped_count ≥ 4; doctor exit 0

## Rollback Strategy

Revert the PR. Safe — declarative knobs + catalog path refs only.

## Migration Plan

None. Existing `plan` knobs retained.
