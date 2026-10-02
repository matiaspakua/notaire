> Governed by [CONSTITUTION.md](../../../CONSTITUTION.md) — §6 Quality Gates,
> §7 Testing Rules, §11 Release Rules.

## Context

`.claude/rules/refactoring.md` is `alwaysApply: true` and still encodes the
2025 Swing-client migration plan. `scripts/check-agent-rules.sh` already
guards emptiness and dead backticked paths (Requirement: Agent rule files stay
valid) and is wired into `preflight.sh` and `sdlc-process.yml`. Migration
dashboards under `docs/github/` were already archived to
`docs/000-archive/github/`; live status lives on the Delivery Board and in
`docs/300-development/DEVELOPMENT-PLAN.md` / SAD phase tables.

## Goals / Non-Goals

**Goals:**

- Make `refactoring.md` match the current architecture agents must follow.
- Mechanically prevent reintroduction of the obsolete markers via
  `check-agent-rules.sh`.
- Point readers at one live migration/delivery status source; keep archived
  dashboards archived.

**Non-Goals:**

- Renaming packages in production code or rewriting CLAUDE.md/AGENTS.md package
  tables (`negocio` vs `business`) — separate docs drift, not this issue's AC.
- Touching `local-ai/` or product feature code.
- Rewriting historical ADR narratives that correctly describe past decisions.

## Decisions

- **Rewrite in place, do not archive `refactoring.md`.** The file is still the
  right always-loaded home for layering / DTO / jpa→repository refactoring
  rules; only the obsolete *target* is wrong. Archiving would drop the still-
  valid guidance agents need.
- **Extend `check-agent-rules.sh` rather than a new script.** Same CI hook,
  same preflight gate, KIS. Scope the new assertions to
  `.claude/rules/refactoring.md` only so historical mentions of Swing in ADRs
  / archives stay untouched.
- **Cloud branch prefix.** Platform requires `cursor/...-f458`; Constitution
  form `docs/1070_…` is noted in traceability as the preferred name when the
  platform allows it.
- **Docs-only Maven/Playwright:** no application code; Gate 2 evidence is the
  red→green agent-rules script. Full `mvn test` / Playwright are regression
  only (unchanged).

## Riesgos / Trade-offs

- [Risk] Marker list is string-based and could false-positive on a historical
  sentence inside `refactoring.md` → Mitigation: rewrite must not retain those
  strings; if a historical note is needed, put it in `docs/000-archive/` and
  link out.
- [Risk] Broader docs still mention Swing historically (README deprecated
  client, ADRs) → Mitigation: AC only forbids Swing as a *target* outside
  archives; README/SAD edits clarify current vs historical.
- [Risk] Cannot apply `in-progress` label with this agent's `gh` token →
  Mitigation: recorded in traceability Exceptions; PR still references #1070.

## Testing Strategy

TDD (Constitution P1): extend `check-agent-rules.sh` first, run it against the
current obsolete `refactoring.md`, observe FAIL, then rewrite the rule and
observe PASS.

| Scenario (spec) | Test level | Test class / file |
|-----------------|------------|-------------------|
| Obsolete package root rejected | process / script | `scripts/check-agent-rules.sh` + `scripts/tests/test_pr_checks.py::AgentRulesTest` |
| Swing-as-target markers rejected | process / script | same |
| Obsolete Boot Java Postgres markers rejected | process / script | same |
| Current stack markers accepted | process / script | same |
| Empty / dead-path / no-files (existing) | process / script | unchanged `AgentRulesTest` cases |

- New unit tests (`src/test/java/.../unit/`): none — no Java surface
- New process self-tests: `AgentRulesTest` cases for #1070 markers
- New integration tests: none
- Coverage impact: none (no production Java/TS code)

## Regression Strategy

- Existing tests affected: none expected; `check-agent-rules.sh` gains
  assertions that only fire on `refactoring.md` content.
- Full suite command: `bash scripts/preflight.sh` (docs/rules change; Maven
  verify optional for confidence, not required to prove this AC).
- HTTP/Bruno API suite: n/a — no API change
- Legacy paths at risk: none

## Playwright Strategy

n/a — no UI surface. This change only updates agent rules, a shell guard, and
engineering documentation.

## Deployment Strategy

- Flyway migration required: no
- Deployment order / coupling: merge via PR; no runtime deploy dependency
- Configuration or `.env` keys to add: none
- Feature flag: no
- Smoke test after deploy (Gate 5): `bash scripts/check-agent-rules.sh` on
  `main` after merge; health endpoint unchanged

## Rollback Strategy

- Revert safe: yes — pure docs + script assert; revert restores previous rule
  text (undesirable but safe) and removes the new markers check
- Database rollback: none needed
- Data written under the new behavior after revert: none
- Blast radius if rollback is delayed: agents may again follow obsolete
  guidance until revert or re-fix

## Migration Plan

1. Add failing marker checks to `check-agent-rules.sh`.
2. Rewrite `refactoring.md`.
3. Fix DEVELOPMENT-PLAN / SAD / README status pointers.
4. Preflight + PR.

## Open Questions

None.
