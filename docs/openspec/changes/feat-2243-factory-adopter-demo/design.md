# Phase E — factory adopter demo design

> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — §6 Quality Gates,
> §7 Testing Rules, §11 Release Rules.

## Context

A–D shipped kit serve, backoffice, and runtime UIs. Phase E (#2243) proves reuse:
Notaire as sandbox adopter with ≥1 approved workflow tied to Foreman stage
agents/skills, plus a kit-neutral temp-project demo that keeps independence green.

## Goals / Non-Goals

**Goals:**
- Kit-neutral `aisdlc demo seed` (+ optional `--from-stages`) over catalogs/workflows
- Second-adopter temp project in kit tests (doctor / catalog validate / report green)
- Notaire `.aisdlc` catalogs: skills + one approved workflow with Foreman refs
- Documented demo path (CLI + UI entry for serve / backoffice / runtime)
- Independence: no adopter product paths in kit `aisdlc/`, `bin/`, `templates/`

**Non-Goals:**
- Public wheel sample repo / conformance matrix (#2256)
- Replacing Notaire product admin
- New OpenAPI paths or SPA
- Context Lake / Wilson vanity / fake Isolator / `required=true`
- Contending with MFA docs #2271

## Decisions

1. **`aisdlc/demo.py`** — application helper; seeds catalog entries then creates
   workflow → migrate v2 → define → request_validation → approve (`--owner-override`
   when `approvals_live=false`). Never executes agents/MCP.
2. **`--from-stages`** — collect agent/skill ids from `Profile.stage_profiles`;
   ensure catalog entries exist (add if missing); pin those in the workflow body.
   Default seed uses generic planner/implementer/reviewer + propose/apply/steward
   ids (kit-neutral; no adopter product names).
3. **MCP empty** — demo workflow does not declare vanity MCP (Foreman honesty).
4. **Notaire wiring** — run seed `--from-stages` (or equivalent committed catalog
   state) so the sandbox profile has an approved workflow for runtime picker.
5. **Docs split** — kit `docs/ADOPTER-DEMO.md` stays brand-independent; Notaire
   sandbox specifics live under `docs/300-development/306-ai-sdlc-framework/`.
6. **CHANGELOG** — omit Unreleased (fence #2271).

## Riesgos / Trade-offs

- [Committed digests drift if catalogs rewrite] → Prefer idempotent seed that
  skips when approved workflow already present; tests assert presence not raw digests.
- [Skills-dir discovery lacks digests] → Demo always materializes catalog entries
  before pinning (required for v2 validation pins).
- [#2256 public sample still open] → Phase E kit fixture satisfies “temp project”
  AC; public sample remains packaging remainder (document honestly).

## Testing Strategy

| Scenario (spec) | Test level | Test class / file |
|-----------------|------------|-------------------|
| demo seed creates approved workflow on temp project | unit | `tests/test_adopter_demo.py` |
| `--from-stages` pins Foreman agent/skill ids | unit | `tests/test_adopter_demo.py` |
| doctor / catalog validate / report green after seed | unit | `tests/test_adopter_demo.py` |
| kit independence still passes (no adopter markers) | unit | `tests/test_independence.py` + demo |
| Notaire sandbox has ≥1 approved workflow with stage refs | unit | `tests/test_notaire_adopter_demo.py` |
| Notaire doctor stays clean | unit | `tests/test_notaire_adopter_demo.py` |

## Regression Strategy

- Existing workflow / catalog / independence / Notaire Foreman tests must stay green
- Full suite: `bash framework/ai-sdlc/verify.sh` then
  `bash workspace/sdlc/preflight.sh --full`
- Bruno / product API: n/a

## Playwright Strategy

- n/a — kit-only demo/docs; no Notaire product UI tip
- Kit UI E2E stays in dedicated kit job if added later (`optional/ui-e2e.txt`)

## Open Questions

- None for Gate 1. Public second-adopter sample timing stays on #2256.

## Deployment Strategy

- Flyway: no
- Kit-only + adopter catalogs; smoke: `aisdlc demo seed --from-stages`, then
  `aisdlc workflow list --approved`, `aisdlc doctor`, `aisdlc catalog validate`,
  `aisdlc report`, and UI entry via `aisdlc serve` / `backoffice` / `runtime`

## Rollback Strategy

- Revert safe: yes — additive `demo` CLI + docs; adopter catalog files can be
  removed or demoted without schema migration
- Database rollback: none needed
- Data written under the new behavior after revert: approved workflow revisions
  remain valid catalog artifacts; digests unchanged by docs-only rollback
- Blast radius if rollback is delayed: local demo catalogs only; product admin
  untouched

## Migration Plan

- Idempotent `aisdlc demo seed` (skip when approved workflow already present);
  Notaire sandbox commits seeded catalogs; public wheel sample remains #2256
